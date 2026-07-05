package com.drivemaster.drivemastermain.dao.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hand-written, thread-safe JDBC connection pool. Deliberately not backed by
 * HikariCP/Spring's DataSource abstraction: {@link #borrowConnection()} hands out
 * a dynamic proxy whose {@code close()} returns the connection to the pool instead
 * of closing the physical connection, so callers can use ordinary
 * {@code try (Connection c = pool.borrowConnection())} semantics.
 */
public class ConnectionPool {
    private static final Logger log = LoggerFactory.getLogger(ConnectionPool.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maxSize;
    private final long connectionTimeoutMs;

    private final BlockingQueue<Connection> available = new LinkedBlockingQueue<>();
    private final AtomicInteger totalCreated = new AtomicInteger(0);
    private volatile boolean shutDown = false;

    public ConnectionPool(String jdbcUrl, String username, String password,
                           int minSize, int maxSize, long connectionTimeoutMs) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.maxSize = maxSize;
        this.connectionTimeoutMs = connectionTimeoutMs;

        for (int i = 0; i < minSize; i++) {
            available.offer(createProxy(createPhysicalConnection()));
            totalCreated.incrementAndGet();
        }
        log.info("ConnectionPool initialized: min={}, max={}, url={}", minSize, maxSize, jdbcUrl);
    }

    public Connection borrowConnection() {
        if (shutDown) {
            throw new IllegalStateException("ConnectionPool has been shut down");
        }

        Connection conn = available.poll();
        if (conn != null) {
            return conn;
        }

        if (totalCreated.incrementAndGet() <= maxSize) {
            return createProxy(createPhysicalConnection());
        }
        totalCreated.decrementAndGet();

        try {
            conn = available.poll(connectionTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PoolExhaustedException("Interrupted while waiting for a database connection", e);
        }
        if (conn == null) {
            throw new PoolExhaustedException(
                    "Timed out waiting for a database connection (pool exhausted, max=" + maxSize + ")");
        }
        return conn;
    }

    void releaseConnection(Connection proxyConnection) {
        if (proxyConnection == null) {
            return;
        }
        if (shutDown) {
            closeQuietly(unwrap(proxyConnection));
            return;
        }

        boolean valid;
        try {
            valid = proxyConnection.isValid(1);
        } catch (SQLException e) {
            valid = false;
        }

        if (valid) {
            available.offer(proxyConnection);
            return;
        }

        log.warn("Discarding invalid connection and replenishing pool");
        closeQuietly(unwrap(proxyConnection));
        totalCreated.decrementAndGet();
        try {
            available.offer(createProxy(createPhysicalConnection()));
            totalCreated.incrementAndGet();
        } catch (RuntimeException e) {
            log.error("Failed to replenish connection pool after discarding invalid connection", e);
        }
    }

    public synchronized void shutdown() {
        shutDown = true;
        Connection proxyConn;
        while ((proxyConn = available.poll()) != null) {
            closeQuietly(unwrap(proxyConn));
        }
        log.info("ConnectionPool shut down");
    }

    private Connection createPhysicalConnection() {
        try {
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create database connection to " + jdbcUrl, e);
        }
    }

    private Connection createProxy(Connection real) {
        return (Connection) Proxy.newProxyInstance(
                ConnectionPool.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new PooledConnectionHandler(real, this));
    }

    private Connection unwrap(Connection proxyConnection) {
        if (Proxy.isProxyClass(proxyConnection.getClass())
                && Proxy.getInvocationHandler(proxyConnection) instanceof PooledConnectionHandler handler) {
            return handler.real;
        }
        return proxyConnection;
    }

    private void closeQuietly(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            log.warn("Error closing physical connection", e);
        }
    }

    private record PooledConnectionHandler(Connection real, ConnectionPool pool) implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if ("close".equals(name)) {
                pool.releaseConnection((Connection) proxy);
                return null;
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
