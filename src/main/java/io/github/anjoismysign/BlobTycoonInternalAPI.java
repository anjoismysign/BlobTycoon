package io.github.anjoismysign;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableBlock;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.itemstack.ItemStackModder;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.entity.DefaultStructuresInitializer;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.mechanics.MechanicsData;
import io.github.anjoismysign.entity.plothelper.PlotHelperContainer;
import io.github.anjoismysign.entity.plothelper.PlotHelperTrade;
import io.github.anjoismysign.entity.structure.ObjectModel;
import io.github.anjoismysign.entity.structure.PrimitiveAsset;
import io.github.anjoismysign.entity.structure.StructureModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class BlobTycoonInternalAPI {
    private static BlobTycoonInternalAPI instance;
    private final TycoonManagerDirector director;

    protected static BlobTycoonInternalAPI getInstance(TycoonManagerDirector director) {
        if (instance == null) {
            if (director == null)
                throw new NullPointerException("injected dependency is null");
            BlobTycoonInternalAPI.instance = new BlobTycoonInternalAPI(director);
        }
        return instance;
    }

    public static BlobTycoonInternalAPI getInstance() {
        return getInstance(null);
    }

    private BlobTycoonInternalAPI(TycoonManagerDirector director) {
        this.director = director;
    }

    public void initializeStructures(File structuresDirectory) {
        DefaultStructuresInitializer.load(director, structuresDirectory);
    }

    /**
     * Will get the empty material from the config.
     * It's used as the material that represents an empty
     * block, such as when removing a structure.
     *
     * @return The empty material from the config.
     */
    @NotNull
    public Material getEmptyMaterial() {
        return director.getConfigManager().getEmptyMaterial();
    }

    /**
     * If true, the player will be teleported to their plot on join.
     *
     * @return true if the player will be teleported to their plot on join
     */
    public boolean teleportToPlotOnJoin() {
        return director.getConfigManager().teleportToPlotOnJoin();
    }

    /**
     * Will return true if the material is a valid floor.
     *
     * @param material The material to check
     * @return true if the material is a valid floor
     */
    public boolean isValidFloor(Material material) {
        return director.getConfigManager().getValidFloor().contains(material);
    }

    /**
     * Will return MaxPlacedPerSecond amount, used for Structrador operations
     *
     * @return the amount
     */
    public int getMaxPlacedPerSecond() {
        return director.getConfigManager().getMaxPlacedPerSecond();
    }

    /**
     * Will check if the plot is done loading.
     *
     * @param plotIndex The plot getIndex
     * @return true if the plot is done loading
     */
    public boolean plotLoaderIsDone(String plotIndex) {
        return director.getPlotManager().getLoader(plotIndex) == null;
    }

    /**
     * Will get a random profile name from the config.
     *
     * @return A random profile name from the config.
     */
    public String getRandomProfileName(@NotNull List<String> noRepeat) {
        Objects.requireNonNull(noRepeat, "'noRepeat' cannot be null");
        List<String> names = director.getConfigManager().getProfileNames();
        String random = names.get((int) (Math.random() * names.size()));
        if (noRepeat.contains(random))
            return getRandomProfileName(noRepeat);
        return random;
    }

    /**
     * Gets a structure from the structure tracker.
     *
     * @param key The getKey of the structure
     * @return The structure
     */
    @Nullable
    public Structure getStructure(String key) {
        return director.getStructureTracker().getStructure(key);
    }

    @Nullable
    public TycoonPlayer getTycoonPlayer(UUID uuid) {
        return director.getTycoonPlayerManager().isBlobSerializable(uuid).orElse(null);
    }

    @Nullable
    public TycoonPlayer getTycoonPlayer(Player player) {
        return getTycoonPlayer(player.getUniqueId());
    }

    @NotNull
    public ItemStack getRemove(Player player) {
        return director.getConfigManager().getRemoveButton(player);
    }

    @NotNull
    public ItemStack getObjectSlot(Player player) {
        return director.getConfigManager().getObjectSlot(player);
    }

    @Nullable
    public <T extends StructureModel> TycoonModelHolder<T> getStructureModelHolder(PrimitiveAsset object, String key) {
        switch (object) {
            case STRUCTURE -> {
                return (TycoonModelHolder<T>) director.getStructureAssetDirector()
                        .getObjectManager().getObject(key);
            }
            case RACK -> {
                return (TycoonModelHolder<T>) director.getRackAssetDirector()
                        .getObjectManager().getObject(key);
            }
            default -> {
                return null;
            }
        }
    }

    @Nullable
    public <T extends ObjectModel> TycoonModelHolder<T> getObjectHolder(PrimitiveAsset object, String key) {
        if (Objects.requireNonNull(object) == PrimitiveAsset.OBJECT) {
            return (TycoonModelHolder<T>) director.getObjectAssetDirector()
                    .getObjectManager().getObject(key);
        }
        return null;
    }

    @NotNull
    public Map<PrimitiveAsset, ItemStack> hasDisplay(@NotNull TranslatableItem translatableItem) {
        Map<PrimitiveAsset, ItemStack> map = new HashMap<>();
        if (director.getRackAssetDirector() == null)
            return map;
        ItemStack rack = director.getRackAssetDirector().hasDisplay(translatableItem);
        if (rack != null)
            map.put(PrimitiveAsset.RACK, rack);
        if (director.getStructureAssetDirector() == null)
            return map;
        ItemStack structure = director.getStructureAssetDirector().hasDisplay(translatableItem);
        if (structure != null)
            map.put(PrimitiveAsset.STRUCTURE, structure);
        if (director.getObjectAssetDirector() == null)
            return map;
        ItemStack objectEarner = director.getObjectAssetDirector().hasDisplay(translatableItem);
        if (objectEarner != null)
            map.put(PrimitiveAsset.OBJECT, objectEarner);
        return map;
    }

    @NotNull
    public Map<PrimitiveAsset, TycoonModelHolder<?>> isLinked(@NotNull TranslatableItem translatableItem) {
        Map<PrimitiveAsset, TycoonModelHolder<?>> map = new HashMap<>();
        String rack = director.getRackAssetDirector().isLinked(translatableItem);
        if (rack != null)
            map.put(PrimitiveAsset.RACK, director.getRackAssetDirector().getObjectManager().getObject(rack));
        String structureEarner = director.getStructureAssetDirector().isLinked(translatableItem);
        if (structureEarner != null)
            map.put(PrimitiveAsset.STRUCTURE, director.getStructureAssetDirector().getObjectManager().getObject(structureEarner));
        String objectEarner = director.getObjectAssetDirector().isLinked(translatableItem);
        if (objectEarner != null)
            map.put(PrimitiveAsset.OBJECT, director.getObjectAssetDirector().getObjectManager().getObject(objectEarner));
        return map;
    }

    @Nullable
    public MechanicsData getMechanicsData(String key) {
        return director.getMechanicsDataDirector().getObjectManager().getObject(key);
    }

    @NotNull
    public Set<String> getAllKeywords() {
        return Collections.unmodifiableSet(director.getConfigManager().getAllKeywords());
    }

    @NotNull
    public Set<String> getHalfKeywords() {
        return Collections.unmodifiableSet(director.getConfigManager().getHalfKeywords());
    }

    /**
     * Checks if the entity is a plot helper.
     *
     * @param entity The entity to check
     * @return The PlotProfile the PlotHelper belongs to, or null if it's not a PlotHelper
     */
    @Nullable
    public PlotProfile isPlotHelper(@Nullable Entity entity) {
        if (entity == null)
            return null;
        for (PlotProfile profile : director.getPlotProfileManager().getAll()) {
            if (profile.getPlotHelper() != null &&
                    profile.getPlotHelper().getUniqueId().equals(entity.getUniqueId())) {
                return profile;
            }
        }
        return null;
    }

    @NotNull
    public List<PlotHelperTrade> getAllPlotHelperTrades() {
        return director.getPlotProfileManager().getAll().stream()
                .map(PlotHelperContainer::getTrades)
                .map(Map::values)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Opens a Community Trade to a specific player
     *
     * @param player         The player to open the Community Trade
     * @param communityTrade The container to get the Community Trade
     * @return true if successful, false otherwise
     */
    public boolean openCommunityTrade(@NotNull Player player,
                                      @NotNull PlotHelperContainer communityTrade) {
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            return false;
        PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
        BlobLibInventoryAPI.getInstance().customSelector(
                "Community-Trades",
                player,
                "Trades",
                "Trade",
                () -> communityTrade.getTrades().values().stream().toList(),
                trade -> {
                    tycoonPlayer.setCommunityTrade(communityTrade);
                    profile.openTradeUI(player, trade, false);
                },
                trade -> {
                    ItemStack itemStack = trade.itemStack(player);
                    List<String> lore = new ArrayList<>();
                    if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasLore())
                        lore.addAll(itemStack.getItemMeta().getLore());
                    TranslatableBlock block = BlobLibTranslatableAPI.getInstance()
                            .getTranslatableBlock("BlobTycoon-PlotHelper.Trade-View", player);
                    lore.addAll(block.get());
                    String format;
                    ItemStackModder.mod(itemStack)
                            .lore(lore)
                            .replace("%sellers%", String.join(", ", trade.getOwners()))
                            .replace("%price%", trade.formatPrice());
                    return itemStack;
                },
                null,
                null,
                null);
        return true;
    }

}
