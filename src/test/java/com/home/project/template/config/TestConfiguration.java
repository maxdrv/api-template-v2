package com.home.project.template.config;

import io.zonky.test.db.postgres.embedded.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Configuration
public class TestConfiguration {

    @Bean
    public DataSource dataSource() throws SQLException {
        DatabasePreparer preparer = LiquibasePreparer.forClasspathLocation("db/changelog/changelog.xml");

        List<Consumer<EmbeddedPostgres.Builder>> builderCustomizers = new CopyOnWriteArrayList<>();

        PreparedDbProvider provider = PreparedDbProvider.forPreparer(preparer, builderCustomizers);
        ConnectionInfo connectionInfo = provider.createNewDatabase();
        return provider.createDataSourceFromConnectionInfo(connectionInfo);
    }

    @Bean(name = "mockMvc")
    public MockMvc mockMvc(WebApplicationContext webApplicationContext) {
        return MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

}
