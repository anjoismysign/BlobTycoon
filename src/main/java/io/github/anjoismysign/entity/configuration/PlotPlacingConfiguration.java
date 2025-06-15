package io.github.anjoismysign.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;

public record PlotPlacingConfiguration(
        @NotNull ParticleConfiguration getBlockParticleConfiguration,
        @NotNull ParticleConfiguration getEntityParticleConfiguration
) {

    @NotNull
    public static PlotPlacingConfiguration getInstance() {
        return BlobTycoonConfiguration.getInstance().getPlotPlacingConfiguration();
    }

    @NotNull
    public static PlotPlacingConfiguration READ(
            @NotNull ConfigurationSection parent) {
        ConfigurationSection plotPlacingSection = parent.getConfigurationSection("PlotPlacing");
        if (plotPlacingSection == null)
            throw new ConfigurationFieldException("'PlotPlacing' is not set or valid");
        if (!plotPlacingSection.isConfigurationSection("Block-Particle-Configuration"))
            throw new ConfigurationFieldException("'PlotPlacing.Block-Particle-Configuration' is not set or valid");
        if (!plotPlacingSection.isConfigurationSection("Entity-Particle-Configuration"))
            throw new ConfigurationFieldException("'PlotPlacing.Entity-Particle-Configuration' is not set or valid");
        ConfigurationSection blockParticleSection = plotPlacingSection.getConfigurationSection("Block-Particle-Configuration");
        ConfigurationSection entityParticleSection = plotPlacingSection.getConfigurationSection("Entity-Particle-Configuration");
        ParticleConfiguration blockParticleConfiguration = ParticleConfiguration.READ(blockParticleSection);
        ParticleConfiguration entityParticleConfiguration = ParticleConfiguration.READ(entityParticleSection);
        return new PlotPlacingConfiguration(blockParticleConfiguration, entityParticleConfiguration);
    }
}
