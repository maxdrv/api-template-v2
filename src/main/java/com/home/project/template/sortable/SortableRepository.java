package com.home.project.template.sortable;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SortableRepository {

    private final JdbcTemplate jdbcTemplate;

    public Sortable findByIdOrThrow(Long id) {
        return jdbcTemplate.queryForObject("select * from sortable where id = ?", this::toSortable, id);
    }

    public List<Sortable> insert(Long sortingCenterId, String type, Stage stage, String barcode) {
        return jdbcTemplate.query(
                """
                    insert into sortable(created_at, updated_at, sc_id, status, type, stage_id, barcode)
                    values (now(), now(), ?, ?, ?, ?, ?)
                    returning *
                """, this::toSortable, sortingCenterId, stage.status(), type, stage.id(), barcode);
    }

    public void update(Long id, Stage stage) {
        jdbcTemplate.update("""
                    update sortable set updated_at = now(), status = ?, stage_id = ?
                    where id = ?
                """, stage.status(), stage.id(), id);
    }

    public int delete(Long archiveId) {
        return jdbcTemplate.update("delete from sortable where archive_id = ?", archiveId);
    }

    private Sortable toSortable(ResultSet rs, int rowNum) throws SQLException {
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
