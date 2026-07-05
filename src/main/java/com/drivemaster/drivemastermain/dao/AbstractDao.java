package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import com.drivemaster.drivemastermain.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic hand-written JDBC DAO base class (deliberately not Spring Data/JPA).
 * Subclasses supply table-specific SQL and row-mapping; this class owns
 * connection borrowing/releasing and translates every {@link SQLException}
 * into an unchecked {@link DataAccessException}.
 */
public abstract class AbstractDao<T, ID> {

    protected final ConnectionPool connectionPool;

    protected AbstractDao(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    protected abstract String tableName();

    protected abstract RowMapper<T> rowMapper();

    protected abstract String insertSql();

    protected abstract String updateSql();

    protected abstract void bindForInsert(PreparedStatement ps, T entity) throws SQLException;

    protected abstract void bindForUpdate(PreparedStatement ps, T entity) throws SQLException;

    protected abstract ID extractGeneratedId(ResultSet keys) throws SQLException;

    protected abstract T withId(T entity, ID id);

    public Optional<T> findById(ID id) {
        return queryOne("SELECT * FROM " + tableName() + " WHERE id = ?", id);
    }

    public List<T> findAll() {
        return query("SELECT * FROM " + tableName());
    }

    public T insert(T entity) {
        Connection conn = connectionPool.borrowConnection();
        try {
            return insert(conn, entity);
        } finally {
            closeQuietly(conn);
        }
    }

    public T insert(Connection conn, T entity) {
        try (PreparedStatement ps = conn.prepareStatement(insertSql(), Statement.RETURN_GENERATED_KEYS)) {
            bindForInsert(ps, entity);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return withId(entity, extractGeneratedId(keys));
                }
            }
            throw new DataAccessException("Insert into " + tableName() + " did not return a generated key", null);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert into " + tableName(), e);
        }
    }

    public int update(T entity) {
        Connection conn = connectionPool.borrowConnection();
        try {
            return update(conn, entity);
        } finally {
            closeQuietly(conn);
        }
    }

    public int update(Connection conn, T entity) {
        try (PreparedStatement ps = conn.prepareStatement(updateSql())) {
            bindForUpdate(ps, entity);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update " + tableName(), e);
        }
    }

    public boolean deleteById(ID id) {
        return executeUpdate("DELETE FROM " + tableName() + " WHERE id = ?", id) > 0;
    }

    protected List<T> query(String sql, Object... params) {
        Connection conn = connectionPool.borrowConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> results = new ArrayList<>();
                RowMapper<T> mapper = rowMapper();
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        } finally {
            closeQuietly(conn);
        }
    }

    protected Optional<T> queryOne(String sql, Object... params) {
        List<T> results = query(sql, params);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    protected int executeUpdate(String sql, Object... params) {
        Connection conn = connectionPool.borrowConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.setParameters(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Update failed: " + sql, e);
        } finally {
            closeQuietly(conn);
        }
    }

    protected double queryDouble(String sql, Object... params) {
        Connection conn = connectionPool.borrowConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0d;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Query failed: " + sql, e);
        } finally {
            closeQuietly(conn);
        }
    }

    protected long queryCount(String sql, Object... params) {
        Connection conn = connectionPool.borrowConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            JdbcUtils.setParameters(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Count query failed: " + sql, e);
        } finally {
            closeQuietly(conn);
        }
    }

    protected void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException ignored) {
            // returning the connection to the pool; nothing actionable here
        }
    }
}
