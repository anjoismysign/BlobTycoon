package io.github.anjoismysign.entity;

import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.anjo.entities.Tuple2;
import io.github.anjoismysign.blobeconomy.BlobEconomyMiddleman;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.BlobCrudable;
import io.github.anjoismysign.bloblib.entities.BlobSelector;
import io.github.anjoismysign.bloblib.entities.SharedSerializable;
import io.github.anjoismysign.bloblib.entities.currency.Currency;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableBlock;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.itemstack.ItemStackModder;
import io.github.anjoismysign.bloblib.utilities.ItemStackUtil;
import io.github.anjoismysign.bloblib.utilities.Structrador;
import io.github.anjoismysign.bloblib.utilities.TextColor;
import io.github.anjoismysign.bloblib.vault.multieconomy.ElasticEconomy;
import io.github.anjoismysign.director.TycoonManagerDirector;
import io.github.anjoismysign.entity.configuration.HologramConfiguration;
import io.github.anjoismysign.entity.configuration.PlotHelperConfiguration;
import io.github.anjoismysign.entity.configuration.PlotHelperDefaultTradeConfiguration;
import io.github.anjoismysign.entity.configuration.RebirthConfiguration;
import io.github.anjoismysign.entity.mechanics.Mechanics;
import io.github.anjoismysign.entity.mechanics.MechanicsData;
import io.github.anjoismysign.entity.mechanics.MechanicsOperator;
import io.github.anjoismysign.entity.plotdata.PlotData;
import io.github.anjoismysign.entity.plothelper.PlotHelper;
import io.github.anjoismysign.entity.plothelper.PlotHelperContainer;
import io.github.anjoismysign.entity.plothelper.PlotHelperTrade;
import io.github.anjoismysign.entity.structure.ItemFrameType;
import io.github.anjoismysign.event.PlayerRebirthEvent;
import io.github.anjoismysign.event.ProfileLoadEvent;
import io.github.anjoismysign.ui.context.CreateTradeContext;
import io.github.anjoismysign.util.TemperatureConversor;
import io.github.anjoismysign.util.TemperatureUnit;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Equivalent to Hypixel's Skyblock "Islands".
 * A PlotProfile can hold multiple PlotProprietors which are
 * the owners of the plot.
 * It can also hold multiple PlotExpansions which are the terrains.
 */
