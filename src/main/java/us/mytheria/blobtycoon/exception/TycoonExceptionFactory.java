package us.mytheria.blobtycoon.exception;

import us.mytheria.blobtycoon.exception.cardinaldirection.InvalidCardinalDirectionExceptionFactory;

public class TycoonExceptionFactory {
    private static TycoonExceptionFactory instance;
    private final InvalidCardinalDirectionExceptionFactory invalidCardinalDirectionExceptionFactory;

    public static TycoonExceptionFactory getInstance() {
        if (instance == null) {
            instance = new TycoonExceptionFactory();
        }
        return instance;
    }

    public TycoonExceptionFactory() {
        invalidCardinalDirectionExceptionFactory = new InvalidCardinalDirectionExceptionFactory();
    }

    public InvalidCardinalDirectionExceptionFactory getInvalidCardinalDirectionExceptionFactory() {
        return invalidCardinalDirectionExceptionFactory;
    }
}
