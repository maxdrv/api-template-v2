package com.home.project.template.metric;

import java.util.List;

public record MetricIdentity(String name, String key, String subKey, String host) {

    public List<String> tags() {
        return List.of(key, subKey);
    }
}
