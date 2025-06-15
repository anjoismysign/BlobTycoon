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
import io.github.anjoismysign.entity.MechanicsProcessor;
import io.github.anjoismysign.entity.MechanicsProcessorHolder;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.ScalarEarnerHolder;
import io.github.anjoismysign.entity.ScalarValuableEarner;
import io.github.anjoismysign.entity.Sellable;
import io.github.anjoismysign.entity.Taggable;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.mechanics.MechanicsData;
import io.github.anjoismysign.entity.reader.TycoonModelHolderReader;
import io.github.anjoismysign.entity.structure.PrimitiveAsset;
import io.github.anjoismysign.entity.structure.StorageModel;
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

public record RackAsset(@NotNull String getKey,
                        @NotNull StorageModel getModel,
                        @NotNull Sellable getSellable,
                        @NotNull Map<String, MechanicsProcessor> getConsumption,
                        @NotNull Map<String, MechanicsProcessor> getProduction,
                        @NotNull Map<String, ScalarValuableEarner> getScalarValuableEarners,
                        @NotNull String getTagSetKey,
                        int getCooldown,
                        @NotNull Uber<Boolean> isOnCooldown,
                        boolean isActionHolderEnabled,
                        @NotNull List<Action<Entity>> getActions)
        implements TycoonModelHolder<StorageModel>,
        Taggable,
        ActionHolder {

    public static RackAsset fromFile(File file, TycoonManagerDirector director) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        String tagSetKey = Taggable.read(config);
        ActionHolderData actionHolderData = ActionHolder.READ(config);
        int cooldown = actionHolderData.getCooldown();
        boolean isActionHolderEnabled = actionHolderData.isActionHolderEnabled();
        List<Action<Entity>> actions = actionHolderData.getActions();
        Map<String, ScalarValuableEarner> scalarValuableEarners = ScalarEarnerHolder.READ(config);
        Tuple2<Map<String, MechanicsProcessor>, Map<String, MechanicsProcessor>> mechanicsMap = MechanicsProcessorHolder.READ(config);
        Map<String, MechanicsProcessor> consumption = mechanicsMap.first();
        Map<String, MechanicsProcessor> production = mechanicsMap.second();
        TycoonModelHolderData<StorageModel> data = TycoonModelHolderReader
                .STORAGE_HOLDER_MODEL(file,
                        director,
                        PrimitiveAsset.RACK.getType(),
                        key,
                        player -> {
                            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI
                                    .getInstance().getTycoonPlayer(player);
                            PlotProprietorProfile profile = tycoonPlayer.getProfile();
                            PlotProfile plotProfile = profile.getPlotProfile();
                            ScalarEarnerHolder.subtract(scalarValuableEarners, plotProfile);
                            MechanicsProcessorHolder.subtract(consumption, production, plotProfile, file);
                        },
                        player -> {
                            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI
                                    .getInstance().getTycoonPlayer(player);
                            PlotProprietorProfile profile = tycoonPlayer.getProfile();
                            PlotProfile plotProfile = profile.getPlotProfile();
                            MechanicsProcessorHolder.add(consumption, production, plotProfile, file);
                            ScalarEarnerHolder.add(scalarValuableEarners, plotProfile);
                        });
        RackAsset rack = new RackAsset(key, data.getModel(), data.getSellable(),
                consumption, production, scalarValuableEarners, tagSetKey,
                cooldown, Uber.drive(false), isActionHolderEnabled, actions);
        director.getRackAssetDirector().process(rack);
        return rack;
    }

    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        TycoonHolderWriter.WRITE(this, yamlConfiguration);
        TycoonModelHolder.super.serializeMechanicsProcessors(yamlConfiguration);
        TycoonModelHolder.super.serializeScalarEarners(yamlConfiguration);
        Taggable.super.serializeTag(yamlConfiguration);
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
