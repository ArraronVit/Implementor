package com.company;

public class ImplerException extends Exception {
    public ImplerException() {
    }

    public ImplerException(final String message) {
        super(message);
    }

    public ImplerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ImplerException(final Throwable cause) {
        super(cause);
    }
}
