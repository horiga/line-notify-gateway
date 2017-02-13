package org.horiga.linenotifygateway.exception;

public class NotifyException extends Exception {

    public NotifyException(String message) {
        super(message);
    }

    public NotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
