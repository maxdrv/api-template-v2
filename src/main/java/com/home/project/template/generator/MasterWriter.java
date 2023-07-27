package com.home.project.template.generator;

import com.home.project.template.configuration.Configuration;
import com.home.project.template.configuration.ConfigurationRepository;
import com.home.project.template.sortable.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class MasterWriter {

    private final ConfigurationRepository configurationRepository;
    private final SortableRepository sortableRepository;
    private final StageRepository stageRepository;
    private final SortingCenterRepository sortingCenterRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void runLongerThanInstantTransaction() {
        List<Stage> stages = stageRepository.findAllCached();
        String startAt = configurationRepository.findByKey("starting_stage")
                .map(Configuration::value)
                .orElse("A");

        Stage startingStage = stages.stream()
                .filter(st -> Objects.equals(st.systemName(), startAt))
                .findFirst()
                .orElse(stages.get(0));

        List<SortingCenter> sortingCenters = sortingCenterRepository.findAllCached();

        var sc = new HashSet<>(sortingCenters).stream().findFirst().orElseThrow();
        Long sortingCenterId = sc.id();

        for (int i = 0; i < 60 * 2; i++) {
            sortableRepository.insert(sortingCenterId, "ANY_ONE", startingStage, UUID.randomUUID().toString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void twoInsertWithDelay(long delay) {
        List<Stage> stages = stageRepository.findAllCached();
        String startAt = configurationRepository.findByKey("starting_stage")
                .map(Configuration::value)
                .orElse("A");

        Stage startingStage = stages.stream()
                .filter(st -> Objects.equals(st.systemName(), startAt))
                .findFirst()
                .orElse(stages.get(0));

        List<SortingCenter> sortingCenters = sortingCenterRepository.findAllCached();

        var sc = new HashSet<>(sortingCenters).stream().findFirst().orElseThrow();
        Long sortingCenterId = sc.id();


        jdbcTemplate.update(
            """
                update sortable set updated_at = now() where id in (select id from sortable order by updated_at asc limit 10);
                """
        );
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        sortableRepository.insert(sortingCenterId, "ANY_ONE", startingStage, UUID.randomUUID().toString());
    }

}
