package us.mytheria.blobtycoon.entity;

import me.anjoismysign.anjo.entities.Tuple2;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.mechanics.MechanicsData;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public interface MechanicsProcessorHolder {

    @NotNull
    static Tuple2<Map<String, MechanicsProcessor>, Map<String, MechanicsProcessor>> READ(ConfigurationSection section) {
        if (!section.isConfigurationSection("MechanicsProcessorHolder"))
            new Tuple2<>(new HashMap<>(), new HashMap<>());
        Map<String, MechanicsProcessor> consumption = new HashMap<>();
        Map<String, MechanicsProcessor> production = new HashMap<>();
        ConfigurationSection mechanicsProcessorHolderSection = section.getConfigurationSection("MechanicsProcessorHolder");
        if (mechanicsProcessorHolderSection == null)
            return new Tuple2<>(new HashMap<>(), new HashMap<>());
        ConfigurationSection consumptionSection = mechanicsProcessorHolderSection.getConfigurationSection("Consumption");
        ConfigurationSection productionSection = mechanicsProcessorHolderSection.getConfigurationSection("Production");
        if (consumptionSection != null)
            consumptionSection.getKeys(false)
                    .forEach(mechanics -> {
                        ConfigurationSection mechanicsProcessorSection = consumptionSection.getConfigurationSection(mechanics);
                        MechanicsProcessor processor = MechanicsProcessor.READ(mechanicsProcessorSection, mechanics);
                        if (processor != null)
                            consumption.put(mechanics, processor);
                    });
        if (productionSection != null)
            productionSection.getKeys(false)
                    .forEach(mechanics -> {
                        ConfigurationSection mechanicsProcessorSection = productionSection.getConfigurationSection(mechanics);
                        MechanicsProcessor processor = MechanicsProcessor.READ(mechanicsProcessorSection, mechanics);
                        if (processor != null)
                            production.put(mechanics, processor);
                    });
        return new Tuple2<>(consumption, production);
    }

    @NotNull
    Map<String, MechanicsProcessor> getConsumption();

    @NotNull
    Map<String, MechanicsProcessor> getProduction();

    default void serializeMechanicsProcessors(ConfigurationSection section) {
        ConfigurationSection mechanicsProcessorHolderSection = section.createSection("MechanicsProcessorHolder");
        ConfigurationSection consumptionSection = mechanicsProcessorHolderSection.createSection("Consumption");
        ConfigurationSection productionSection = mechanicsProcessorHolderSection.createSection("Production");
        getConsumption().forEach((key, processor) -> {
            ConfigurationSection mechanicsProcessorSection = consumptionSection.createSection(key);
            processor.serialize(mechanicsProcessorSection);
        });
        getProduction().forEach((key, processor) -> {
            ConfigurationSection mechanicsProcessorSection = productionSection.createSection(key);
            processor.serialize(mechanicsProcessorSection);
        });
    }

    static void subtract(@NotNull Map<String, MechanicsProcessor> consumption,
                         @NotNull Map<String, MechanicsProcessor> production,
                         @NotNull PlotProfile plotProfile,
                         @NotNull File file) {
        consumption.forEach((reference, processor) -> {
            MechanicsData mechanicsData = BlobTycoonInternalAPI
                    .getInstance().getMechanicsData(reference);
            if (mechanicsData == null) {
                Bukkit.getPluginManager().getPlugin("BlobTycoon").getLogger()
                        .severe("'" + file.getPath() + "' has invalid consumption mechanics (" + reference + ")");
                return;
            }
            plotProfile.subtractConsumption(mechanicsData, processor.getAmount());
        });
        production.forEach((reference, processor) -> {
            MechanicsData mechanicsData = BlobTycoonInternalAPI
                    .getInstance().getMechanicsData(reference);
            if (mechanicsData == null) {
                Bukkit.getPluginManager().getPlugin("BlobTycoon").getLogger()
                        .severe("'" + file.getPath() + "' has invalid production mechanics (" + reference + ")");
                return;
            }
            plotProfile.subtractProduction(mechanicsData, processor.getAmount());
        });
    }

    static void add(@NotNull Map<String, MechanicsProcessor> consumption,
                    @NotNull Map<String, MechanicsProcessor> production,
                    @NotNull PlotProfile plotProfile,
                    @NotNull File file) {
        consumption.forEach((reference, processor) -> {
            MechanicsData mechanicsData = BlobTycoonInternalAPI
                    .getInstance().getMechanicsData(reference);
            if (mechanicsData == null) {
                Bukkit.getPluginManager().getPlugin("BlobTycoon").getLogger()
                        .severe("'" + file.getPath() + "' has invalid consumption mechanics (" + reference + ")");
                return;
            }
            plotProfile.addConsumption(mechanicsData, processor.getAmount());
        });
        production.forEach((reference, processor) -> {
            MechanicsData mechanicsData = BlobTycoonInternalAPI
                    .getInstance().getMechanicsData(reference);
            if (mechanicsData == null) {
                Bukkit.getPluginManager().getPlugin("BlobTycoon").getLogger()
                        .severe("'" + file.getPath() + "' has invalid production mechanics (" + reference + ")");
                return;
            }
            plotProfile.addProduction(mechanicsData, processor.getAmount());
        });
    }
}
