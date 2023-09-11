package com.vocabnote.app.exception;

public class HttpErrorStatusException extends RuntimeException {
    private final int statusCode;

    public HttpErrorStatusException(int statusCode) {
        super("HTTP error response: " + statusCode);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
