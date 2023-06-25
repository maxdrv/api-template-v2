package com.home.project.template.generator;

import com.home.project.template.configuration.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.yoomoney.tech.dbqueue.api.EnqueueParams;
import ru.yoomoney.tech.dbqueue.api.impl.ShardingQueueProducer;
import ru.yoomoney.tech.dbqueue.spring.dao.SpringDatabaseAccessLayer;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoadGenerator {

    private final StageRepository stageRepository;
    private final SortableRepository sortableRepository;
    private final ShardingQueueProducer<String, SpringDatabaseAccessLayer> stringQueueProducer;
    private final ConfigurationRepository configurationRepository;
    private final List<String> types = List.of("PALLET", "PLACE");
    private final Random random = new Random();

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

        if (run) {
            insert(10);
            log.info("total inserted: " + insertCnt.get());
        }
    }

    private void insert(int amount) {
        for (int i = 0; i < amount; i++) {
            insert();
        }
    }

    private void insert() {
        List<Stage> stages = stageRepository.findAllCached();
        Stage stage = rand(stages);

        List<Sortable> created = sortableRepository.insert(rand(types), stage, nextBarcode());

        created.forEach(s -> {
            EnqueueParams<String> params = EnqueueParams.create(String.valueOf(s.id())).withExecutionDelay(Duration.ofSeconds(2));
            stringQueueProducer.enqueue(params);
        });

        insertCnt.addAndGet(created.size());
    }


    private <T> T rand(List<T> any) {
        int idx = random.nextInt(any.size());
        return any.get(idx);
    }

    private String nextBarcode() {
        return appStartTime + "-" + seq.getAndIncrement();
    }

    private Sortable toSortable(ResultSet rs, int rowNum) throws SQLException {
        return new Sortable(
                rs.getLong("id"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getString("status"),
                rs.getString("type"),
                rs.getLong("stage_id"),
                rs.getString("barcode")
        );
    }

}
