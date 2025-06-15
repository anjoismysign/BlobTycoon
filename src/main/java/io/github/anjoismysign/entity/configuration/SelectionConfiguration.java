package io.github.anjoismysign.entity.configuration;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public record SelectionConfiguration(@NotNull Particle getRemoveParticle,
                                     long getRemoveDuration,
                                     double getRemoveDistance,
                                     @NotNull Particle getAllowedParticle,
                                     double getAllowedDistance,
                                     @NotNull Particle getDeniedParticle,
                                     double getDeniedDistance,
                                     @NotNull Particle getSelectorParticle,
                                     double getSelectorDistance) {
    @NotNull
    public static SelectionConfiguration getInstance() {
        return BlobTycoonConfiguration.getInstance().getSelectionConfiguration();
    }

    public static SelectionConfiguration of(@NotNull ConfigurationSection section) {
        ConfigurationSection selectionSection = section.getConfigurationSection("Selection");
        ConfigurationSection removeSection = selectionSection.getConfigurationSection("Remove");
        Particle removeParticle = deserializeParticle(removeSection.getString("Particle"));
        long removeDuration = removeSection.getLong("Duration", 30);
        double removeDistance = removeSection.getDouble("Distance", 0.5);
        ConfigurationSection allowedSection = selectionSection.getConfigurationSection("Allowed");
        Particle allowedParticle = deserializeParticle(allowedSection.getString("Particle"));
        double allowedDistance = allowedSection.getDouble("Distance", 0.5);
        ConfigurationSection deniedSection = selectionSection.getConfigurationSection("Denied");
        Particle deniedParticle = deserializeParticle(deniedSection.getString("Particle"));
        double deniedDistance = deniedSection.getDouble("Distance", 0.5);
        ConfigurationSection selectorSection = selectionSection.getConfigurationSection("Selector");
        Particle selectorParticle = deserializeParticle(selectorSection.getString("Particle"));
        double selectorDistance = selectorSection.getDouble("Distance", 0.5);
        return new SelectionConfiguration(removeParticle, removeDuration, removeDistance,
                allowedParticle, allowedDistance, deniedParticle, deniedDistance, selectorParticle, selectorDistance);
    }

    private static Particle deserializeParticle(String particle) {
        return Particle.valueOf(particle.toUpperCase());
    }
}
