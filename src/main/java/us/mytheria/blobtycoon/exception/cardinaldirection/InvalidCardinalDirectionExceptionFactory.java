package us.mytheria.blobtycoon.exception.cardinaldirection;

public class InvalidCardinalDirectionExceptionFactory {

    public InvalidCardinalDirectionException custom(String message) {
        return new InvalidCardinalDirectionException(message);
    }

    public InvalidCardinalDirectionException invalidDirection(String direction) {
        return new InvalidCardinalDirectionException("Invalid cardinal getDirection: " + direction);
    }

    public InvalidCardinalDirectionException notMain(String direction) {
        return new InvalidCardinalDirectionException("There are only four (4) main cardinal directions (North,South,East,West). " +
                "Unexpected getDirection at: " + direction);
    }
}
