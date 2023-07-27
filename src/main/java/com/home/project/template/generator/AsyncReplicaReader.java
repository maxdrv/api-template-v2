package com.home.project.template.generator;

import com.home.project.template.db.JdbcTemplatePerHost;
import com.home.project.template.metric.Const;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AsyncReplicaReader {

    private final JdbcTemplatePerHost jdbcTemplatePerHost;

    @Transactional
    public void doJob() {
        JdbcTemplate jdbcTemplate = jdbcTemplatePerHost.get(Const.REPLICA_ASYNC);

        List<String> strings = jdbcTemplate.queryForList(
                "select status || ' '  || count(*) as smth  from sortable where id % 2 = 0 group by status;",
                String.class
        );

    }


}
