package com.home.project.template.generator;

import com.home.project.template.configuration.Configuration;
import com.home.project.template.configuration.ConfigurationRepository;
import com.home.project.template.db.JdbcTemplatePerHost;
import com.home.project.template.sortable.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yoomoney.tech.dbqueue.api.EnqueueParams;
import ru.yoomoney.tech.dbqueue.api.impl.ShardingQueueProducer;
import ru.yoomoney.tech.dbqueue.spring.dao.SpringDatabaseAccessLayer;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadGenerator {

    private final JdbcTemplatePerHost jdbcTemplatePerHost;
    private final JdbcTemplate jdbcTemplate;
    private final StageRepository stageRepository;
    private final SortingCenterRepository sortingCenterRepository;
    private final SortableRepository sortableRepository;
    private final ShardingQueueProducer<String, SpringDatabaseAccessLayer> stringQueueProducer;
    private final ConfigurationRepository configurationRepository;
    private final List<String> types = List.of("PALLET", "PLACE");
    private final Random random = new SecureRandom();

    private final long appStartTime = Instant.now().toEpochMilli();
    private final AtomicLong seq = new AtomicLong(10_000_000);
    private final AtomicLong insertCnt = new AtomicLong(0);


    /**
     * insert: 10 * 60 * 60 = 36000 в час
     * затем происходит отложенные dbQueue таски, которые проводят sortable до статуса shipped
     */
    @Scheduled(fixedDelay = 1000)
    public void inserter() {
        boolean run = configurationRepository.findByKey("insert")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        int cnt = configurationRepository.findByKey("insert_cnt")
                .map(Configuration::value)
                .map(Integer::parseInt)
                .orElse(10);

        List<Stage> stages = stageRepository.findAllCached();
        String startAt = configurationRepository.findByKey("starting_stage")
                .map(Configuration::value)
                .orElse("A");

        Stage startingStage = stages.stream()
                .filter(st -> Objects.equals(st.systemName(), startAt))
                .findFirst()
                .orElse(stages.get(0));

        List<SortingCenter> sortingCenters = sortingCenterRepository.findAllCached();
        Long sortingCenterId = rand(sortingCenters).id();

        insert(cnt, sortingCenterId, startingStage);
        log.info("total inserted: " + insertCnt.get());
    }

    private void insert(int amount, Long sortingCenterId, Stage startingStage) {
        List<SortableInsert> inserts = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            inserts.add(new SortableInsert(sortingCenterId, rand(types), startingStage, nextBarcode()));
        }

        List<Long> ids = sortableRepository.insertMulti(new SortableInsertMulti(inserts));

        String idsString = ids.stream().map(Object::toString).collect(Collectors.joining(","));

        EnqueueParams<String> params = EnqueueParams.create(idsString).withExecutionDelay(Duration.ofSeconds(2));
        stringQueueProducer.enqueue(params);

        insertCnt.addAndGet(amount);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void archiveMarker() {
        boolean run = configurationRepository.findByKey("archive_marker")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        int cnt = configurationRepository.findByKey("archive_cnt")
                .map(Configuration::value)
                .map(Integer::parseInt)
                .orElse(600);


        Long archiveId = jdbcTemplate.queryForObject("""
                insert into archive(created_at, updated_at, marked, deleted)
                values (now(), now(), null, null)
                returning id
                """, Long.class);


        int updateCnt = jdbcTemplate.update("""
                update sortable set archive_id = ? where id in (
                    select id from sortable where archive_id is null and status = 'SHIPPED' order by id asc limit ?
                )
                """, archiveId, cnt);

        jdbcTemplate.update("update archive set marked = ? where id = ?", updateCnt, archiveId);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void archiveDeleter() {
        boolean run = configurationRepository.findByKey("archive_deleter")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        Optional<Long> archiveIdO = findArchive();
        if (archiveIdO.isEmpty()) {
            log.info("archive not found");
            return;
        }

        int deletedCnt = sortableRepository.delete(archiveIdO.get());
        jdbcTemplate.update("update archive set deleted = ? where id = ?", deletedCnt, archiveIdO.get());
    }

    record Cnt(Long cnt, String status) {
    }

    @Scheduled(fixedDelay = 500)
    public void aggReplica() {
        boolean run = configurationRepository.findByKey("agg_replica")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        JdbcTemplate replica = jdbcTemplatePerHost.get("replica");
        List<Cnt> cnt = replica.query(
                "select count(*) cnt, status  from sortable group by status",
                (rs, rowNum) -> new Cnt(rs.getLong("cnt"), rs.getString("status"))
        );
    }

    @Scheduled(fixedDelay = 500)
    public void selectReplica() {
        boolean run = configurationRepository.findByKey("select_replica")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        JdbcTemplate replica = jdbcTemplatePerHost.get("replica");

        List<Sortable> sortableList = replica.query(
                "select * from sortable where status = 'IN_PROGRESS' limit 2000",
                SortableMapper::toSortable
        );
    }

    @Scheduled(fixedDelay = 500)
    public void aggMaster() {
        boolean run = configurationRepository.findByKey("agg_master")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        JdbcTemplate replica = jdbcTemplatePerHost.get("master");
        List<Cnt> cnt = replica.query(
                "select count(*) cnt, status  from sortable group by status",
                (rs, rowNum) -> new Cnt(rs.getLong("cnt"), rs.getString("status"))
        );
    }

    @Scheduled(fixedDelay = 500)
    public void selectMaster() {
        boolean run = configurationRepository.findByKey("select_master")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (!run) {
            return;
        }

        JdbcTemplate replica = jdbcTemplatePerHost.get("master");

        List<Sortable> sortableList = replica.query(
                "select * from sortable where status = 'IN_PROGRESS' limit 2000",
                SortableMapper::toSortable
        );
    }

    public Optional<Long> findArchive() {
        List<Long> res = jdbcTemplate.query(
                "select id from archive where marked is not null and deleted is null order by id asc limit 1",
                (rs, rowNum) -> rs.getLong("id")
        );
        if (res.size() == 0) {
            return Optional.empty();
        }
        if (res.size() > 1) {
            throw new RuntimeException("should never happen size > 1 findArchive");
        }
        return Optional.of(res.get(0));
    }

    private <T> T rand(List<T> any) {
        int idx = random.nextInt(any.size());
        return any.get(idx);
    }

    private String nextBarcode() {
        return appStartTime + "-" + seq.getAndIncrement();
    }

}
