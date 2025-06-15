package io.github.anjoismysign.exception;

public class NoAvailablePlotsException extends TycoonException {
    public NoAvailablePlotsException() {
        super("No available plots.");
    }
}
