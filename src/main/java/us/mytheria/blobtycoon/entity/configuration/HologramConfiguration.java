package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibHologramAPI;
import us.mytheria.bloblib.entities.positionable.Positionable;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.bloblib.hologram.FancyHolograms;
import us.mytheria.bloblib.hologram.HologramDriver;
import us.mytheria.bloblib.hologram.HologramDriverType;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record HologramConfiguration(
        @NotNull List<String> getLines,
        double getX,
        double getY,
        double getZ,
        boolean isEnabled
) implements Positionable {

    @NotNull
    public static HologramConfiguration getInstance() {
        return BlobTycoonConfiguration.getInstance().getHologramConfiguration();
    }

    public static HologramConfiguration READ(@NotNull ConfigurationSection settingsSection) {
        ConfigurationSection section = settingsSection.getConfigurationSection("Hologram");
        if (!section.isDouble("X"))
            throw new ConfigurationFieldException("'X' is not set or valid");
        if (!section.isDouble("Y"))
            throw new ConfigurationFieldException("'Y' is not set or valid");
        if (!section.isDouble("Z"))
            throw new ConfigurationFieldException("'Z' is not set or valid");
        boolean isEnabled = section.getBoolean("Enabled", true);
        List<String> lines = section.getStringList("Lines");
        return new HologramConfiguration(
                lines,
                section.getDouble("X"),
                section.getDouble("Y"),
                section.getDouble("Z"),
                isEnabled);
    }

    public void createHologram(@NotNull PlotProfile plotProfile) {
        Objects.requireNonNull(plotProfile, "'plotProfile' cannot be null");
        Vector vector = toVector();
        PlotData plotData = plotProfile.getPlot().getData();

        List<String> hologramLines = new ArrayList<>();
        getLines.forEach(line -> {
            if (line.contains("%owners%")) {
                String strip = line.replace("%owners%", "");
                hologramLines.add(strip);
                hologramLines.addAll(plotProfile.getOwners());
            } else {
                hologramLines.add(line);
            }
        });

        HologramDriver driver = BlobLibHologramAPI.getInstance().getHologramDriver();
        if (driver.getType() == HologramDriverType.FANCY_HOLOGRAMS) {
            FancyHolograms fancyHolograms = (FancyHolograms) driver;
            fancyHolograms.create(
                    plotData.getIndex(),
                    plotData.fromOffset(vector),
                    hologramLines,
                    false);
        } else {
            driver.create(
                    plotData.getIndex(),
                    plotData.fromOffset(vector),
                    hologramLines,
                    false);
        }
    }

    public void deleteHologram(@NotNull PlotData plotData) {
        BlobLibHologramAPI.getInstance().getHologramDriver().remove(plotData.getIndex());
    }
}
