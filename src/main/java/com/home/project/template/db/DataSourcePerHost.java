package com.home.project.template.db;

import javax.sql.DataSource;
import java.util.Map;

public record DataSourcePerHost(Map<String, DataSource> dataSourcePerHost) {
}
