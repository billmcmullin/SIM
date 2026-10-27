package com.sim.chatserver.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

public final class DatabaseTaskLock implements AutoCloseable {
    private static final String SQL_TRY_LOCK = "SELECT pg_try_advisory_xact_lock(hashtextextended(?,0))";
    private static final String TASK_NAMESPACE = "sim:";
    private static final String MSG_COORDINATION_FAILED = "Unable to coordinate task ";
    private static final String MSG_RELEASE_FAILED = "Unable to release task coordination connection";
    private final Connection connection;

    private DatabaseTaskLock(Connection connection) {
        this.connection = connection;
    }

    public static DatabaseTaskLock tryAcquire(DataSource dataSource, String task) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(SQL_TRY_LOCK)) {
                statement.setString(1, TASK_NAMESPACE + task);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next() && result.getBoolean(1)) {
                        return new DatabaseTaskLock(connection);
                    }
                }
            }
            new DatabaseTaskLock(connection).close();
            return null;
        } catch (SQLException ex) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeFailure) {
                    ex.addSuppressed(closeFailure);
                }
            }
            throw new IllegalStateException(MSG_COORDINATION_FAILED + task, ex);
        }
    }

    static DatabaseTaskLock localOnly() {
        return new DatabaseTaskLock(null);
    }

    @Override
    public void close() {
        if (connection == null) {
            return;
        }
        try (connection) {
            connection.rollback();
        } catch (SQLException ex) {
            throw new IllegalStateException(MSG_RELEASE_FAILED, ex);
        }
    }
}