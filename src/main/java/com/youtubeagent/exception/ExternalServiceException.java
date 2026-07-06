package com.youtubeagent.exception;

public class ExternalServiceException extends RuntimeException {

    private final String service;

    public ExternalServiceException(String service, String message) {
        super("External service '" + service + "' error: " + message);
        this.service = service;
    }

    public ExternalServiceException(String service, Throwable cause) {
        super("External service '" + service + "' error: " + cause.getMessage(), cause);
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
