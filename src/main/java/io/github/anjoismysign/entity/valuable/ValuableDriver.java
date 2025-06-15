package io.github.anjoismysign.entity.valuable;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A ValuableDriver is a class that handles transactions of a valuable
 * between a player and BlobTycoon.
 */
public interface ValuableDriver {
    /**
     * If this driver supports deposit transactions.
     *
     * @return true if this driver supports deposits
     */
    boolean supportsDeposits();

    /**
     * If this driver supports withdraw transactions.
     *
     * @return true if this driver supports withdraws
     */
    boolean supportsWithdraws();

    /**
     * In this transaction, the player deposits an amount of the valuable.
     *
     * @param player   the player that's depositing
     * @param currency the currency to deposit
     * @param amount   the amount to deposit
     * @return true if the transaction was successful
     */
    boolean deposit(@NotNull Player player, @NotNull String currency, double amount);

    /**
     * In this transaction, the player withdraws an amount of the valuable.
     *
     * @param player   the player that's withdrawing
     * @param currency the currency to withdraw
     * @param amount   the amount to withdraw
     * @return true if the transaction was successful
     */
    boolean withdraw(@NotNull Player player, @NotNull String currency, double amount);

    /**
     * Will display a specific currency using this ValuableDriver
     *
     * @param player   The player that's requesting to display
     * @param currency The currency being displayed
     * @return The display
     */
    ItemStack display(@NotNull Player player, @NotNull String currency);
}
