package com.home.project.template.sortable;

import lombok.RequiredArgsConstructor;
import one.util.streamex.StreamEx;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SortableRepository {

    private final JdbcTemplate jdbcTemplate;

    public Sortable findByIdOrThrow(Long id) {
        return jdbcTemplate.queryForObject("select * from sortable where id = ?", SortableMapper::toSortable, id);
    }

    public List<Sortable> findAllByIds(List<Long> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }
        String inClauseContent = ids.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        String sql = "select * from sortable where id in (" + inClauseContent + ")";

        return jdbcTemplate.query(sql, SortableMapper::toSortable);
    }

    public List<Sortable> insert(Long sortingCenterId, String type, Stage stage, String barcode) {
        return jdbcTemplate.query(
                """
                            insert into sortable(created_at, updated_at, sc_id, status, type, stage_id, barcode)
                            values (now(), now(), ?, ?, ?, ?, ?)
                            returning *
                        """, SortableMapper::toSortable, sortingCenterId, stage.status(), type, stage.id(), barcode);
    }

    public List<Long> insertMulti(SortableInsertMulti inserts) {
        if (inserts.inserts().size() < 1) {
            return Collections.emptyList();
        }

        String getIdsSql = "select nextval('sortable_id_seq') as next_id from generate_series(1, " + inserts.inserts().size() + ")";
        List<Long> nextIds = jdbcTemplate.query(getIdsSql, (rs, rowNum) -> rs.getLong("next_id"));

        record InsertWithId(Long id, Long sortingCenterId, String type, Stage stage, String barcode) {
        }

        List<InsertWithId> withIds = StreamEx.zip(inserts.inserts(), nextIds, (insert, id) -> new InsertWithId(
                id,
                insert.sortingCenterId(),
                insert.type(),
                insert.stage(),
                insert.barcode()
        )).toList();

        jdbcTemplate.batchUpdate("""
                 insert into sortable(id, created_at, updated_at, sc_id, status, type, stage_id, barcode)
                            values (?, now(), now(), ?, ?, ?, ?, ?)
                """, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                InsertWithId insert = withIds.get(i);

                ps.setLong(1, insert.id());
                ps.setLong(2, insert.sortingCenterId());
                ps.setString(3, insert.stage().status());
                ps.setString(4, insert.type());
                ps.setLong(5, insert.stage().id());
                ps.setString(6, insert.barcode());
            }

            @Override
            public int getBatchSize() {
                return withIds.size();
            }
        });

        return nextIds;
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

}
