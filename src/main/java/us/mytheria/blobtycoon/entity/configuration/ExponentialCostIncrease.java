package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

public record ExponentialCostIncrease(double getBaseCost,
                                      @NotNull String getCostCurrency,
                                      double getMultiplier)
        implements CostIncreaseConfiguration {

    @NotNull
    public static ExponentialCostIncrease READ(@NotNull ConfigurationSection section) {
        if (!section.isDouble("Base-Cost"))
            throw new ConfigurationFieldException("'Base-Cost' is not valid or set");
        double baseCost = section.getDouble("Base-Cost");
        String baseCostCurrency = section.getString("Base-Cost-Currency");
        if (baseCostCurrency != null) {
            section.set("Cost-Currency", baseCostCurrency);
            section.set("Base-Cost-Currency", null);
        }
        String costCurrency = section.getString("Cost-Currency", "default");
        if (!section.isDouble("Multiplier"))
            throw new ConfigurationFieldException("'Multiplier' is not valid or set");
        double multiplier = section.getDouble("Multiplier");
        multiplier += 1.0;
        return new ExponentialCostIncrease(baseCost, costCurrency, multiplier);
    }

    @Override
    public double getCost(int level) {
        return getBaseCost * Math.pow(getMultiplier, level);
    }
}
