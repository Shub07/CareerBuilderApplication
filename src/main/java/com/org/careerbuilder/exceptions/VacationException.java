package com.org.careerbuilder.exceptions;

/**
 * 🎫 Vacation-specific exception for vacation operations
 */
public class VacationException extends RuntimeException {
    private String errorCode;

    public VacationException(String message) {
        super(message);
        this.errorCode = "VACATION_ERROR";
    }

    public VacationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public VacationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "VACATION_ERROR";
    }

    public VacationException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

