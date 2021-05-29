package com.javi.uned.pfgcomposer.exceptions;

public class MusescoreException extends Exception {

    public MusescoreException() {
    }

    public MusescoreException(String message) {
        super(message);
    }

    public MusescoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public MusescoreException(Throwable cause) {
        super(cause);
    }
}
