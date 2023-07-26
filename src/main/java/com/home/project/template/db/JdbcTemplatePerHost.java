package com.home.project.template.db;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.Set;

public record JdbcTemplatePerHost(Map<String, JdbcTemplate> jdbcTemplates) {

    public JdbcTemplate get(String host) {
        return jdbcTemplates.get(host);
    }

    public Set<String> hosts() {
        return jdbcTemplates().keySet();
    }
}
