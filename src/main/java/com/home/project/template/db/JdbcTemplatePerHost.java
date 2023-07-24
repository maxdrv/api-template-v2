package com.home.project.template.db;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

public record JdbcTemplatePerHost(Map<String, JdbcTemplate> jdbcTemplates) {

}
