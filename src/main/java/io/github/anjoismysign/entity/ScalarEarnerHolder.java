package io.github.anjoismysign.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface ScalarEarnerHolder {
    @NotNull
    static Map<String, ScalarValuableEarner> READ(ConfigurationSection section) {
        Map<String, ScalarValuableEarner> map = new HashMap<>();
        if (!section.isConfigurationSection("ScalarEarnerHolder"))
            new HashMap<>();
        ConfigurationSection scalarEarnerHolderSection = section.getConfigurationSection("ScalarEarnerHolder");
        if (scalarEarnerHolderSection == null)
            return new HashMap<>();
        scalarEarnerHolderSection.getKeys(false)
                .forEach(currency -> {
                    ConfigurationSection moneyEarnerSection = scalarEarnerHolderSection.getConfigurationSection(currency);
                    ScalarValuableEarner moneyEarner = ScalarValuableEarner.READ(moneyEarnerSection, currency);
                    if (moneyEarner != null)
                        map.put(currency, moneyEarner);
                });
        return map;
    }

    Map<String, ScalarValuableEarner> getScalarValuableEarners();

    default void serializeScalarEarners(ConfigurationSection section) {
        ConfigurationSection scalarEarnerHolderSection = section.createSection("ScalarEarnerHolder");
        getScalarValuableEarners().forEach((key, valuableEarner) -> {
            ConfigurationSection moneyEarnerSection = scalarEarnerHolderSection.createSection(key);
            valuableEarner.serialize(moneyEarnerSection);
        });
    }

    static void subtract(@NotNull Map<String, ScalarValuableEarner> scalarValuableEarners,
                         @NotNull PlotProfile plotProfile) {
        scalarValuableEarners.forEach((currency, scalarValuableEarner) -> plotProfile
                .subtractScalarEarner(currency, scalarValuableEarner.getAmount()));
    }

    static void add(@NotNull Map<String, ScalarValuableEarner> scalarValuableEarners,
                    @NotNull PlotProfile plotProfile) {
        scalarValuableEarners.forEach((currency, scalarValuableEarner) -> plotProfile
                .addScalarEarner(currency, scalarValuableEarner.getAmount()));
    }
}
