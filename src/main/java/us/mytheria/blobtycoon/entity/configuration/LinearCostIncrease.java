package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

public record LinearCostIncrease(double getBaseCost,
                                 @NotNull String getCostCurrency,
                                 double getIncreasePerLevel)
        implements CostIncreaseConfiguration {

    public static LinearCostIncrease READ(ConfigurationSection section) {
        if (!section.isDouble("Base-Cost"))
            throw new ConfigurationFieldException("'Base-Cost' is not valid or set");
        double baseCost = section.getDouble("Base-Cost");
        String costCurrency = section.getString("Cost-Currency", "default");
        if (!section.isDouble("Increase-Per-Level"))
            throw new ConfigurationFieldException("'Increase-Per-Level' is not valid or set");
        double increasePerLevel = section.getDouble("Increase-Per-Level");
        return new LinearCostIncrease(baseCost, costCurrency, increasePerLevel);
    }

    @Override
    public double getCost(int level) {
        return getBaseCost + (getIncreasePerLevel * level);
    }
}
