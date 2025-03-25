package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.positionable.Positionable;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

import java.util.Objects;

public record ParticleConfiguration(
        @NotNull Particle getParticle,
        double getX,
        double getY,
        double getZ,
        int getCount,
        boolean useRandomOffset,
        double getOffsetX,
        double getOffsetY,
        double getOffsetZ,
        boolean useExtra,
        double getExtra
) implements Positionable {

    public void spawn(
            @NotNull Location location) {
        Objects.requireNonNull(location, "'location' cannot be null");
        if (!useRandomOffset) {
            location.getWorld()
                    .spawnParticle(
                            getParticle,
                            location.add(toVector()),
                            getCount);
            return;
        }
        if (!useExtra)
            location.getWorld()
                    .spawnParticle(
                            getParticle,
                            location.add(toVector()),
                            getCount,
                            getOffsetX,
                            getOffsetY,
                            getOffsetZ);
        else
            location.getWorld()
                    .spawnParticle(
                            getParticle,
                            location.add(toVector()),
                            getCount,
                            getOffsetX,
                            getOffsetY,
                            getOffsetZ,
                            getExtra);
    }

    @NotNull
    public static ParticleConfiguration READ(
            @NotNull ConfigurationSection parent
    ) {
        return READ(parent, null);
    }

    @NotNull
    public static ParticleConfiguration READ(
            @NotNull ConfigurationSection parent,
            @Nullable Particle replacement) {
        Objects.requireNonNull(parent, "'parent' cannot be null");
        if (!parent.isDouble("X"))
            throw new ConfigurationFieldException("'X' is not set or valid");
        if (!parent.isDouble("Y"))
            throw new ConfigurationFieldException("'Y' is not set or valid");
        if (!parent.isDouble("Z"))
            throw new ConfigurationFieldException("'Z' is not set or valid");
        if (!parent.isInt("Count"))
            throw new ConfigurationFieldException("'Count' is not set or valid");
        String readParticle = parent.getString("Particle");
        Particle particle = Registry.PARTICLE_TYPE.match(readParticle);
        if (particle == null && replacement == null)
            throw new ConfigurationFieldException("'Particle' is not a valid Particle: " + readParticle);
        double x = parent.getDouble("X");
        double y = parent.getDouble("Y");
        double z = parent.getDouble("Z");
        int count = parent.getInt("Count");
        boolean useRandomOffset = parent.getBoolean("Use-Random-Offset", false);
        double offsetX = parent.getDouble("Offset-X", 1);
        double offsetY = parent.getDouble("Offset-Y", 1);
        double offsetZ = parent.getDouble("Offset-Z", 1);
        boolean useExtra = parent.getBoolean("Use-Extra", false);
        double extra = parent.getDouble("Extra", 0);
        return new ParticleConfiguration(
                particle == null ? replacement : particle,
                x,
                y,
                z,
                count,
                useRandomOffset,
                offsetX,
                offsetY,
                offsetZ,
                useExtra,
                extra);
    }
}
