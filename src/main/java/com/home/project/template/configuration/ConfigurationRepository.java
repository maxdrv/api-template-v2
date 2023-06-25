package com.home.project.template.configuration;

import com.home.project.template.generator.Sortable;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ConfigurationRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void upsert(String key, String value) {
        jdbcTemplate.update("""
                insert into configuration(created_at, updated_at, key, value)
                    values (now(), now(), ?, ?)
                    on conflict (key) do update set updated_at = now(), value = excluded.value
                """, key, value);
    }

    public List<Configuration> findAll() {
        return jdbcTemplate.query("select * from configuration", this::toConfiguration);
    }

    public Optional<Configuration> findByKey(String key) {
        List<Configuration> res = jdbcTemplate.query("select * from configuration where key = ?", this::toConfiguration, key);
        if (res.size() == 0) {
            return Optional.empty();
        }
        if (res.size() > 1) {
            throw new RuntimeException("should never happen size > 1 for config");
        }
        return Optional.of(res.get(0));
    }

    @Transactional
    public void delete(String key) {
        jdbcTemplate.update("delete from configuration where key = ?", key);
    }

    private Configuration toConfiguration(ResultSet rs, int rowNum) throws SQLException {
        return new Configuration(
                rs.getLong("id"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getString("key"),
                rs.getString("value")
        );
    }

}
