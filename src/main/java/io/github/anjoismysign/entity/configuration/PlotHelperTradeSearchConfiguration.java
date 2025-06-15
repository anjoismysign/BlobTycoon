package io.github.anjoismysign.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

/**
 * @param isEnabled                           Whether the search ability is enabled.
 * @param autoTeleportOnSuccessfulTransaction Whether the player is teleported to target's plot on successful transaction.
 * @param notifyPlotOwners                    Whether the plot owners are notified when transaction is successful.
 */
public record PlotHelperTradeSearchConfiguration(
        boolean isEnabled,
        boolean autoTeleportOnSuccessfulTransaction,
        boolean notifyPlotOwners
) {

    public static PlotHelperTradeSearchConfiguration READ(@NotNull ConfigurationSection section) {
        boolean isEnabled = section.getBoolean("Enabled", true);
        if (!section.isBoolean("Auto-Teleport-On-Successful-Transaction"))
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Trade-Search.Auto-Teleport-On-Successful-Transaction' is not set or valid");
        boolean autoTeleportOnSuccessfulTransaction = section.getBoolean("Auto-Teleport-On-Successful-Transaction");
        if (!section.isBoolean("Notify-Plot-Owners"))
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Trade-Search.Notify-Plot-Owners' is not set or valid");
        boolean notifyPlotOwners = section.getBoolean("Notify-Plot-Owners");
        return new PlotHelperTradeSearchConfiguration(isEnabled, autoTeleportOnSuccessfulTransaction, notifyPlotOwners);
    }
}
