package com.drivemaster.drivemastermain.dao.tx;

import com.drivemaster.drivemastermain.dao.DataAccessException;
import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Hand-written transaction boundary around the custom {@link ConnectionPool}:
 * borrows a connection, disables auto-commit, runs the callback, commits on
 * success, and rolls back on any exception before releasing the connection.
 */
public class TransactionManager {
    private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

    private final ConnectionPool connectionPool;

    public TransactionManager(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public <T> T executeInTransaction(TransactionCallback<T> callback) {
        Connection conn = connectionPool.borrowConnection();
        try {
            conn.setAutoCommit(false);
            T result = callback.doInTransaction(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            try {
                conn.rollback();
            } catch (SQLException rollbackEx) {
                log.error("Failed to roll back transaction", rollbackEx);
            }
            if (e instanceof DataAccessException dae) {
                throw dae;
            }
            throw new DataAccessException("Transaction failed and was rolled back", e);
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                log.warn("Failed to restore autoCommit=true", e);
            }
            try {
                conn.close();
            } catch (SQLException e) {
                log.warn("Failed to release connection back to pool", e);
            }
        }
    }
}
