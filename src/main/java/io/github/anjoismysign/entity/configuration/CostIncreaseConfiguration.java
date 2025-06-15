package io.github.anjoismysign.entity.configuration;

import org.jetbrains.annotations.NotNull;

public interface CostIncreaseConfiguration {
    /**
     * Whether this configuration is enabled.
     *
     * @return true if enabled, false otherwise
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * Get the cost for a given level.
     *
     * @param level the level
     * @return the cost
     */
    double getCost(int level);

    /**
     * Get the currency for the cost.
     *
     * @return the currency
     */
    @NotNull
    String getCostCurrency();
}
