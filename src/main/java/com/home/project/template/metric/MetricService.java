package com.home.project.template.metric;

import com.home.project.template.db.JdbcTemplatePerHost;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class MetricService {

    private final MetricRepository metricRepository;
    private final Map<MetricIdentity, AtomicLong> metrics;

    public MetricService(
            JdbcTemplatePerHost jdbcTemplatePerHost,
            MeterRegistry meterRegistry,
            MetricRepository metricRepository
    ) {
        this.metricRepository = metricRepository;
        this.metrics = new ConcurrentHashMap<>();

        for (Metric metric : metricRepository.tableTuples()) {
            MetricIdentity identity = metric.identity();
            Tag table = Tag.of("table", identity.key());
            Tag tuple = Tag.of("tuple", identity.subKey());
            Tag host = Tag.of("host", identity.host());
            metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(table, tuple, host), new AtomicLong(0)));
        }
        for (Metric metric : metricRepository.tableSpace()) {
            MetricIdentity identity = metric.identity();
            Tag table = Tag.of("table", identity.key());
            Tag space = Tag.of("space", identity.subKey());
            Tag host = Tag.of("host_custom", identity.host());
            metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(table, space, host), new AtomicLong(0)));
        }

        for (String host : jdbcTemplatePerHost.hosts()) {
            Const.SLRU.forEach((type, names) -> {
                for (String name : names) {
                    var identity = new MetricIdentity("slru", type, name, host);
                    Tag slruName = Tag.of("slru_name", identity.key());
                    Tag slruBlks = Tag.of("slru_blks", identity.subKey());
                    Tag hostCustom = Tag.of("host_custom", identity.host());
                    metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(slruName, slruBlks, hostCustom), new AtomicLong(0)));
                }
            });
        }

        for (String host : jdbcTemplatePerHost.hosts()) {
            Const.WAIT_EVENTS.forEach((type, names) -> {
                for (String name : names) {
                    var identity = new MetricIdentity("wait", type, name, host);
                    Tag waitType = Tag.of("wait_type", identity.key());
                    Tag waitName = Tag.of("wait_name", identity.subKey());
                    Tag hostCustom = Tag.of("host_custom", identity.host());
                    metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(waitType, waitName, hostCustom), new AtomicLong(0)));
                }
            });
        }
    }

    @Scheduled(fixedRate = 3000)
    public void extractMetrics() {
        for (Metric metric : metricRepository.tableTuples()) {
            metrics.get(metric.identity()).set(metric.value());
        }
        for (Metric metric : metricRepository.tableSpace()) {
            metrics.get(metric.identity()).set(metric.value());
        }

        for (Metric metric : metricRepository.slru()) {
            metrics.get(metric.identity()).set(metric.value());
        }

        for (Metric metric : metricRepository.waitEvents()) {
            metrics.get(metric.identity()).set(metric.value());
        }
    }

}
