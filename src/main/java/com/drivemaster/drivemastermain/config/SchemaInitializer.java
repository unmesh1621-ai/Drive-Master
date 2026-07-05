package com.drivemaster.drivemastermain.config;

import com.drivemaster.drivemastermain.dao.pool.ConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
@Order(1)
public class SchemaInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SchemaInitializer.class);

    private final ConnectionPool connectionPool;
    private final AppProperties props;

    public SchemaInitializer(ConnectionPool connectionPool, AppProperties props) {
        this.connectionPool = connectionPool;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        if (!props.getDatasource().isInitSchema()) {
            log.info("Schema initialization disabled (app.datasource.init-schema=false)");
            return;
        }

        String script = readScript();
        Connection conn = connectionPool.borrowConnection();
        try (Statement statement = conn.createStatement()) {
            for (String sql : script.split(";")) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    statement.execute(trimmed);
                }
            }
            log.info("Database schema initialized/verified");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database schema", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        }
    }

    private String readScript() throws IOException {
        try (InputStream in = new ClassPathResource("db/schema.sql").getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
