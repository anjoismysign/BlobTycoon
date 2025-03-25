package us.mytheria.blobtycoon.exception;

public abstract class TycoonException extends RuntimeException {
    public TycoonException(String message) {
        super(message);
    }
}
