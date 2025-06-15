package io.github.anjoismysign.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

import java.util.Objects;

/**
 * Represents a scalar valuable earner. Adds the amount
 * to the scalar valuable earners.
 *
 * @param getCurrency the currency to earn
 * @param getAmount   the amount to earn
 */
public record ScalarValuableEarner(@NotNull String getCurrency,
                                   double getAmount) {
    /**
     * Attempts reading a valuable earner from a ConfigurationSection.
     *
     * @param section  the section to read from
     * @param currency the currency to earn
     * @return the earner. null if cooldown is greater than 15 or if currency is null
     */
    @Nullable
    public static ScalarValuableEarner READ(@NotNull ConfigurationSection section, @NotNull String currency) {
        Objects.requireNonNull(section);
        if (!section.isDouble("Amount"))
            throw new ConfigurationFieldException("'Amount' is not set or valid");
        double amount = section.getDouble("Amount") / 100;
        return READ(currency, amount);
    }

    /**
     * Creates an earner.
     *
     * @param amount the amount to earn
     * @return the earner. null if cooldown is greater than 15 or if currency is null
     */
    @Nullable
    public static ScalarValuableEarner READ(@NotNull String currency, double amount) {
        Objects.requireNonNull(currency);
        return new ScalarValuableEarner(currency, amount);
    }

    public void serialize(ConfigurationSection section) {
        section.set("Currency", getCurrency);
        section.set("Amount", getAmount);
    }

}
