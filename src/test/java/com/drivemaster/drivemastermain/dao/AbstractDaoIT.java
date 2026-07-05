package com.drivemaster.drivemastermain.dao;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Base class for DAO integration tests that run against a real MySQL schema
 * (see the "drivemaster_test" setup in the project run instructions). Only
 * executed via {@code mvnw verify} (Failsafe), never plain {@code mvnw test}.
 */
public abstract class AbstractDaoIT {

    protected static ConnectionPool connectionPool;

    @BeforeAll
    static void setUpPool() throws IOException {
        String url = envOrDefault("TEST_DB_URL",
                "jdbc:mysql://localhost:3306/drivemaster_app_test?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
        String username = envOrDefault("TEST_DB_USERNAME", "drivemaster_app");
        String password = envOrDefault("TEST_DB_PASSWORD", "ChangeMe_123!");
        connectionPool = new ConnectionPool(url, username, password, 2, 5, 5000);
        initSchema();
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    private static void initSchema() throws IOException {
        String script;
        try (InputStream in = AbstractDaoIT.class.getClassLoader().getResourceAsStream("db/schema.sql")) {
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Connection conn = connectionPool.borrowConnection();
        try (Statement statement = conn.createStatement()) {
            for (String sql : script.split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize test schema", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    @BeforeEach
    void truncateTables() {
        Connection conn = connectionPool.borrowConnection();
        try (Statement statement = conn.createStatement()) {
            statement.execute("SET FOREIGN_KEY_CHECKS=0");
            statement.execute("TRUNCATE TABLE orders");
            statement.execute("TRUNCATE TABLE vehicles");
            statement.execute("TRUNCATE TABLE dealer_profiles");
            statement.execute("TRUNCATE TABLE users");
            statement.execute("SET FOREIGN_KEY_CHECKS=1");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to truncate test tables", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }
}
