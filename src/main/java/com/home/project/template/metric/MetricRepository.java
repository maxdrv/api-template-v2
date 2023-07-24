package com.home.project.template.metric;

import com.home.project.template.db.JdbcTemplatePerHost;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MetricRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JdbcTemplatePerHost jdbcTemplatePerHost;

    public List<Metric> tableTuples() {
        return jdbcTemplate.query("""
                select
                    psut.relname as key,
                    unnest(array[
                        'n_tup_ins', 'n_tup_upd', 'n_tup_hot_upd', 'n_tup_del', 'n_live_tup', 'n_dead_tup', 'reltuples', 'relpages'
                    ]) as subkey,
                    unnest(array[
                        n_tup_ins, n_tup_upd, n_tup_hot_upd, n_tup_del, n_live_tup, n_dead_tup, reltuples, relpages
                    ]) as value
                from pg_stat_user_tables psut
                    join pg_class pc on psut.relid = pc.oid
                where psut.relname in ('sortable');
                """, (rs, rowNum) -> {
            MetricIdentity metricIdentity = new MetricIdentity("tableTuples", rs.getString("key"), rs.getString("subkey"), "");
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
            MetricIdentity metricIdentity = new MetricIdentity("tableSpace", rs.getString("key"), rs.getString("subkey"), "");
            return new Metric(metricIdentity, rs.getLong("value"));
        });
    }

    public List<Metric> slru() {
        List<Metric> metrics = new ArrayList<>();
        for (var entry : jdbcTemplatePerHost.jdbcTemplates().entrySet()) {
            String host = entry.getKey();
            JdbcTemplate jdbcTemplate = entry.getValue();

            List<Metric> newMetrics = jdbcTemplate.query("""
                            select name                                                    as key,
                                   unnest(array ['blks_hit', 'blks_read', 'blks_written']) as subkey,
                                   unnest(array [blks_hit, blks_read, blks_written])       as value
                            from pg_stat_slru
                            where name in ('Subtrans', 'Xact')
                            """,
                    (rs, rowNum) -> {
                        MetricIdentity metricIdentity = new MetricIdentity("slru", rs.getString("key"), rs.getString("subkey"), host);
                        return new Metric(metricIdentity, rs.getLong("value"));
                    }
            );
            metrics.addAll(newMetrics);
        }
        return metrics;
    }

    public List<Metric> waitEvents() {
        List<Metric> metrics = new ArrayList<>();
        for (var entry : jdbcTemplatePerHost.jdbcTemplates().entrySet()) {
            String host = entry.getKey();
            JdbcTemplate jdbcTemplate = entry.getValue();

            List<Metric> newMetrics = jdbcTemplate.query("""
                            select wait_event_type as key,
                                   wait_event      as subkey,
                                   count(*)        as value
                            from pg_stat_activity
                            where wait_event_type is not null
                              and wait_event_type is not null
                            group by wait_event_type, wait_event;
                            """,
                    (rs, rowNum) -> {
                        MetricIdentity metricIdentity = new MetricIdentity("waits", rs.getString("key"), rs.getString("subkey"), host);
                        return new Metric(metricIdentity, rs.getLong("value"));
                    }
            );
            metrics.addAll(newMetrics);
        }
        return metrics;
    }

}
