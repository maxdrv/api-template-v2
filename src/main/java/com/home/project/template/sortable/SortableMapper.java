package com.home.project.template.sortable;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SortableMapper {

    public static Sortable toSortable(ResultSet rs, int rowNum) throws SQLException {
        return new Sortable(
                rs.getLong("id"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant(),
                rs.getLong("sc_id"),
                rs.getString("status"),
                rs.getString("type"),
                rs.getLong("stage_id"),
                rs.getString("barcode"),
                rs.getLong("archive_id")
        );
    }

}
