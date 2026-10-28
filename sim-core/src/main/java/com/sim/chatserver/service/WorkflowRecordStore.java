package com.sim.chatserver.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.sql.DataSource;

public final class WorkflowRecordStore {
    private static final String SQL_FIND = "SELECT payload,touched_at FROM sim_workflow_records WHERE record_kind=? AND record_id=? AND expires_at>?";
    private static final String SQL_INSERT = "INSERT INTO sim_workflow_records(record_kind,record_id,payload,touched_at,expires_at) VALUES(?,?,?,?,?)";
    private static final String SQL_REPLACE = "UPDATE sim_workflow_records SET payload=?,touched_at=?,expires_at=? WHERE record_kind=? AND record_id=? AND payload=? AND touched_at<=? AND expires_at>?";
    private static final String SQL_TOUCH = "UPDATE sim_workflow_records SET touched_at=? WHERE record_kind=? AND record_id=?";
    private static final String SQL_DELETE = "DELETE FROM sim_workflow_records WHERE record_kind=? AND record_id=?";
    private static final String SQL_LIST = "SELECT record_id,payload,touched_at FROM sim_workflow_records WHERE record_kind=? AND expires_at>?";
    private static final String SQL_PURGE = "DELETE FROM sim_workflow_records WHERE expires_at<=?";
    private static final String MSG_UNAVAILABLE = "Durable workflow storage is unavailable";

    public record StoredRecord(String payload, long touchedAt) { }

    private final Supplier<DataSource> dataSource;
    private final LongSupplier clock;

    WorkflowRecordStore(Supplier<DataSource> dataSource) {
        this(dataSource, System::currentTimeMillis);
    }

    WorkflowRecordStore(Supplier<DataSource> dataSource, LongSupplier clock) {
        this.dataSource = dataSource;
        this.clock = clock;
    }

    public StoredRecord find(String kind, String id) {
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(SQL_FIND)) {
            statement.setString(1, kind);
            statement.setString(2, id);
            statement.setLong(3, clock.getAsLong());
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new StoredRecord(result.getString(1), result.getLong(2)) : null;
            }
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    public void insert(String kind, String id, String payload, long retentionMillis) {
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            long now = clock.getAsLong();
            statement.setString(1, kind);
            statement.setString(2, id);
            statement.setString(3, payload);
            statement.setLong(4, now);
            statement.setLong(5, now + retentionMillis);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    boolean replace(String kind, String id, String expected, String payload, long retentionMillis, long staleBefore) {
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(SQL_REPLACE)) {
            long now = clock.getAsLong();
            statement.setString(1, payload);
            statement.setLong(2, now);
            statement.setLong(3, now + retentionMillis);
            statement.setString(4, kind);
            statement.setString(5, id);
            statement.setString(6, expected);
            statement.setLong(7, staleBefore);
            statement.setLong(8, now);
            return statement.executeUpdate() == 1;
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    void touch(String kind, String id) {
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(
                SQL_TOUCH)) {
            statement.setLong(1, clock.getAsLong());
            statement.setString(2, kind);
            statement.setString(3, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    void delete(String kind, String id) {
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(
                SQL_DELETE)) {
            statement.setString(1, kind);
            statement.setString(2, id);
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    Map<String, StoredRecord> list(String kind) {
        Map<String, StoredRecord> records = new LinkedHashMap<>();
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(
                SQL_LIST)) {
            statement.setString(1, kind);
            statement.setLong(2, clock.getAsLong());
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    records.put(result.getString(1), new StoredRecord(result.getString(2), result.getLong(3)));
                }
            }
            return records;
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    void purgeExpired() {
        try (Connection connection = dataSource.get().getConnection(); PreparedStatement statement = connection.prepareStatement(
                SQL_PURGE)) {
            statement.setLong(1, clock.getAsLong());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw unavailable(ex);
        }
    }

    private IllegalStateException unavailable(SQLException ex) {
        return new IllegalStateException(MSG_UNAVAILABLE, ex);
    }

    long currentTimeMillis() {
        return clock.getAsLong();
    }
}