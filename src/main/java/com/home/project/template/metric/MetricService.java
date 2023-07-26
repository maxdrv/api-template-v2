package com.home.project.template.metric;

import com.home.project.template.configuration.ConfigurationRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class MetricService {

    private final ConfigurationRepository configurationRepository;
    private final MeterRegistry meterRegistry;
    private final MetricRepository metricRepository;
    private final Map<MetricIdentity, AtomicLong> metrics;

    public MetricService(
            ConfigurationRepository configurationRepository,
            MeterRegistry meterRegistry,
            MetricRepository metricRepository
    ) {
        this.configurationRepository = configurationRepository;
        this.meterRegistry = meterRegistry;
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

        boolean monitorSlru = configurationRepository.findByKey("slru")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);
        if (monitorSlru) {
            for (Metric metric : metricRepository.slru()) {
                MetricIdentity identity = metric.identity();
                Tag slru = Tag.of("slru", identity.key());
                Tag type = Tag.of("blks", identity.subKey());
                Tag host = Tag.of("host_custom", identity.host());
                metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(slru, type, host), new AtomicLong(0)));
            }
        }

        for (String host : List.of("master", "replica")) {
            Const.WAIT_EVENTS.forEach((type, names) -> {
                for (String name : names) {
                    var identity = new MetricIdentity("wait", type, name, host);
                    Tag waitType = Tag.of("wait_type", identity.key());
                    Tag waitName = Tag.of("wait_name", identity.subKey());
                    Tag host1 = Tag.of("host_custom", identity.host());
                    metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(waitType, waitName, host1), new AtomicLong(0)));
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

        boolean monitorSlru = configurationRepository.findByKey("slru")
                .map(conf -> Objects.equals("true", conf.value()))
                .orElse(false);

        if (monitorSlru) {
            for (Metric metric : metricRepository.slru()) {
                metrics.get(metric.identity()).set(metric.value());
            }
        }

        for (Metric metric : metricRepository.waitEvents()) {
            metrics.get(metric.identity()).set(metric.value());
        }
    }

}
