package us.mytheria.blobtycoon.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

import java.util.Objects;

/**
 * Represents a valuable earner. Earns an amount
 * of a valuable every cooldown (in seconds).
 * Cooldown is expected to be between 1 and 15.
 *
 * @param getKey      the currency to earn
 * @param getAmount   the amount to earn
 * @param getCooldown the cooldown in seconds
 */
public record ValuableEarner(@NotNull String getKey,
                             double getAmount,
                             int getCooldown) {
    /**
     * Attempts reading a valuable earner from a ConfigurationSection.
     *
     * @param section  the section to read from
     * @param currency the currency to earn
     * @return the earner. null if cooldown is greater than 15 or if currency is null
     */
    @Nullable
    public static ValuableEarner READ(@NotNull ConfigurationSection section, @NotNull String currency) {
        Objects.requireNonNull(section);
        if (!section.isDouble("Amount"))
            throw new ConfigurationFieldException("'Amount' is not set or valid");
        double amount = section.getDouble("Amount");
        int cooldown = section.getInt("Cooldown", 1);
        return READ(currency, amount, cooldown);
    }

    /**
     * Creates an earner.
     *
     * @param amount   the amount to earn
     * @param cooldown the cooldown in seconds
     * @return the earner. null if cooldown is greater than 15 or if currency is null
     */
    @Nullable
    public static ValuableEarner READ(@NotNull String currency, double amount, int cooldown) {
        Objects.requireNonNull(currency);
        if (cooldown > 15 || cooldown < 1)
            return null;
        return new ValuableEarner(currency, amount, cooldown);
    }

    public void serialize(ConfigurationSection section) {
        section.set("Currency", getKey);
        section.set("Amount", getAmount);
        section.set("Cooldown", getCooldown);
    }

    public double getPerSecond() {
        return getAmount / getCooldown;
    }

}
