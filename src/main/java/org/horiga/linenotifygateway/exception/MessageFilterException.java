package org.horiga.linenotifygateway.exception;

public class MessageFilterException extends NotifyException {

    private static final long serialVersionUID = 884437489088536574L;

    public MessageFilterException(String condition, String payload) {
        super("This event is filtered by condition: " + condition + ", payload: " + payload);
    }
}
