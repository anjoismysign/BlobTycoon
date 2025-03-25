package us.mytheria.blobtycoon.entity.plothelper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.ChunkCoordinates;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.configuration.PlotHelperConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PlotHelper {
    protected transient int maxOrders;
    protected transient final Map<Integer, String> inventoriesReferences;
    protected transient final Map<Integer, PlotHelperTrade> trades;
    protected transient final Map<Integer, PlotHelperInventory> inventories;
    protected transient final Map<Inventory, PlotHelperInventory> openInventories;
    protected transient final TycoonManagerDirector director;
    protected transient final PlotHelperContainer container;

    private final BukkitTask saveTask;

    @Nullable
    protected transient Entity entity;
    private transient ChunkCoordinates chunkCoordinates;

    public PlotHelper(@NotNull Map<String, Object> serialized,
                      @NotNull TycoonManagerDirector director,
                      @NotNull PlotHelperContainer container) {
        this.director = director;
        this.container = container;
        this.maxOrders = PlotHelperConfiguration.getInstance().getMerchantConfiguration().getDefaultMaximumTrades();
        this.inventoriesReferences = new HashMap<>();
        this.inventories = new HashMap<>();
        this.openInventories = new HashMap<>();
        this.trades = new HashMap<>();
        this.saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                saveInventories();
            }
        }.runTaskTimerAsynchronously(director.getPlugin(), 20, 20 * 30);
        reload();
        Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
            deserialize(serialized, container);
        });
    }

    private void deserialize(@NotNull Map<String, Object> serialized,
                             @NotNull PlotHelperContainer container) {
        maxOrders = serialized.containsKey("maxOrders") ? (int) serialized.get("maxOrders") : PlotHelperConfiguration.getInstance().getMerchantConfiguration().getDefaultMaximumTrades();
        Map<Integer, Object> inventories = serialized.containsKey("inventories") ? (Map<Integer, Object>) serialized.get("inventories") : new HashMap<>();
        inventories.forEach((key, value) -> {
            this.inventoriesReferences.put(key, (String) value);
        });
        inventoriesReferences.forEach((order, reference) -> {
            director.getPlotHelperInventoryManager().readAsynchronously(reference)
                    .whenComplete(((plotHelperInventory, throwable) -> {
                        if (throwable != null) {
                            Bukkit.getLogger().severe("Failed to load inventory with reference " + reference);
                            throwable.printStackTrace();
                            return;
                        }
                        this.openInventories.put(plotHelperInventory.getInventory(), plotHelperInventory);
                        this.inventories.put(order, plotHelperInventory);
                    }));
        });
        Map<String, Object> trades = serialized.containsKey("trades") ? (Map<String, Object>) serialized.get("trades") : new HashMap<>();
        trades.forEach((key, value) -> PlotHelperTradeFactory.getInstance()
                .createTrade(PlotHelperTradeData.deserialize((Map<String, Object>) value), container));
    }

    private Map<String, Object> serializeTrades() {
        Map<String, Object> map = new HashMap<>();
        trades.forEach((key, value) -> map.put(key.toString(), value.serialize().serialize()));
        return map;
    }

    public void reload() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(director.getPlugin(), this::reload);
            return;
        }
        if (!(container instanceof PlotProfile plotProfile))
            return;
        if (entity != null && entity.isValid())
            entity.remove();
        if (!PlotHelperConfiguration.getInstance().isEnabled())
            return;
        entity = PlotHelperConfiguration.getInstance().spawnPlotHelper(plotProfile);
        chunkCoordinates = ChunkCoordinates.of(entity.getLocation().getChunk());
    }

    public void unload() {
        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(director.getPlugin(), this::unload);
            return;
        }
        if (entity != null && entity.isValid())
            entity.remove();
        saveInventories();
    }

    public void saveInventories() {
        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(director.getPlugin(), this::saveInventories);
            return;
        }
        Map.copyOf(inventories).forEach((order, inventory) -> {
            director.getPlotHelperInventoryManager().update(inventory.serializeAllAttributes());
        });
    }

    @Nullable
    public ChunkCoordinates getChunkCoordinates() {
        return chunkCoordinates;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("inventories", inventoriesReferences);
        map.put("trades", serializeTrades());
        map.put("maxOrders", maxOrders);
        return map;
    }
}
