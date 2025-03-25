package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.positionable.Spatial;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.StructureDirection;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;

import java.util.Objects;

public record PlotHelperConfiguration(
        boolean isEnabled,
        double getX,
        double getY,
        double getZ,
        float getYaw,
        float getPitch,
        @NotNull EntityType getEntityType,
        @NotNull PlotHelperStorageConfiguration getStorageConfiguration,
        @NotNull PlotHelperMerchantConfiguration getMerchantConfiguration
) implements Spatial {

    @NotNull
    public static PlotHelperConfiguration getInstance() {
        return BlobTycoonConfiguration.getInstance().getPlotHelperConfiguration();
    }

    public static PlotHelperConfiguration READ(@NotNull ConfigurationSection parent) {
        ConfigurationSection plotHelperSection = parent.getConfigurationSection("PlotHelper");
        if (plotHelperSection == null)
            throw new ConfigurationFieldException("'PlotHelper' is not set or valid");
        boolean isEnabled = plotHelperSection.getBoolean("Enabled", true);
        if (!plotHelperSection.isDouble("X"))
            throw new ConfigurationFieldException("'PlotHelper.X' is not set or valid");
        if (!plotHelperSection.isDouble("Y"))
            throw new ConfigurationFieldException("'PlotHelper.Y' is not set or valid");
        if (!plotHelperSection.isDouble("Z"))
            throw new ConfigurationFieldException("'PlotHelper.Z' is not set or valid");
        float yaw = (float) plotHelperSection.getDouble("Yaw", 0.0);
        yaw = Location.normalizeYaw(yaw);
        float pitch = (float) plotHelperSection.getDouble("Pitch", 0.0);
        pitch = Location.normalizePitch(pitch);
        String readEntityType = plotHelperSection.getString("EntityType", "ALLAY");
        EntityType entityType = Registry.ENTITY_TYPE.match(readEntityType);
        if (entityType == null)
            throw new ConfigurationFieldException("'PlotHelper.EntityType' does not match any entity type: " + readEntityType);
        if (!entityType.isSpawnable())
            throw new ConfigurationFieldException("'PlotHelper.EntityType' is not spawnable: " + entityType);
        ConfigurationSection storageSection = plotHelperSection.getConfigurationSection("Storage");
        if (storageSection == null)
            throw new ConfigurationFieldException("'PlotHelper.Storage' is not set or valid");
        ConfigurationSection merchantSection = plotHelperSection.getConfigurationSection("Merchant");
        if (merchantSection == null)
            throw new ConfigurationFieldException("'PlotHelper.Merchant' is not set or valid");
        PlotHelperStorageConfiguration storageConfiguration = PlotHelperStorageConfiguration.READ(storageSection);
        PlotHelperMerchantConfiguration merchantConfiguration = PlotHelperMerchantConfiguration.READ(merchantSection);
        double x = plotHelperSection.getDouble("X");
        double y = plotHelperSection.getDouble("Y");
        double z = plotHelperSection.getDouble("Z");
        return new PlotHelperConfiguration(isEnabled, x, y, z, yaw, pitch, entityType, storageConfiguration, merchantConfiguration);
    }

    /**
     * Spawns a plot helper entity at the specified location.
     *
     * @param plotProfile The plot profile
     * @return The spawned entity
     */
    public Entity spawnPlotHelper(@NotNull PlotProfile plotProfile) {
        Objects.requireNonNull(plotProfile, "'plotProfile' cannot be null");
        Vector vector = toVector();
        PlotData plotData = plotProfile.getPlot().getData();
        Location location = plotData.fromOffset(vector);
        StructureDirection direction = plotData.getDirection();
        location.setYaw(location.getYaw() + getYaw);
        location.setPitch(getPitch);

        Entity entity = location.getWorld().spawnEntity(location, getEntityType);
        entity.setPersistent(false);
        entity.setSilent(true);
        if (entity instanceof LivingEntity livingEntity)
            livingEntity.setAI(false);
        return entity;
    }
}
