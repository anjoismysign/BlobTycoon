package io.github.anjoismysign.entity.configuration;

import org.jetbrains.annotations.NotNull;

public class AbsentCostIncrease implements CostIncreaseConfiguration {
    private static AbsentCostIncrease instance;

    public static AbsentCostIncrease getInstance() {
        if (instance == null) {
            return new AbsentCostIncrease();
        }
        return instance;
    }

    private AbsentCostIncrease() {
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public double getCost(int level) {
        return Double.MAX_VALUE;
    }

    @Override
    public @NotNull String getCostCurrency() {
        return "default";
    }
}