public class PlotProfile implements SharedSerializable<PlotProprietorProfile>,
        Serializable, EarnerOwner, ValuableAccount, BankAccount, MechanicsOperator,
        Host, PlotHelperContainer, PlotContainer {
    transient protected final TycoonManagerDirector director;
    transient private final BlobCrudable blobCrudable;
    transient private final TemperatureConversor temperatureConversor;

    /*
     * Non-transient attributes (in case of somebody being crazy enough
     * to do binary serialization instead of attribute based serialization)
     */
    private final List<PlotExpansion> expansions;
    private final Map<String, PlotProprietorProfile> proprietors;
    private final SharedSerializable<PlotProprietorProfile> sharedSerializable = this;
    transient private final Plot plot;
    transient private final PlotHelper plotHelper;
    transient private final Map<String, Mechanics> mechanics;
    transient private final Map<String, Double> mechanicsData;
    transient private final Map<String, Double> earners;
    transient private final Map<String, Double> scalarEarners;
    transient private final Map<String, Double> valuables;
    transient private final Map<UUID, Map<String, Double>> bankBalances;
    transient private final Map<String, Double> transientEarners;
    transient private final Map<String, Double> transientScalarEarners;
    transient private final Map<String, CreateTradeContext> tradeContexts;
    transient private final long lastConnection;
    transient private final Set<UUID> invited;
    transient private final Set<String> deniedVisit;
    final transient private Map<String, String> onlineProprietors;
    private final int rebirths;
    private int selectedExpansionIndex;
    transient private boolean isValid, plotLoaded;
    transient private BukkitTask mechanicsTimer;
    transient private boolean isFresh;

    public PlotProfile(BlobCrudable blobCrudable,
                       TycoonManagerDirector director,
                       Plot plot) {
        this.invited = new HashSet<>();
        Document document = blobCrudable.getDocument();
        this.rebirths = document.containsKey("Rebirths") ?
                document.getInteger("Rebirths") : 0;
        this.temperatureConversor = TemperatureConversor.of(TemperatureUnit.CELSIUS);
        this.plot = Objects.requireNonNull(plot);
        this.plotHelper = new PlotHelper(document.containsKey("PlotHelper") ?
                (Map<String, Object>) document.get("PlotHelper") : new HashMap<>(),
                director,
                this);
        this.director = director;
        this.blobCrudable = blobCrudable;
        this.proprietors = new HashMap<>();
        this.transientEarners = new HashMap<>();
        this.transientScalarEarners = new HashMap<>();
        this.selectedExpansionIndex = document.containsKey("SelectedPlot") ?
                document.getInteger("SelectedPlot") : 0;
        this.mechanicsData = document.containsKey("Mechanics") ?
                (Map<String, Double>) document.get("Mechanics") :
                new HashMap<>();
        this.earners = document.containsKey("Earners") ?
                (Map<String, Double>) document.get("Earners") :
                new HashMap<>();
        this.scalarEarners = document.containsKey("ScalarEarners") ?
                (Map<String, Double>) document.get("ScalarEarners") :
                new HashMap<>();
        this.valuables = document.containsKey("Valuables") ?
                (Map<String, Double>) document.get("Valuables") :
                new HashMap<>();
        this.bankBalances = document.containsKey("Bank-Balances") ?
                (Map<UUID, Map<String, Double>>) document.get("Bank-Balances") :
                new HashMap<>();
        List<Map<String, Object>> serializedProprietors = document.get("Proprietors", List.class);
        this.proprietors.putAll(serializedProprietors == null ? new HashMap<>() : deserializeProprietors(serializedProprietors)
                .stream().collect(Collectors.toMap(PlotProprietorProfile::getIdentification, plotProprietor -> plotProprietor)));
        List<Map<String, Object>> serializedExpansions = document.get("Plots", List.class);
        this.expansions = serializedExpansions == null ? new ArrayList<>() : deserializePlots(serializedExpansions);
        if (this.expansions.isEmpty()) {
            this.expansions.add(new PlotExpansion(DefaultStructures.STOCK.getStructrador(),
                    plot.getData().getDirection(),
                    0,
                    director.getPlugin()));
        }
        this.lastConnection = document.containsKey("Last-Connection") ?
                document.getLong("Last-Connection") : Instant.now().toEpochMilli();
        this.isFresh = document.containsKey("Is-Fresh") ?
                document.getBoolean("Is-Fresh") : true;
        if (isFresh) {
            int amount = PlotHelperConfiguration.getInstance().getStorageConfiguration().getDefaultStorage();
            addInventories(amount);
        }
        this.isValid = true;
        this.onlineProprietors = new HashMap<>();
        loadExpansion(selectedExpansionIndex, null, true, false);
        this.mechanics = new HashMap<>();
        this.tradeContexts = new HashMap<>();
        this.deniedVisit = new HashSet<>();
        reloadMechanicsOperator();
        HologramConfiguration hologramConfiguration = HologramConfiguration.getInstance();
        if (hologramConfiguration.isEnabled())
            hologramConfiguration.createHologram(this);
    }

    @Override
    public Map<String, PlotProprietorProfile> getProprietors() {
        return proprietors;
    }

    public void join(@NotNull Player player) {
        TycoonPlayer tycoonPlayer = director.getTycoonPlayerManager()
                .isBlobSerializable(player).orElseThrow();
        if (!proprietors.containsKey(tycoonPlayer.getIdentification()))
            throw new NullPointerException("PlotProprietorProfile not found! Use PlotProfile#visit instead");
        //At this point, the PlotProprietorProfile must exist!
        PlotProprietorProfile fetch = updateName(tycoonPlayer.getIdentification(), player);
        if (fetch == null)
            throw new NullPointerException("PlotProprietorProfile was null!");
        //Links the profile of the TycoonPlayer
        if (!tycoonPlayer.linkProfile(fetch)) {
            player.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD +
                    "For some reason, BlobTycoon is trying to load a profile while " +
                    "you have already linked another");
            return;
        }
        fetch.apply(player);
        ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
        if (!elasticEconomy.isMultiEconomy()) {
            throw new IllegalStateException("Not using multi-economy!");
        } else {
            getUserAccount(player.getUniqueId()).forEach((key, amount) -> {
                elasticEconomy.getImplementation(key).depositPlayer(player, amount);
            });
        }
        BlobLibMessageAPI.getInstance()
                .getMessage("BlobTycoon.Profile-Loaded", player)
                .modder()
                .replace("%profile%", fetch.getProfileName())
                .get()
                .handle(player);
        if (BlobTycoonInternalAPI.getInstance().teleportToPlotOnJoin())
            player.teleport(plot.getData().getHomeLocation());
        if (director.getConfigManager().notifyOwnerJoin()) {
            forEachOnlineProprietor(onlineTycoonPlayer -> {
                Player onlinePlayer = onlineTycoonPlayer.getPlayer();
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Owner-Join-Notify", onlinePlayer)
                        .modder()
                        .replace("%player%", player.getName())
                        .get()
                        .handle(onlinePlayer);
            });
        }
        Bukkit.getPluginManager().callEvent(new ProfileLoadEvent(tycoonPlayer));
        onlineProprietors.put(player.getUniqueId().toString(), fetch.getIdentification());
        reloadHologram();
    }

    public void reload() {
        reloadMechanicsOperator();
        reloadHologram();
        plotHelper().reload();
    }

    public void unload() {
        forEachOnlineProprietor(tycoonPlayer -> {
            Player player = tycoonPlayer.getPlayer();
            UUID uuid = player.getUniqueId();
            getCreateTradeContext(player).cancel(player);
            tradeContexts.remove(uuid.toString());
        });
    }

    /**
     * Closes the player relation to this PlotProfile.
     * Won't run any logic when the player switches plot and the plot is not loaded.
     *
     * @param player   The player to close.
     * @param onSwitch The runnable to run when the player switches plot.
     */
    public void close(@NotNull Player player,
                      @Nullable Runnable onSwitch) {
        close(player, onSwitch, null);
    }

    /**
     * Closes the player relation to this PlotProfile.
     *
     * @param player       The player to close.
     * @param onSwitch     The runnable to run when the player switches profile.
     * @param onCoopSwitch The runnable to run when the player switches profile but there are other owners online in current profile.
     */
    public void close(@NotNull Player player,
                      @Nullable Runnable onSwitch,
                      @Nullable Runnable onCoopSwitch) {
        UUID uuid = player.getUniqueId();
        getCreateTradeContext(player).cancel(player);
        tradeContexts.remove(uuid.toString());
        TycoonPlayer tycoonPlayer = director.getTycoonPlayerManager()
                .isBlobSerializable(player).orElseThrow();
        saveEco();

        String proprietorIdentification = onlineProprietors.get(tycoonPlayer.getIdentification());
        if (proprietorIdentification != null)
            disjoinProprietor(proprietorIdentification, player);

        if (onlineProprietors.isEmpty()) {
            plotHelper().unload();
            HologramConfiguration.getInstance().deleteHologram(plot.getData());
            director.getPlotProfileManager().removeObject(this);
            invalidate();
            if (plotLoaded) {
                if (!isPlacingQueued())
                    getCurrentExpansion().restructure(plot.getData());
                director.getPlotProfileManager().uploadObject(this, onSwitch);
            } else {
                freePlot();
            }
            if (mechanicsTimer != null)
                mechanicsTimer.cancel();
        } else {
            reloadHologram();
            if (onCoopSwitch != null)
                onCoopSwitch.run();
        }
    }

    /**
     * Frees the plot.
     *
     * @return If the plot was freed.
     */
    public boolean freePlot() {
        if (!onlineProprietors.isEmpty())
            return false;
        director.getPlotManager().freePlayerPlot(getPlot().getData().getIndex());
        return true;
    }

    /**
     * Will disjoin all online proprietors.
     * This also saves the getInventory of the proprietors.
     *
     * @param reAdd If true, it will re-add the proprietors.
     */
    public void disjoinOnlineProprietors(boolean reAdd) {
        new HashSet<>(onlineProprietors.entrySet()).forEach(entry -> {
            Player player = director.getPlugin().getServer().getPlayer(UUID.fromString(entry.getKey()));
            if (player == null)
                return;
            disjoinProprietor(entry.getValue(), player);
            if (reAdd)
                onlineProprietors.put(entry.getKey(), entry.getValue());
        });
    }

    private void disjoinProprietor(String proprietorIdentification, Player player) {
        PlotProprietorProfile profile = proprietors.get(proprietorIdentification);
        profile.serialize(player);
        onlineProprietors.remove(proprietorIdentification);
    }

    /**
     * Loads an expansion.
     * If the expansion is the same as the current one, will not proceed.
     * If it's already loading an expansion or placing other structure,
     * it won't proceed.
     *
     * @param index        The getIndex of the expansion.
     * @param whenComplete The runnable to run when the expansion is loaded.
     * @param force        If true, it will force the expansion to load.
     * @param saveCurrent  If true, it will save the current expansion structure.
     * @return The CompletableFuture of the expansion.
     */
    @Nullable
    private CompletableFuture<PlotPlaceProgress.PlaceResult> loadExpansion(int index,
                                                                           @Nullable Runnable whenComplete,
                                                                           boolean force,
                                                                           boolean saveCurrent) {
        CompletableFuture<PlotPlaceProgress.PlaceResult> future = new CompletableFuture<>();
        if (!force) {
            PlotPlaceProgress progress = plot.getProgress();
            if (index < 0 || index >= expansions.size() || index == selectedExpansionIndex || progress.isPlacing() || progress.isWaiting()) {
                future.complete(null);
                return future;
            }
        } else if (force && plot.getProgress().isWaiting()) {
            plot.clear();
        }
        PlotExpansion currentExpansion = getCurrentExpansion();
        PlotExpansion saveCurrentExpansion;
        if (saveCurrent)
            saveCurrentExpansion = getCurrentExpansion();
        else
            saveCurrentExpansion = null;
        Structrador structrador = expansions.get(index).getStructrador();
        plotLoaded = false;
        Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
            PlotPlaceProgress.PlaceResult result = plot.paste(structrador,
                    true, () -> {
                        selectedExpansionIndex = index;
                        blobCrudable.getDocument().put("SelectedPlot", selectedExpansionIndex);
                        plotLoaded = true;
                        if (whenComplete != null)
                            whenComplete.run();
                    }, saveCurrentExpansion, false,
                    new DirectionOperation(currentExpansion.getDirection(),
                            plot.getData().getDirection()));
            future.complete(result);
        });
        return future;
    }

    /**
     * Loads an expansion.
     * If the expansion is the same as the current one, will not proceed.
     * If it's already loading an expansion or placing other structure,
     * it won't proceed.
     *
     * @param index        The getIndex of the expansion.
     * @param whenComplete The runnable to run when the expansion is loaded.
     * @return The CompletableFuture of the expansion.
     */
    public CompletableFuture<PlotPlaceProgress.PlaceResult> loadExpansion(int index,
                                                                          @Nullable Runnable whenComplete) {
        return loadExpansion(index, whenComplete, false, true);
    }

    public BlobCrudable blobCrudable() {
        return blobCrudable;
    }

    public BlobCrudable serializeAllAttributes() {
        return serializeAllAttributes(false);
    }

    private BlobCrudable serializeAllAttributes(boolean reset) {
        Document document = blobCrudable.getDocument();
        document.put("Proprietors", serializeProprietors());
        if (reset)
            return blobCrudable;
        document.put("Rebirths", rebirths);
        document.put("Plots", serializePlots());
        document.put("Earners", earners);
        document.put("ScalarEarners", scalarEarners);
        document.put("Valuables", valuables);
        document.put("Bank-Balances", bankBalances);
        document.put("Mechanics", mechanicsData);
        document.put("Last-Connection", Instant.now().toEpochMilli());
        document.put("Is-Fresh", isFresh);
        document.put("PlotHelper", plotHelper.serialize());
        return blobCrudable;
    }

    /**
     * Will only save the proprietors.
     * It's meant to reload.
     */
    public void resetProgress() {
        mechanicsTimer.cancel();
        serializeAllAttributes(true);
    }

    public boolean isPlacingQueued() {
        PlotPlaceProgress progress = plot.getProgress();
        return progress.isPlacing() || progress.isWaiting();
    }

    /**
     * Gets the list of expansions.
     *
     * @return The list of expansions.
     */
    public List<PlotExpansion> getExpansions() {
        return expansions;
    }

    public Plot getPlot() {
        return plot;
    }

    public boolean canVisit(@NotNull Player visitor) {
        String name = visitor.getName();
        boolean isDenied = deniedVisit.contains(name);
        if (isDenied) {
            BlobLibMessageAPI.getInstance().getMessage("BlobTycoon.Visit-Failed", visitor)
                    .handle(visitor);
            return false;
        }
        deniedVisit.add(name);
        Bukkit.getScheduler().runTaskLater(director.getPlugin(), () -> {
            if (!visitor.isValid())
                return;
            deniedVisit.remove(name);
        }, 20 * 3);
        return true;
    }

    public int getSelectedExpansionIndex() {
        return selectedExpansionIndex;
    }

    /**
     * Retrieves the current expansion.
     * Might want to check with getPlot#getProgress#isPlacing() if it's placing
     *
     * @return The current expansion.
     */
    public PlotExpansion getCurrentExpansion() {
        return expansions.get(getSelectedExpansionIndex());
    }

    /**
     * If false, it means it was marked for removal.
     *
     * @return If the plot is valid.
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Whether the plot is loaded or not.
     *
     * @return Whether the plot is loaded or not.
     */
    public boolean isPlotLoaded() {
        return plotLoaded;
    }

    /**
     * Will mark the plot for removal.
     */
    public void invalidate() {
        isValid = false;
    }

    private List<Map<String, Object>> serializePlots() {
        return this.expansions.stream().map(PlotExpansion::serialize)
                .collect(Collectors.toList());
    }

    private List<PlotExpansion> deserializePlots(List<Map<String, Object>> list) {
        return list.stream().map(map -> PlotExpansion.deserialize(map, director.getPlugin()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<PlotProprietorProfile> deserializeProprietors(List<Map<String, Object>> list) {
        return list.stream().map(map -> new PlotProprietorProfile(map, this))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Will update the name of the proprietor.
     *
     * @param identification The identification of the proprietor.
     * @param player         The player to update the name to.
     * @return The updated proprietor.
     */
    @Nullable
    private PlotProprietorProfile updateName(String identification, Player player) {
        PlotProprietorProfile proprietor = proprietors.get(identification);
        if (proprietor == null)
            return null;
        proprietor = proprietor.updateName(player);
        addProprietor(proprietor);
        return proprietor;
    }

    public void forEachProprietor(@NotNull Consumer<UUID> consumer) {
        proprietors.values().stream()
                .map(PlotProprietorProfile::getIdentification)
                .map(UUID::fromString)
                .forEach(consumer);
    }

    public int getNumberOfOnlinePlayers() {
        return onlineProprietors.size();
    }

    /**
     * Will get a plot object remove button.
     *
     * @param uuid The uuid of the object.
     * @return The item frame remove button, null if not found.
     */
    @Nullable
    public ItemFrame getRemoveButton(UUID uuid) {
        PlotObject plotObject = getCurrentExpansion().getObject(uuid);
        if (plotObject == null)
            throw new NullPointerException("PlotObject not found by uuid (was structure cloned?)");
        PlotData plotData = plot.getData();
        Location location = plotData.fromOffset(plotObject.getOffset(), false);
        for (Entity entity : location.getWorld()
                .getNearbyEntities(location, 3, 3, 3)) {
            if (entity.getType() != EntityType.ITEM_FRAME)
                continue;
            ItemFrame itemFrame = (ItemFrame) entity;
            PersistentDataContainer container = itemFrame.getPersistentDataContainer();
            if (!container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                continue;
            if (!container.has(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING))
                continue;
            String id = container.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
            if (!id.equals(uuid.toString()))
                continue;
            ItemFrameType type = ItemFrameType.valueOf(container.get(TycoonKey.ITEM_FRAME_TYPE.getKey(),
                    PersistentDataType.STRING));
            if (type != ItemFrameType.REMOVE_BUTTON)
                continue;
            return itemFrame;
        }
        return null;
    }

    @NotNull
    public UUID generateObjectId() {
        UUID uuid = UUID.randomUUID();
        while (getCurrentExpansion().getObject(uuid) != null)
            uuid = UUID.randomUUID();
        return uuid;
    }

    private void reloadHologram() {
        HologramConfiguration hologramConfiguration = HologramConfiguration.getInstance();
        hologramConfiguration.deleteHologram(plot.getData());
        if (hologramConfiguration.isEnabled())
            hologramConfiguration.createHologram(this);
    }

    public Map<String, Double> getEarners() {
        return earners;
    }

    public Map<String, Double> getScalarEarners() {
        return scalarEarners;
    }

    public Map<String, Double> getTransientEarners() {
        return transientEarners;
    }

    public Map<String, Double> getTransientScalarEarners() {
        return transientScalarEarners;
    }

    public double getRebirthMultiplier() {
        double multiplier = 0.0;
        multiplier += rebirths;
        multiplier *= RebirthConfiguration.getInstance().getEarningMultiplier();
        multiplier += 1.0;
        return multiplier;
    }

    public Map<String, Double> getValuables() {
        return valuables;
    }

    public Map<UUID, Map<String, Double>> getBankBalances() {
        return bankBalances;
    }

    public TemperatureConversor getTemperatureConversor() {
        return temperatureConversor;
    }

    public Map<String, Double> getMechanicsData() {
        return mechanicsData;
    }

    @NotNull
    public Map<String, Mechanics> getMechanics() {
        return mechanics;
    }

    public BukkitTask mechanicsTimer() {
        if (mechanicsTimer != null)
            return mechanicsTimer;
        return Bukkit.getScheduler().runTaskTimer(director.getPlugin(), () -> {
            List<Mechanics> fallingShort = mechanics.values().stream()
                    .filter(Mechanics::isFallingShort)
                    .toList();
            if (fallingShort.isEmpty()) {
                getEarners().forEach((currency, d) -> {
                    depositValuable(currency, getTotalEarnings(currency));
                });
                return;
            }
            String fallingShortMessage = fallingShort.stream().map(Mechanics::getShortening)
                    .collect(Collectors.joining(", "));
            forEachOnlineProprietor(tycoonPlayer -> {
                Player player = tycoonPlayer.getPlayer();
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Insufficient-Mechanics", player)
                        .modder()
                        .replace("%mechanics%", fallingShortMessage)
                        .get()
                        .handle(player);
            });
        }, 0, 20);
    }

    @Override
    public void reloadMechanicsOperator() {
        if (mechanicsTimer != null) {
            mechanicsTimer.cancel();
            mechanicsTimer = null;
        }
        mechanics.clear();
        director.getMechanicsDataDirector().getObjectManager().values().stream()
                .filter(MechanicsData::isEnabled)
                .forEach(mechanicsData -> mechanics.put(mechanicsData.getKey(),
                        mechanicsData.instantiate(this)));
        mechanicsTimer = mechanicsTimer();
    }

    public void forEachOnlineProprietor(@NotNull Consumer<TycoonPlayer> consumer) {
        onlineProprietors.keySet().forEach(uuid -> {
            Player player = director.getPlugin().getServer().getPlayer(UUID.fromString(uuid));
            if (player == null)
                return;
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                    .getTycoonPlayer(player);
            if (tycoonPlayer == null)
                return;
            consumer.accept(tycoonPlayer);
        });
    }

    public long getLastConnection() {
        return lastConnection;
    }

    public boolean isFresh() {
        return isFresh;
    }

    public void setFresh(boolean fresh) {
        if (!isFresh)
            return;
        isFresh = fresh;
    }

    public void saveEco() {
        ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
        if (!elasticEconomy.isMultiEconomy())
            throw new IllegalStateException("Not using multi-economy!");
        else {
            forEachOnlineProprietor(tycoonPlayer -> {
                Player player = tycoonPlayer.getPlayer();
                if (player == null)
                    return;
                Map<String, Double> account = getUserAccount(player.getUniqueId());
                elasticEconomy.getAllImplementations().forEach(identityEconomy -> {
                    double amount = identityEconomy.getBalance(player);
                    account.put(identityEconomy.getName(), amount);
                });
            });
        }
    }

    public Set<UUID> getInvited() {
        return invited;
    }

    /**
     * Resets the progress of profile.
     *
     * @param sender The sender to notify
     * @param target The target to notify
     */
    public void reset(@Nullable CommandSender sender, @Nullable Player target) {
        Tuple2<ProfileInitData, List<TycoonPlayer>> result = ProfileInitData.reset(this, false);
        PlotProfileManager manager = director.getPlotProfileManager();
        manager.removeObject(this);
        ProfileInitData initData = result.first();
        PlotProfile reset = new PlotProfile(initData.crudable(), director,
                initData.plot());
        manager.addObject(reset);
        result.second().forEach(unlinked -> {
            Player player = unlinked.getPlayer();
            if (player == null)
                return;
            reset.join(player);
        });
        if (sender == null || target == null)
            return;
        BlobLibMessageAPI.getInstance()
                .getMessage("BlobTycoonCmd.Progress-Successfully-Reset", sender)
                .modder()
                .replace("%player%", target.getName())
                .get()
                .toCommandSender(sender);
    }

    /**
     * Rebirths the progress of profile.
     */
    public void rebirth() {
        Tuple2<ProfileInitData, List<TycoonPlayer>> result = ProfileInitData.reset(this, true);
        PlotProfileManager manager = director.getPlotProfileManager();
        manager.removeObject(this);
        ProfileInitData initData = result.first();
        PlotProfile reset = new PlotProfile(initData.crudable(), director,
                initData.plot());
        manager.addObject(reset);
        result.second().forEach(unlinked -> {
            Player player = unlinked.getPlayer();
            if (player == null)
                return;
            reset.join(player);
        });
        reset.forEachOnlineProprietor(online -> {
            PlayerRebirthEvent event = new PlayerRebirthEvent(online);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
            Player player = online.getPlayer();
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Rebirth-Success", player)
                    .handle(player);
        });
    }

    public int getRebirths() {
        return rebirths;
    }

    public PlotHelper plotHelper() {
        return plotHelper;
    }

    @Override
    public @NotNull List<String> getOwners() {
        List<String> list = new ArrayList<>();
        forEachOnlineProprietor(tycoonPlayer -> {
            Player player = tycoonPlayer.getPlayer();
            list.add(player.getName());
        });
        return list;
    }

    @NotNull
    public CreateTradeContext getCreateTradeContext(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        String uuid = player.getUniqueId().toString();
        if (!onlineProprietors.containsKey(uuid))
            throw new IllegalStateException("Player is not a proprietor");
        PlotHelperDefaultTradeConfiguration tradeConfiguration = PlotHelperConfiguration
                .getInstance().getMerchantConfiguration().getDefaultTradeConfiguration();
        return tradeContexts.computeIfAbsent(uuid, key -> CreateTradeContext
                .of(tradeConfiguration.getCurrency(), tradeConfiguration.getAmount()));
    }

    /**
     * Opens the Trade creation UI to a specific player.
     *
     * @param player The player to open the UI to.
     */
    public void openCreateTradeUI(
            @NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        String uuid = player.getUniqueId().toString();
        if (!onlineProprietors.containsKey(uuid))
            throw new IllegalStateException("Player is not a proprietor");
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        CreateTradeContext context = getCreateTradeContext(player);
        BlobInventory blobInventory = BlobLibInventoryAPI.getInstance()
                .trackInventory(player, "Create-Trade").getInventory();
        blobInventory.modify("Create", current -> {
            if (!context.isReady())
                return current;
            TranslatableItem tradingItemTranslatableItem = context.getTradingTranslatableItem();
            ItemStack tradingItem = context.getTradingItem();
            String itemDisplay = tradingItemTranslatableItem == null ? ItemStackUtil.display(tradingItem) :
                    ItemStackUtil.display(tradingItemTranslatableItem.localize(player).get());
            if (Registry.MATERIAL.match(itemDisplay) != null)
                itemDisplay = TextColor.PARSE("&f" + itemDisplay);
            String name = context.getCurrency();
            String format = BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(name)
                    .format(context.getAmount());
            return TranslatableItem
                    .by("BlobTycoon-PlotHelper.Create-Trade")
                    .modder()
                    .replace("%item%", itemDisplay)
                    .replace("%format%", format)
                    .get()
                    .get();
        });
        blobInventory.modify("Item", current -> {
            ItemStack itemStack = context.getTradingItem();
            if (itemStack == null)
                return current;
            TranslatableItem tradingItemTranslatableItem = context.getTradingTranslatableItem();
            ItemStack clone;
            if (tradingItemTranslatableItem != null) {
                int amount = itemStack.getAmount();
                clone = tradingItemTranslatableItem.localize(player).getClone();
                clone.setAmount(amount);
            } else
                clone = new ItemStack(itemStack);
            ItemMeta itemMeta = clone.getItemMeta();
            if (itemMeta == null)
                throw new IllegalStateException("Cannot set lore if #getItemMeta returns null");
            List<String> add = BlobLibTranslatableAPI.getInstance()
                    .getTranslatableBlock("BlobTycoon-PlotHelper.Pick-Up-Item", player)
                    .get();
            List<String> lore = itemMeta.getLore() == null ? new ArrayList<>() : itemMeta.getLore();
            lore.addAll(add);
            itemMeta.setLore(lore);
            clone.setItemMeta(itemMeta);
            return clone;
        });
        blobInventory.modder("Currency", modder -> {
            String name = context.getCurrency();
            Currency currency = BlobEconomyMiddleman.getInstance().getCurrency(name);
            String displayCurrency = currency == null ? name : currency.getDisplayName(player);
            modder.replace("%currency%", displayCurrency);
        });
        blobInventory.modder("Price", modder -> {
            String name = context.getCurrency();
            String format = BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(name)
                    .format(context.getAmount());
            modder.replace("%format%", format);
        });
        blobInventory.open(player);
    }

    public void openManageTradesUI(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            throw new NullPointerException("'player' not inside plugin cache");
        PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
        BlobLibInventoryAPI.getInstance().customSelector(
                "Trades-Manager",
                player,
                "Trades",
                "Trade",
                () -> profile.getTrades().values().stream().toList(),
                trade -> {
                    openTradeUI(player, trade, true);
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
                onReturnPlayer -> {
                    BlobLibInventoryAPI.getInstance().trackInventory(onReturnPlayer, "Plot-Helper-Trading")
                            .getInventory().open(onReturnPlayer);
                },
                null,
                null);

    }

    /**
     * Makes a specific player open the trades marketplace
     *
     * @param player The player to open the UI to.
     * @param query  The query to search for.
     */
    public void openTradesMarketplaceUI(
            @NotNull Player player,
            @Nullable String query) {
        Objects.requireNonNull(player, "'player' cannot be null");
        BlobSelector<PlotHelperTrade> blobSelector = BlobLibInventoryAPI.getInstance().customSelector(
                "Trades-Marketplace",
                player,
                "Trades",
                "Trade",
                () -> {
                    if (query != null) {
                        String lowercasedQuery = query.toLowerCase(Locale.ROOT);
                        return BlobTycoonInternalAPI.getInstance().getAllPlotHelperTrades()
                                .stream()
                                .filter(trade -> {
                                    ItemStack itemStack = trade.itemStack(player);
                                    Material type = itemStack.getType();
                                    String searchableType = type.name().toLowerCase(Locale.ROOT).replace("_", "");
                                    if (searchableType.contains(lowercasedQuery))
                                        return true;
                                    ItemMeta itemMeta = itemStack.getItemMeta();
                                    if (itemMeta == null)
                                        return false;
                                    if (!itemMeta.hasDisplayName())
                                        return false;
                                    String displayName = itemMeta.getDisplayName();
                                    if (ChatColor.stripColor(displayName).toLowerCase(Locale.ROOT).contains(lowercasedQuery))
                                        return true;
                                    if (!itemMeta.hasLore())
                                        return false;
                                    List<String> lore = itemMeta.getLore();
                                    for (String line : lore) {
                                        if (!ChatColor.stripColor(line).toLowerCase(Locale.ROOT).contains(lowercasedQuery))
                                            continue;
                                        return true;
                                    }
                                    return false;
                                })
                                .toList();
                    } else
                        return BlobTycoonInternalAPI.getInstance().getAllPlotHelperTrades();
                },
                trade -> {
                    openTradeUI(player, trade, false);
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
                onReturnPlayer -> {
                    BlobLibInventoryAPI.getInstance().trackInventory(onReturnPlayer, "Plot-Helper-Trading")
                            .getInventory().open(onReturnPlayer);
                },
                null,
                null);
        if (query == null)
            return;
        blobSelector.modify("Search", current -> {
            TranslatableBlock block = BlobLibTranslatableAPI.getInstance().getTranslatableBlock("BlobTycoon-PlotHelper.Search-Query", player);
            List<String> lore = new ArrayList<>();
            ItemMeta itemMeta = Objects.requireNonNull(current.getItemMeta(), "'current' has no ItemMeta");
            if (itemMeta.hasLore())
                lore.addAll(itemMeta.getLore());
            List<String> add = block.get();
            lore.addAll(add);
            itemMeta.setLore(lore);
            current.setItemMeta(itemMeta);
            ItemStackModder.mod(current)
                    .replace("%query%", query);
            return current;
        });
    }

    public void openTradeUI(
            @NotNull Player player,
            @NotNull PlotHelperTrade trade,
            boolean isManageTrade
    ) {
        Objects.requireNonNull(player, "'player' cannot be null");
        Objects.requireNonNull(trade, "'trade' cannot be null");
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            throw new NullPointerException("'player' not inside plugin cache");
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        if (!isManageTrade && plotProfile.getTrades().containsValue(trade)) {
            plotProfile.openTradesMarketplaceUI(player, tycoonPlayer.getTradeQuery());
            return;
        }
        tycoonPlayer.setTrade(trade);
        BlobInventory blobInventory;
        if (!isManageTrade) {
            blobInventory = BlobLibInventoryAPI.getInstance()
                    .trackInventory(player, "View-Trade")
                    .getInventory();
            blobInventory.modder("Buy", itemStackModder -> {
                itemStackModder.replace("%price%", trade.formatPrice());
            });
        } else
            blobInventory = BlobLibInventoryAPI.getInstance()
                    .trackInventory(player, "Cancel-Trade")
                    .getInventory();
        if (!trade.isForSale()) {
            blobInventory.modify("Item", ignore -> {
                ItemStack itemStack = trade.itemStack(player);
                List<String> lore = new ArrayList<>();
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null && itemMeta.hasLore())
                    lore.addAll(itemMeta.getLore());
                TranslatableBlock block = BlobLibTranslatableAPI.getInstance()
                        .getTranslatableBlock("BlobTycoon-PlotHelper.Trade-No-Longer-For-Sale", player);
                lore.addAll(block.get());
                ItemStackModder.mod(itemStack)
                        .lore(lore)
                        .replace("%sellers%", String.join(", ", trade.getOwners()))
                        .replace("%price%", trade.formatPrice());
                return itemStack;
            });
        } else {
            blobInventory.modify("Item", ignore -> {
                ItemStack itemStack = trade.itemStack(player);
                List<String> lore = new ArrayList<>();
                ItemMeta itemMeta = itemStack.getItemMeta();
                if (itemMeta != null && itemMeta.hasLore())
                    lore.addAll(itemMeta.getLore());
                TranslatableBlock block = BlobLibTranslatableAPI.getInstance()
                        .getTranslatableBlock("BlobTycoon-PlotHelper.Trade-View", player);
                lore.addAll(block.get());
                ItemStackModder.mod(itemStack)
                        .lore(lore)
                        .replace("%sellers%", String.join(", ", trade.getOwners()))
                        .replace("%price%", trade.formatPrice());
                return itemStack;
            });
        }
        blobInventory.open(player);
    }
}