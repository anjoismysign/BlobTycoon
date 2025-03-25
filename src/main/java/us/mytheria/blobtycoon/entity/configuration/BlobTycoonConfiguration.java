package us.mytheria.blobtycoon.entity.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.director.manager.ConfigManager;

public class BlobTycoonConfiguration {
    private static BlobTycoonConfiguration instance;

    private SelectionConfiguration selectionConfiguration;
    private RebirthConfiguration rebirthConfiguration;
    private HologramConfiguration hologramConfiguration;
    private PlotHelperConfiguration plotHelperConfiguration;
    private PlotPlacingConfiguration plotPlacingConfiguration;

    private final ConfigManager configManager;

    private BlobTycoonConfiguration(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @NotNull
    public static BlobTycoonConfiguration getInstance(@Nullable ConfigManager configManager) {
        if (instance == null) {
            if (configManager == null)
                throw new NullPointerException("injected dependency is null");
            instance = new BlobTycoonConfiguration(configManager);
        }
        return instance;
    }

    @NotNull
    public static BlobTycoonConfiguration getInstance() {
        return getInstance(null);
    }

    public void reload(@NotNull ConfigurationSection settingsSection) {
        plotPlacingConfiguration = PlotPlacingConfiguration.READ(settingsSection);
        rebirthConfiguration = RebirthConfiguration.READ(settingsSection);
        if (hologramConfiguration != null)
            configManager.getManagerDirector().getPlotManager().getPlots().forEach(plot -> hologramConfiguration.deleteHologram(plot.getData()));
        hologramConfiguration = HologramConfiguration.READ(settingsSection);
        plotHelperConfiguration = PlotHelperConfiguration.READ(settingsSection);
        selectionConfiguration = SelectionConfiguration.of(settingsSection);
    }

    @NotNull
    public HologramConfiguration getHologramConfiguration() {
        return hologramConfiguration;
    }

    @NotNull
    public PlotHelperConfiguration getPlotHelperConfiguration() {
        return plotHelperConfiguration;
    }

    @NotNull
    public SelectionConfiguration getSelectionConfiguration() {
        return selectionConfiguration;
    }

    @NotNull
    public RebirthConfiguration getRebirthConfiguration() {
        return rebirthConfiguration;
    }

    @NotNull
    public PlotPlacingConfiguration getPlotPlacingConfiguration() {
        return plotPlacingConfiguration;
    }
}
