package com.drivemaster.drivemastermain.dao.pool;

public class PoolExhaustedException extends RuntimeException {
    public PoolExhaustedException(String message) {
        super(message);
    }

    public PoolExhaustedException(String message, Throwable cause) {
        super(message, cause);
    }
}
