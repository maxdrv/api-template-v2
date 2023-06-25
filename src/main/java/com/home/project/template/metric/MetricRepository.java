package com.home.project.template.metric;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Metric> tableTuples() {
        return jdbcTemplate.query("""
                select
                    psut.relname as key,
                    unnest(array[
                        'n_tup_ins', 'n_tup_upd', 'n_tup_hot_upd', 'n_tup_del', 'n_live_tup', 'n_dead_tup', 'reltuples', 'relpages'
                    ]) as subkey,
                    unnest(array[
                        n_tup_ins, n_tup_upd, n_tup_hot_upd, n_tup_del, n_live_tup, n_dead_tup, reltuples, relpages
                    ]) as value,
                    -- далее теги, для того что бы заполнить формат key-value
                    psut.relname as ownerId,
                    -1 AS scId,
                    '' as scName,
                    '' as comment
                from pg_stat_user_tables psut
                    join pg_class pc on psut.relid = pc.oid
                where psut.relname in ('sortable');
                """, (rs, rowNum) -> {
            MetricIdentity metricIdentity = new MetricIdentity("tableTuples", rs.getString("key"), rs.getString("subkey"));
            return new Metric(metricIdentity, rs.getLong("value"));
        });
    }

    public List<Metric> tableSpace() {
        return jdbcTemplate.query("""
                with monitored_tables as (
                    select unnest(array['sortable']) as name
                )
                select
                       monitored_tables.name as key,
                       unnest(array[
                           'pg_indexes_size',
                           'pg_table_size',
                           'pg_total_relation_size',
                           'fork_main',
                           'fork_fsm',
                           'fork_vm'
                       ]) as subkey,
                       unnest(array[
                           pg_indexes_size(monitored_tables.name),
                           pg_table_size(monitored_tables.name),
                           pg_total_relation_size(monitored_tables.name),
                           pg_relation_size(monitored_tables.name, 'main'),
                           pg_relation_size(monitored_tables.name, 'fsm'),
                           pg_relation_size(monitored_tables.name, 'vm')
                       ]) as value
                from monitored_tables;
                """, (rs, rowNum) -> {
            MetricIdentity metricIdentity = new MetricIdentity("tableSpace", rs.getString("key"), rs.getString("subkey"));
            return new Metric(metricIdentity, rs.getLong("value"));
        });
    }

}
