package us.mytheria.blobtycoon.entity.asset;

import me.anjoismysign.anjo.entities.Tuple2;
import me.anjoismysign.anjo.entities.Uber;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.action.Action;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.utilities.TextColor;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.ActionHolder;
import us.mytheria.blobtycoon.entity.ActionHolderData;
import us.mytheria.blobtycoon.entity.MechanicsProcessor;
import us.mytheria.blobtycoon.entity.MechanicsProcessorHolder;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.PlotProprietorProfile;
import us.mytheria.blobtycoon.entity.ScalarEarnerHolder;
import us.mytheria.blobtycoon.entity.ScalarValuableEarner;
import us.mytheria.blobtycoon.entity.Sellable;
import us.mytheria.blobtycoon.entity.Taggable;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.mechanics.MechanicsData;
import us.mytheria.blobtycoon.entity.reader.TycoonModelHolderReader;
import us.mytheria.blobtycoon.entity.structure.PrimitiveAsset;
import us.mytheria.blobtycoon.entity.structure.StorageModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolderData;
import us.mytheria.blobtycoon.entity.writers.TycoonHolderWriter;
import us.mytheria.blobtycoon.util.TycoonUnit;

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
                        @NotNull int getCooldown,
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
        } catch ( Exception exception ) {
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
