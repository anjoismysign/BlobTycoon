package us.mytheria.blobtycoon.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

import java.util.Objects;

/**
 * Represents a Mechanics processor.
 *
 * @param getMechanics the mechanics to process
 * @param getAmount    the amount to process
 */
public record MechanicsProcessor(@NotNull String getMechanics,
                                 double getAmount) {
    /**
     * Attempts reading a mechanics processor from a ConfigurationSection.
     *
     * @param section   the section to read from
     * @param mechanics the mechanics to process
     * @return the processor. null if mechanics is null
     */
    @Nullable
    public static MechanicsProcessor READ(@NotNull ConfigurationSection section,
                                          @NotNull String mechanics) {
        Objects.requireNonNull(section);
        if (!section.isDouble("Amount"))
            throw new ConfigurationFieldException("'Amount' is not set or valid");
        double amount = section.getDouble("Amount");
        return READ(mechanics, amount);
    }

    /**
     * Creates a processor.
     *
     * @param amount the amount to process
     * @return the processor. null if mechanics is null
     */
    @Nullable
    public static MechanicsProcessor READ(@NotNull String mechanics,
                                          double amount) {
        Objects.requireNonNull(mechanics);
        return new MechanicsProcessor(mechanics, amount);
    }

    public void serialize(ConfigurationSection section) {
        section.set("Mechanics", getMechanics);
        section.set("Amount", getAmount);
    }

}
