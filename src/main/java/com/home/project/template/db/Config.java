package com.home.project.template.db;

import com.home.project.template.util.UrlUtil;
import com.zaxxer.hikari.util.DriverDataSource;
import one.util.streamex.EntryStream;
import one.util.streamex.StreamEx;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
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

    @Bean
    public DataSourcePerHost dataSourcePerHost() {
        var readOnlyProps = new Properties();
        readOnlyProps.put("readOnly", "true");
        readOnlyProps.put("prepareThreshold", "0");
        readOnlyProps.put("connectTimeout", "1");

        Map<String, DataSource> dataSourcePerHost = StreamEx.of(UrlUtil.splitPgUrlByHosts(url))
                .mapToEntry(UrlUtil::getHostFromPgUrl, val -> val)
                .mapValues(pgUrl ->
                        new DriverDataSource(pgUrl, driverName, readOnlyProps, username, password)
                )
                .mapValues(ds -> (DataSource) ds)
                .toMap();

        return new DataSourcePerHost(dataSourcePerHost);
    }

    @Bean
    public JdbcTemplatePerHost jdbcTemplatePerHost(DataSourcePerHost dataSourcePerHost) {
        Map<String, JdbcTemplate> jdbcTemplatePerHost = EntryStream.of(dataSourcePerHost.dataSourcePerHost())
                .mapValues(JdbcTemplate::new)
                .toMap();
        return new JdbcTemplatePerHost(jdbcTemplatePerHost);
    }

}
