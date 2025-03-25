package us.mytheria.blobtycoon.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface EarnerHolder {

    @NotNull
    static Map<String, ValuableEarner> READ(ConfigurationSection section) {
        Map<String, ValuableEarner> map = new HashMap<>();
        if (!section.isConfigurationSection("EarnerHolder"))
            return new HashMap<>();
        ConfigurationSection earnerHolderSection = section.getConfigurationSection("EarnerHolder");
        if (earnerHolderSection == null)
            return new HashMap<>();
        earnerHolderSection.getKeys(false)
                .forEach(currency -> {
                    ConfigurationSection moneyEarnerSection = earnerHolderSection.getConfigurationSection(currency);
                    ValuableEarner moneyEarner = ValuableEarner.READ(moneyEarnerSection, currency);
                    if (moneyEarner != null)
                        map.put(currency, moneyEarner);
                });
        return map;
    }

    Map<String, ValuableEarner> getValuableEarners();

    default void serializeValuableEarners(ConfigurationSection section) {
        ConfigurationSection earnerHolderSection = section.createSection("EarnerHolder");
        getValuableEarners().forEach((key, valuableEarner) -> {
            ConfigurationSection moneyEarnerSection = earnerHolderSection.createSection(key);
            valuableEarner.serialize(moneyEarnerSection);
        });
    }

    static void subtract(@NotNull Map<String, ValuableEarner> valuableEarners,
                         @NotNull PlotProfile plotProfile) {
        valuableEarners.forEach((reference, earner) -> plotProfile.subtractEarner(earner));
    }

    static void add(@NotNull Map<String, ValuableEarner> valuableEarners,
                    @NotNull PlotProfile plotProfile) {
        valuableEarners.forEach((reference, earner) -> plotProfile.addEarner(earner));
    }
}
