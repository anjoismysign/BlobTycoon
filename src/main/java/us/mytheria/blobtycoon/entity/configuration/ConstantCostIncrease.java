package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

public record ConstantCostIncrease(double getBaseCost,
                                   @NotNull String getCostCurrency)
        implements CostIncreaseConfiguration {

    @NotNull
    public static ConstantCostIncrease READ(@NotNull ConfigurationSection section) {
        if (!section.isDouble("Base-Cost"))
            throw new ConfigurationFieldException("'Base-Cost' is not valid or set");
        double baseCost = section.getDouble("Base-Cost");
        String costCurrency = section.getString("Cost-Currency", "default");
        return new ConstantCostIncrease(baseCost, costCurrency);
    }

    @Override
    public double getCost(int level) {
        return getBaseCost;
    }
}
