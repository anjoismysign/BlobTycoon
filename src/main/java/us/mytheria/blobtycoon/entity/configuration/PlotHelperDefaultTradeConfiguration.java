package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

/**
 * @param getCurrency The currency to get
 * @param getAmount   The amount to get
 */
public record PlotHelperDefaultTradeConfiguration(
        @NotNull String getCurrency,
        double getAmount
) {

    public static PlotHelperDefaultTradeConfiguration READ(@NotNull ConfigurationSection section) {
        if (!section.isString("Currency"))
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Default-Trade.Currency' is not valid or set");
        if (!section.isDouble("Amount"))
            throw new ConfigurationFieldException("'PlotHelper.Merchant.Default-Trade.Amount' is not valid or set");
        String currency = section.getString("Currency");
        double amount = section.getDouble("Amount");
        return new PlotHelperDefaultTradeConfiguration(currency, amount);
    }
}
