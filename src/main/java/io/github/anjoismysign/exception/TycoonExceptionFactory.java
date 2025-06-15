package io.github.anjoismysign.exception;

import io.github.anjoismysign.exception.cardinaldirection.InvalidCardinalDirectionExceptionFactory;

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
