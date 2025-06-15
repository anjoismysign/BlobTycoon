package io.github.anjoismysign.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

import java.util.Locale;
import java.util.Objects;

public record RebirthConfiguration(double getEarningMultiplier,
                                   @NotNull CostIncreaseConfiguration getCostIncreaseConfiguration) {

    @NotNull
    public static RebirthConfiguration getInstance() {
        return BlobTycoonConfiguration.getInstance().getRebirthConfiguration();
    }

    @NotNull
    public static RebirthConfiguration READ(@NotNull ConfigurationSection settingsSection) {
        ConfigurationSection section = settingsSection.getConfigurationSection("Rebirth");
        double earningMultiplier = section.getDouble("Earning-Multiplier", 1.0);
        ConfigurationSection costIncreaseSection = section.getConfigurationSection("Cost-Increase");
        Objects.requireNonNull(costIncreaseSection, "'Cost-Increase' section is required for RebirthConfiguration");
        String type = costIncreaseSection.getString("Type");
        Objects.requireNonNull(type, "'Type' is required for Cost-Increase section");
        CostIncrease costIncrease = CostIncrease.byName(type.toUpperCase(Locale.ROOT));
        if (costIncrease == null)
            throw new ConfigurationFieldException("Invalid Cost-Increase type: " + type);
        ConfigurationSection configurationSection = costIncreaseSection.getConfigurationSection("Configuration");
        if (configurationSection == null)
            throw new ConfigurationFieldException("'Configuration' section is required for 'Cost-Increase' section");
        CostIncreaseConfiguration costIncreaseConfiguration = switch (costIncrease) {
            case ABSENT -> AbsentCostIncrease.getInstance();
            case CONSTANT -> ConstantCostIncrease.READ(configurationSection);
            case EXPONENTIAL -> ExponentialCostIncrease.READ(configurationSection);
            case LINEAR -> LinearCostIncrease.READ(configurationSection);
        };
        return new RebirthConfiguration(earningMultiplier, costIncreaseConfiguration);
    }
}
