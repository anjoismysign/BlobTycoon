package io.github.anjoismysign.entity.mechanics;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SideChannelMechanics(@NotNull String getSource,
                                   double getThreshold) {

    @Nullable
    public static SideChannelMechanics READ(@NotNull ConfigurationSection section) {
        ConfigurationSection sideChannelSection = section.getConfigurationSection("Side-Channel");
        if (sideChannelSection == null)
            return null;
        String source = sideChannelSection.getString("Source");
        if (source == null)
            return null;
        double threshold = sideChannelSection.getDouble("Threshold", 1);
        return new SideChannelMechanics(source, threshold);
    }

    public void write(@NotNull ConfigurationSection section) {
        ConfigurationSection sideChannelSection = section.createSection("Side-Channel");
        sideChannelSection.set("Source", getSource);
        sideChannelSection.set("Threshold", getThreshold);
    }
}
