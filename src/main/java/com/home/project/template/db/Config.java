package com.home.project.template.db;

import com.zaxxer.hikari.util.DriverDataSource;
import one.util.streamex.EntryStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;
import java.util.Properties;

@Configuration
@Profile("!test")
public class Config {

    @Value("${spring.datasource.username}")
    private String driverName;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${pg.url.master}")
    private String masterUrl;

    @Value("${pg.url.replica}")
    private String replicaUrl;

    @Bean
    public DataSourcePerHost dataSourcePerHost() {
        var readOnlyProps = new Properties();
        readOnlyProps.put("readOnly", "true");
        readOnlyProps.put("prepareThreshold", "0");
        readOnlyProps.put("connectTimeout", "1");

        return new DataSourcePerHost(
                Map.of(
                        "master", new DriverDataSource(masterUrl, driverName, readOnlyProps, username, password),
                        "replica", new DriverDataSource(replicaUrl, driverName, readOnlyProps, username, password)
                )
        );
    }

    @Bean
    public JdbcTemplatePerHost jdbcTemplatePerHost(DataSourcePerHost dataSourcePerHost) {
        Map<String, JdbcTemplate> jdbcTemplatePerHost = EntryStream.of(dataSourcePerHost.dataSourcePerHost())
                .mapValues(JdbcTemplate::new)
                .toMap();
        return new JdbcTemplatePerHost(jdbcTemplatePerHost);
    }

}
