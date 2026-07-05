package com.drivemaster.drivemastermain.dao.pool;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConnectionPoolTest {

    private ConnectionPool pool;

    @AfterEach
    void tearDown() {
        if (pool != null) {
            pool.shutdown();
        }
    }

    @Test
    void closeReturnsConnectionToPoolInsteadOfClosingIt() throws Exception {
        pool = new ConnectionPool("jdbc:h2:mem:pool-return;DB_CLOSE_DELAY=-1", "sa", "", 1, 1, 2000);

        Connection first = pool.borrowConnection();
        first.close();
        Connection second = pool.borrowConnection();

        assertThat(second.isValid(1)).isTrue();
        second.close();
    }

    @Test
    void borrowBeyondMaxSizeThrowsPoolExhausted() {
        pool = new ConnectionPool("jdbc:h2:mem:pool-exhausted;DB_CLOSE_DELAY=-1", "sa", "", 1, 1, 200);

        pool.borrowConnection();

        assertThatThrownBy(() -> pool.borrowConnection())
                .isInstanceOf(PoolExhaustedException.class);
    }
}
