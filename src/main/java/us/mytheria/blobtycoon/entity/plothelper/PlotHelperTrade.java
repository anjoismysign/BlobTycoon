package us.mytheria.blobtycoon.entity.plothelper;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.utilities.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public interface PlotHelperTrade {

    /**
     * Gets the owners
     *
     * @return the owners
     */
    @NotNull
    List<String> getOwners();

    /**
     * Cancels the trade
     *
     * @param player The player that requests to cancel the trade
     */
    void cancel(@NotNull Player player);

    /**
     * Whether the trade can be processed by the given player and by this PlotHelper
     *
     * @param player The player to check
     * @return Whether the trade can be processed
     */
    boolean canProcess(@NotNull Player player);

    /**
     * Whether the trade is still for sale.
     *
     * @return True if the trade is still for sale, false if was already sold
     */
    boolean isForSale();

    /**
     * Will process the trade with the given player, needs to be checked with {@link #canProcess(Player)} first
     * If player is null, the trade will be processed as the server instead of a player (unlimited funds)
     *
     * @param player The player to process the trade with
     */
    void process(@Nullable Player player);

    /**
     * Serializes the trade to a {@link PlotHelperTradeData} object
     *
     * @return The serialized trade
     */
    @NotNull
    PlotHelperTradeData serialize();

    /**
     * Formats the trade price using the currency's format
     *
     * @return The formatted price
     */
    @NotNull
    default String formatPrice() {
        PlotHelperTradeData data = serialize();
        String currency = data.getCurrency();
        double price = data.getPrice();
        return BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(currency).format(price);
    }

    @NotNull
    default ItemStack itemStack(@Nullable Player player) {
        ItemStack itemStack = serialize().getItemStack();
        ItemStack clone;
        TranslatableItem translatableItem = TranslatableItem.byItemStack(itemStack);
        if (translatableItem != null && player != null) {
            clone = translatableItem.localize(player).getClone();
            clone.setAmount(itemStack.getAmount());
        } else
            clone = new ItemStack(itemStack);
        return clone;
    }

    @NotNull
    default String display(@Nullable Player player) {
        ItemStack itemStack = itemStack(player);
        return ItemStackUtil.display(itemStack);
    }

    @NotNull
    default List<String> lore(@Nullable Player player) {
        ItemStack itemStack = itemStack(player);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null || !itemMeta.hasLore())
            return new ArrayList<>();
        return itemMeta.getLore();
    }

}
