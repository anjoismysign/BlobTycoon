package io.github.anjoismysign.entity.asset;

import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.anjo.entities.Tuple2;
import io.github.anjoismysign.anjo.entities.Uber;
import io.github.anjoismysign.bloblib.action.Action;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.entity.ActionHolder;
import io.github.anjoismysign.entity.ActionHolderData;
import io.github.anjoismysign.entity.EarnerHolder;
import io.github.anjoismysign.entity.MechanicsProcessor;
import io.github.anjoismysign.entity.MechanicsProcessorHolder;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.ScalarEarnerHolder;
import io.github.anjoismysign.entity.ScalarValuableEarner;
import io.github.anjoismysign.entity.Sellable;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.ValuableEarner;
import io.github.anjoismysign.entity.mechanics.MechanicsData;
import io.github.anjoismysign.entity.reader.TycoonModelHolderReader;
import io.github.anjoismysign.entity.structure.PrimitiveAsset;
import io.github.anjoismysign.entity.structure.StructureModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;
import io.github.anjoismysign.entity.structure.TycoonModelHolderData;
import io.github.anjoismysign.entity.writers.TycoonHolderWriter;
import io.github.anjoismysign.util.TycoonUnit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;

public record StructureAsset(@NotNull String getKey,
                             @NotNull StructureModel getModel,
                             @NotNull Sellable getSellable,
                             @NotNull Map<String, MechanicsProcessor> getConsumption,
                             @NotNull Map<String, MechanicsProcessor> getProduction,
                             @NotNull Map<String, ValuableEarner> getValuableEarners,
                             @NotNull Map<String, ScalarValuableEarner> getScalarValuableEarners,
                             int getCooldown,
                             @NotNull Uber<Boolean> isOnCooldown,
                             boolean isActionHolderEnabled,
                             @NotNull List<Action<Entity>> getActions)
        implements TycoonModelHolder<StructureModel>,
        EarnerHolder,
        ActionHolder {

    public static StructureAsset fromFile(File file, TycoonManagerDirector director) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        ActionHolderData actionHolderData = ActionHolder.READ(config);
        int cooldown = actionHolderData.getCooldown();
        boolean isActionHolderEnabled = actionHolderData.isActionHolderEnabled();
        List<Action<Entity>> actions = actionHolderData.getActions();
        Map<String, ValuableEarner> valuableEarners = EarnerHolder.READ(config);
        Map<String, ScalarValuableEarner> scalarValuableEarners = ScalarEarnerHolder.READ(config);
        Tuple2<Map<String, MechanicsProcessor>, Map<String, MechanicsProcessor>> mechanicsMap = MechanicsProcessorHolder.READ(config);
        Map<String, MechanicsProcessor> consumption = mechanicsMap.first();
        Map<String, MechanicsProcessor> production = mechanicsMap.second();
        TycoonModelHolderData<StructureModel> data = TycoonModelHolderReader
                .STRUCTURE_MODEL(file,
                        director,
                        PrimitiveAsset.STRUCTURE.getType(),
                        key,
                        player -> {
                            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI
                                    .getInstance().getTycoonPlayer(player);
                            PlotProprietorProfile profile = tycoonPlayer.getProfile();
                            PlotProfile plotProfile = profile.getPlotProfile();
                            EarnerHolder.subtract(valuableEarners, plotProfile);
                            ScalarEarnerHolder.subtract(scalarValuableEarners, plotProfile);
                            MechanicsProcessorHolder.subtract(consumption, production, plotProfile, file);
                        },
                        player -> {
                            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI
                                    .getInstance().getTycoonPlayer(player);
                            PlotProprietorProfile profile = tycoonPlayer.getProfile();
                            PlotProfile plotProfile = profile.getPlotProfile();
                            MechanicsProcessorHolder.add(consumption, production, plotProfile, file);
                            EarnerHolder.add(valuableEarners, plotProfile);
                            ScalarEarnerHolder.add(scalarValuableEarners, plotProfile);
                        });
        StructureAsset structureEarner = new StructureAsset(key, data.getModel(), data.getSellable(),
                consumption, production, valuableEarners, scalarValuableEarners,
                cooldown, Uber.drive(false), isActionHolderEnabled, actions);
        director.getStructureAssetDirector().process(structureEarner);
        return structureEarner;
    }

    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        TycoonHolderWriter.WRITE(this, yamlConfiguration);
        TycoonModelHolder.super.serializeMechanicsProcessors(yamlConfiguration);
        EarnerHolder.super.serializeValuableEarners(yamlConfiguration);
        TycoonModelHolder.super.serializeScalarEarners(yamlConfiguration);
        try {
            yamlConfiguration.save(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return file;
    }

    public @Nullable ItemStack apply(TranslatableItem item) {
        String locale = item.locale();
        return item
                .modder()
                .matchReplace("%hashrate@%", key -> {
                    ValuableEarner valuableEarner = getValuableEarners.get(key);
                    if (valuableEarner == null)
                        return TextColor.PARSE("&cNot Valid!");
                    return TycoonUnit.THOUSANDS_SEPARATOR
                            .format(valuableEarner.getPerSecond());
                })
                .matchReplace("%@Consumption%", key -> {
                    MechanicsProcessor mechanicsProcessor = getConsumption.get(key);
                    if (mechanicsProcessor == null)
                        return TextColor.PARSE("&cNot Valid!");
                    double amount = mechanicsProcessor.getAmount();
                    MechanicsData mechanicsData = BlobTycoonInternalAPI.getInstance()
                            .getMechanicsData(key);
                    if (mechanicsData == null)
                        return TextColor.PARSE("&cNot Valid!");
                    return mechanicsData.display(amount, locale, "en_gb");
                })
                .matchReplace("%@Production%", key -> {
                    MechanicsProcessor mechanicsProcessor = getProduction.get(key);
                    if (mechanicsProcessor == null)
                        return TextColor.PARSE("&cNot Valid!");
                    double amount = mechanicsProcessor.getAmount();
                    MechanicsData mechanicsData = BlobTycoonInternalAPI.getInstance()
                            .getMechanicsData(key);
                    if (mechanicsData == null)
                        return TextColor.PARSE("&cNot Valid!");
                    return mechanicsData.display(amount, locale, "en_gb");
                })
                .matchReplace("%boosterAmount@%", key -> {
                    ScalarValuableEarner valuableEarner = getScalarValuableEarners.get(key);
                    if (valuableEarner == null)
                        return TextColor.PARSE("&cNot Valid!");
                    return TycoonUnit.THOUSANDS_SEPARATOR.format(valuableEarner.getAmount());
                })
                .get().get();
    }
}
