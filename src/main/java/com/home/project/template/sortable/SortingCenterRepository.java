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
public class SortingCenterRepository {

    private final JdbcTemplate jdbcTemplate;
    private final CopyOnWriteArrayList<SortingCenter> sortingCenters = new CopyOnWriteArrayList<>();
    private final Object monitor = new Object();


    public List<SortingCenter> findAllCached() {
        if (sortingCenters.isEmpty()) {
            List<SortingCenter> all = findAll();
            synchronized (monitor) {
                if (sortingCenters.isEmpty()) {
                    sortingCenters.addAll(all);
                }
            }
        }
        return Collections.unmodifiableList(sortingCenters);
    }


    public List<SortingCenter> findAll() {
        return jdbcTemplate.query(
                "select * from sorting_center",
                (ResultSet rs, int rowNum) -> {
                    return new SortingCenter(rs.getLong("id"), rs.getString("name"));
                });
    }

}
