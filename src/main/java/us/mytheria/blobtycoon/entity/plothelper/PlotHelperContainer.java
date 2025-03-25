package us.mytheria.blobtycoon.entity.plothelper;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlotHelperContainer {
    PlotHelper plotHelper();

    /**
     * Gets the PlotHelper entity.
     *
     * @return The PlotHelper entity.
     */
    @Nullable
    default Entity getPlotHelper() {
        return plotHelper().entity;
    }

    /**
     * Opens the PlotHelper inventory for the given player.
     *
     * @param player The player to open the inventory for.
     */
    default void openPlotHelperInventory(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        BlobLibInventoryAPI.getInstance().trackInventory(player, "Plot-Helper")
                .getInventory().open(player);
    }

    /**
     * Resets the PlotHelper.
     *
     * @param rebirth Whether the PlotHelper is being reset due to a rebirth.
     */
    default void resetPlotHelper(boolean rebirth) {
        // currently it deletes inventories and trades
        PlotHelperInventoryManager inventoryManager = plotHelper().director.getPlotHelperInventoryManager();
        plotHelper().inventoriesReferences.forEach((order, reference) -> {
            inventoryManager.deleteObject(reference);
        });
    }

    /**
     * Adds a PlotHelper inventory
     *
     * @return A CompletableFuture that completes when the inventory is added.
     */
    default CompletableFuture<PlotHelperInventory> addInventory() {
        Map<Integer, String> references = plotHelper().inventoriesReferences;
        int order = references.size();
        if (references.containsKey(order))
            throw new IllegalStateException("Order already exists. Had inventories been deleted?");
        UUID uuid = UUID.randomUUID();
        String reference = uuid.toString();
        references.put(order, reference);
        return plotHelper().director.getPlotHelperInventoryManager()
                .readAsynchronously(reference)
                .whenComplete((plotHelperInventory, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        return;
                    }
                    plotHelper().openInventories.put(plotHelperInventory.getInventory(), plotHelperInventory);
                    plotHelper().inventories.put(order, plotHelperInventory);
                });
    }

    /**
     * Adds the specified number of inventories, one after the other.
     *
     * @param count The number of inventories to add.
     * @return A CompletableFuture that completes when all inventories are added.
     */
    default CompletableFuture<Void> addInventories(int count) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        for (int i = 0; i < count; i++) {
            future = future.thenCompose(ignored -> addInventory().thenApply(ignored2 -> null));
        }

        return future;
    }

    /**
     * Gets the PlotHelper's inventories.
     *
     * @return A map of the PlotHelper's inventories, where the key is the order of the inventory and the value is the inventory.
     */
    @NotNull
    default Map<Integer, PlotHelperInventory> getInventories() {
        return Map.copyOf(plotHelper().inventories);
    }

    /**
     * Checks if the PlotHelper has PlotHelperInventory linked to the given inventory.
     *
     * @param inventory The inventory to check.
     * @return The PlotHelperInventory linked to the given inventory, or null if no PlotHelperInventory is linked.
     */
    @Nullable
    default PlotHelperInventory isLinked(@Nullable Inventory inventory) {
        if (inventory == null)
            return null;
        return plotHelper().openInventories.get(inventory);
    }

    /**
     * Checks if can add a trade to the PlotHelper.
     *
     * @return The order of the trade that can be added, or null if no trade can be added.
     */
    @Nullable
    default Integer canAddTrade() {
        int max = plotHelper().maxOrders;
        Map<Integer, PlotHelperTrade> x = plotHelper().trades;
        for (int i = 0; i < max; i++) {
            if (!x.containsKey(i)) {
                return i;
            }
        }
        return null;
    }

    /**
     * Adds a trade to the PlotHelper.
     *
     * @param plotHelperTrade The trade to add
     * @param order           The order of the trade
     */
    default void addTrade(@NotNull PlotHelperTrade plotHelperTrade,
                          @NotNull Integer order) {
        Objects.requireNonNull(plotHelperTrade, "'plotHelperTrade' cannot be null");
        Objects.requireNonNull(order, "'order' cannot be null");
        plotHelper().trades.put(order, plotHelperTrade);
    }

    /**
     * Removes a trade from the PlotHelper.
     *
     * @param order the order of the trade
     */
    default void removeTrade(int order) {
        plotHelper().trades.entrySet().removeIf(entry -> entry.getKey() == order);
    }

    /**
     * Gets all trades that the PlotHelper is managing.
     *
     * @return A map of trades, where the key is the trade order and the value is the trade.
     */
    @NotNull
    default Map<Integer, PlotHelperTrade> getTrades() {
        return Map.copyOf(plotHelper().trades);
    }

    default int getMaxOrders() {
        return plotHelper().maxOrders;
    }

    /**
     * Sets the maximum number of orders this PlotHelper can have.
     * If the new maximum number of orders is less than the current maximum number of orders, the excess orders will be removed.
     *
     * @param maxOrders The new maximum number of orders.
     * @return True if the maximum number of orders was successfully set, false otherwise.
     */
    default boolean setMaxOrders(int maxOrders) {
        if (maxOrders < plotHelper().maxOrders) {
            for (int i = plotHelper().maxOrders - 1; i >= maxOrders; i--) {
                plotHelper().trades.remove(i);
            }
        }
        plotHelper().maxOrders = maxOrders;
        return true;
    }

    /**
     * Gets the owners
     *
     * @return the owners
     */
    @NotNull
    List<String> getOwners();
}
