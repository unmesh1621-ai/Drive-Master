package com.drivemaster.drivemastermain.exception;

public class DealerNotApprovedException extends RuntimeException {
    public DealerNotApprovedException(String message) {
        super(message);
    }
}
