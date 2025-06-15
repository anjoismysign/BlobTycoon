package io.github.anjoismysign.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

/**
 * @param isEnabled         Whether the storage ability is enabled.
 * @param getDefaultStorage The amount inventories that are given by default to a new profile
 */
public record PlotHelperStorageConfiguration(
        boolean isEnabled,
        int getDefaultStorage) {

    public static PlotHelperStorageConfiguration READ(@NotNull ConfigurationSection section) {
        boolean isEnabled = section.getBoolean("Enabled", true);
        if (!section.isInt("Default-Storage"))
            throw new ConfigurationFieldException("'PlotHelper.Storage.Default-Storage' is not set or valid");
        int defaultStorage = section.getInt("Default-Storage");
        return new PlotHelperStorageConfiguration(isEnabled, defaultStorage);
    }
}
