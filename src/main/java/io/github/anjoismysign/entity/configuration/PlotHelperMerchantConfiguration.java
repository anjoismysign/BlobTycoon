package io.github.anjoismysign.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

/**
 * @param isEnabled                   Whether the merchant ability is enabled.
 * @param enableCommunityTrades       Whether to enable community trading whenever interacting with other plot's helpers
 * @param getDefaultMaximumTrades     The default maximum allowed trades at the same time the merchant can have.
 * @param getTradeSearchConfiguration The search configuration
 */
public record PlotHelperMerchantConfiguration(
        boolean isEnabled,
        boolean enableCommunityTrades,
        int getDefaultMaximumTrades,
        @NotNull PlotHelperTradeSearchConfiguration getTradeSearchConfiguration,
        @NotNull PlotHelperDefaultTradeConfiguration getDefaultTradeConfiguration) {

    public static PlotHelperMerchantConfiguration READ(@NotNull ConfigurationSection section) {
        boolean isEnabled = section.getBoolean("Enabled", true);
        if (!section.isBoolean("Enable-Community-Trades"))
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Enable-Community-Trades' is not set or valid");
        if (!section.isInt("Default-Maximum-Trades"))
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Default-Maximum-Trades' is not set or valid");
        boolean enableCommunityTrades = section.getBoolean("Enable-Community-Trades");
        int defaultMaximumTrades = section.getInt("Default-Maximum-Trades");
        ConfigurationSection tradeSearchSection = section.getConfigurationSection("Trade-Search");
        if (tradeSearchSection == null)
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Trade-Search' is not set or valid");
        PlotHelperTradeSearchConfiguration tradeSearchConfiguration = PlotHelperTradeSearchConfiguration.READ(tradeSearchSection);
        ConfigurationSection defaultTradeSection = section.getConfigurationSection("Default-Trade");
        if (defaultTradeSection == null)
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Default-Trade' is not set or valid");
        PlotHelperDefaultTradeConfiguration defaultTradeConfiguration = PlotHelperDefaultTradeConfiguration.READ(defaultTradeSection);
        return new PlotHelperMerchantConfiguration(
                isEnabled,
                enableCommunityTrades,
                defaultMaximumTrades,
                tradeSearchConfiguration,
                defaultTradeConfiguration
        );
    }
}
