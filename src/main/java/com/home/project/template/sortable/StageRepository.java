package com.home.project.template.sortable;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@RequiredArgsConstructor
public class StageRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CopyOnWriteArrayList<Stage> stages = new CopyOnWriteArrayList<>();
    private final Object monitor = new Object();


    public List<Stage> findAllCached() {
        if (stages.isEmpty()) {
            List<Stage> all = findAll();
            synchronized (monitor) {
                if (stages.isEmpty()) {
                    stages.addAll(all);
                }
            }
        }
        return Collections.unmodifiableList(stages);
    }


    public List<Stage> findAll() {
        return jdbcTemplate.query(
                "select * from stage",
                (ResultSet rs, int rowNum) -> {
                    return new Stage(rs.getLong("id"), rs.getString("system_name"), rs.getString("status"));
                });
    }

}
