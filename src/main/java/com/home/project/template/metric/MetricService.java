package com.home.project.template.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class MetricService {
    private final MeterRegistry meterRegistry;
    private final MetricRepository metricRepository;
    private final Map<MetricIdentity, AtomicLong> metrics;

    public MetricService(MeterRegistry meterRegistry, MetricRepository metricRepository) {
        this.meterRegistry = meterRegistry;
        this.metricRepository = metricRepository;
        this.metrics = new ConcurrentHashMap<>();

    }

    @PostConstruct
    void init() {
        for (Metric metric : metricRepository.tableTuples()) {
            MetricIdentity identity = metric.identity();
            Tag table = Tag.of("table", identity.key());
            Tag tuple = Tag.of("tuple", identity.subKey());
            metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(table, tuple), new AtomicLong(0)));
        }
        for (Metric metric : metricRepository.tableSpace()) {
            MetricIdentity identity = metric.identity();
            Tag table = Tag.of("table", identity.key());
            Tag space = Tag.of("space", identity.subKey());
            metrics.put(identity, meterRegistry.gauge(identity.name(), List.of(table, space), new AtomicLong(0)));
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
    }

}
