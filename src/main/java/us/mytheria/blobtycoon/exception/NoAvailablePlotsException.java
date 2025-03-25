package us.mytheria.blobtycoon.exception;

public class NoAvailablePlotsException extends TycoonException {
    public NoAvailablePlotsException() {
        super("No available plots.");
    }
}
