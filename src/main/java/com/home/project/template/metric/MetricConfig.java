package com.home.project.template.metric;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Configuration
public class MetricConfig {

    /**
     * unified-agent не хочет работать с тегом name
     */
    @Bean
    public MeterFilter meterFilter() {
        return new MeterFilter() {
            @Override
            public Meter.Id map(Meter.Id id) {
                List<Tag> updatedTags = new ArrayList<>();
                for (Tag tag : id.getTags()) {
                    if (Objects.equals(tag.getKey(), "name")) {
                        updatedTags.add(Tag.of("name_custom", tag.getValue()));
                    } else {
                        updatedTags.add(tag);
                    }
                }
                return id.replaceTags(updatedTags);
            }
        };
    }

}
