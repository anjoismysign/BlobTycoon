package io.github.anjoismysign.blobtycoon.exception;

public abstract class TycoonException extends RuntimeException {
    public TycoonException(String message) {
        super(message);
    }
}
