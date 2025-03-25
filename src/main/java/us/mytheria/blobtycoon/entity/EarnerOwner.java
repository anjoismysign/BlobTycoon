package us.mytheria.blobtycoon.entity;

import us.mytheria.blobtycoon.util.TycoonUnit;

import java.util.Map;

public interface EarnerOwner {
    /**
     * Should not be called, use EarnerOwner methods instead
     *
     * @return The map of earners
     */
    Map<String, Double> getEarners();

    /**
     * Should not be called, use ScalarEarnerOwner methods instead
     *
     * @return The map of scalar earners
     */
    Map<String, Double> getScalarEarners();

    /**
     * Gets the transient earners that use ADD operation.
     *
     * @return the map of transient earners
     */
    Map<String, Double> getTransientEarners();

    /**
     * Gets the transient earners that use ADD_SCALAR operation.
     *
     * @return the map of transient earners
     */
    Map<String, Double> getTransientScalarEarners();

    /**
     * Gets the rebirth multiplier.
     *
     * @return the rebirth multiplier
     */
    double getRebirthMultiplier();

    /**
     * Gets a specific valuable earner.
     * This represents the amount of valuable earned per second.
     *
     * @param valuable the valuable
     * @return the earner
     */
    default double getEarner(String valuable) {
        return getEarners().getOrDefault(valuable, 0.0);
    }

    /**
     * Gets a specific scalar valuable earner.
     *
     * @param valuable the valuable
     * @return the earner
     */
    default double getScalarEarner(String valuable) {
        return getScalarEarners().getOrDefault(valuable, 0.0);
    }

    /**
     * Gets a specific transient valuable earner with ADD operation.
     *
     * @param valuable the valuable
     * @return the earner
     */
    default double getTransientEarner(String valuable) {
        return getTransientEarners().getOrDefault(valuable, 0.0);
    }

    /**
     * Gets a specific transient valuable earner with ADD_SCALAR operation.
     *
     * @param valuable the valuable
     * @return the earner
     */
    default double getTransientScalarEarner(String valuable) {
        return getTransientScalarEarners().getOrDefault(valuable, 0.0);
    }

    /**
     * Gets the total earnings of a valuable.
     *
     * @param valuable the valuable
     * @return the total earnings
     */
    default double getTotalEarnings(String valuable) {
        double scalar = getScalarEarner(valuable);
        scalar += getTransientScalarEarner(valuable);
        double earner = getEarner(valuable);
        earner += getTransientEarner(valuable);
        earner *= (scalar + 1);
        earner *= getRebirthMultiplier();
        return earner;
    }

    /**
     * Parses a specific valuable earner
     *
     * @param valuable the valuable
     * @return the parsed balance
     */
    default String parseEarner(String valuable) {
        return TycoonUnit.THOUSANDS_SEPARATOR.format(getTotalEarnings(valuable));
    }

    /**
     * Adds a specific amount to a valuable earners.
     *
     * @param valuable        the valuable
     * @param amountPerSecond the amount
     */
    default void addEarner(String valuable, double amountPerSecond) {
        double get = getEarners().getOrDefault(valuable, 0.0);
        getEarners().put(valuable, get + amountPerSecond);
    }

    /**
     * Adds a specific amount to a scalar valuable earners.
     *
     * @param valuable the valuable
     * @param amount   the amount
     */
    default void addScalarEarner(String valuable, double amount) {
        double get = getScalarEarners().getOrDefault(valuable, 0.0);
        getScalarEarners().put(valuable, get + amount);
    }

    /**
     * Adds a specific amount to a transient valuable earners with ADD operation.
     *
     * @param valuable        the valuable
     * @param amountPerSecond the amount
     */
    default void addTransientEarner(String valuable, double amountPerSecond) {
        double get = getTransientEarners().getOrDefault(valuable, 0.0);
        getTransientEarners().put(valuable, get + amountPerSecond);
    }

    /**
     * Adds a specific amount to a transient valuable earners with ADD_SCALAR operation.
     *
     * @param valuable        the valuable
     * @param amountPerSecond the amount
     */
    default void addTransientScalarEarner(String valuable, double amountPerSecond) {
        double get = getTransientScalarEarners().getOrDefault(valuable, 0.0);
        getTransientScalarEarners().put(valuable, get + amountPerSecond);
    }

    /**
     * Adds a ValuableEarner to the earners map.
     *
     * @param earner the earner
     */
    default void addEarner(ValuableEarner earner) {
        addEarner(earner.getKey(), earner.getPerSecond());
    }

    /**
     * Subtracts a specific amount from a valuable earners.
     *
     * @param valuable        the valuable
     * @param amountPerSecond the amount
     */
    default void subtractEarner(String valuable, double amountPerSecond) {
        double get = getEarners().getOrDefault(valuable, 0.0);
        getEarners().put(valuable, get - amountPerSecond);
    }

    /**
     * Subtracts a specific amount from a scalar valuable earners.
     *
     * @param valuable the valuable
     * @param amount   the amount
     */
    default void subtractScalarEarner(String valuable, double amount) {
        double get = getScalarEarners().getOrDefault(valuable, 0.0);
        getScalarEarners().put(valuable, get - amount);
    }

    /**
     * Subtracts a specific amount from a transient valuable earners with ADD operation.
     *
     * @param valuable        the valuable
     * @param amountPerSecond the amount
     */
    default void subtractTransientEarner(String valuable, double amountPerSecond) {
        double get = getTransientEarners().getOrDefault(valuable, 0.0);
        getTransientEarners().put(valuable, get - amountPerSecond);
    }

    /**
     * Subtracts a specific amount from a transient valuable earners with ADD_SCALAR operation.
     *
     * @param valuable        the valuable
     * @param amountPerSecond the amount
     */
    default void subtractTransientScalarEarner(String valuable, double amountPerSecond) {
        double get = getTransientScalarEarners().getOrDefault(valuable, 0.0);
        getTransientScalarEarners().put(valuable, get - amountPerSecond);
    }

    /**
     * Subtracts a ValuableEarner from the earners map.
     *
     * @param earner the earner
     */
    default void subtractEarner(ValuableEarner earner) {
        subtractEarner(earner.getKey(), earner.getPerSecond());
    }
}
