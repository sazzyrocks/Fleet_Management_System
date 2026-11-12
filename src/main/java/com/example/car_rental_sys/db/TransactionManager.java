package com.example.car_rental_sys.db;

import com.example.car_rental_sys.dao.DAOException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Functional Transaction Manager for executing multi-step database mutations with ACID guarantees.
 */
public final class TransactionManager {

    private static final Logger LOGGER = Logger.getLogger(TransactionManager.class.getName());

    private TransactionManager() {
        // Prevent instantiation
    }

    /**
     * Functional callback interface for operations requiring transactional isolation.
     *
     * @param <T> Return type of the transaction block
     */
    @FunctionalInterface
    public interface TransactionCallback<T> {
        T doInTransaction(Connection connection) throws SQLException;
    }

    /**
     * Executes a callback within a managed ACID transaction boundary.
     * Automatically commits on success and rolls back on any exception.
     *
     * @param callback the transactional block to execute
     * @param <T>      result type
     * @return the result returned by the callback
     * @throws DAOException if transaction fails or rolls back
     */
    public static <T> T execute(TransactionCallback<T> callback) throws DAOException {
        Connection conn = null;
        boolean originalAutoCommit = true;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            T result = callback.doInTransaction(conn);
            conn.commit();
            return result;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.log(Level.WARNING, "Transaction rolled back due to error: {0}", e.getMessage());
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Failed to rollback transaction", rollbackEx);
                }
            }
            throw new DAOException("Transaction execution failed: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(originalAutoCommit);
                } catch (SQLException ignored) {
                }
                try {
                    conn.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
