package us.mytheria.blobtycoon.entity.plothelper;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.utilities.ItemStackSerializer;
import us.mytheria.bloblib.utilities.PlayerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the data of a trade with a PlotHelper
 *
 * @param getItemStack The ItemStack of the TranslatableItem involved in the trade.
 * @param getPrice     The price of the TranslatableItem involved in the trade PER UNIT.
 * @param getCurrency  The currency of the trade.
 */
public record PlotHelperTradeData(@NotNull ItemStack getItemStack,
                                  double getPrice,
                                  @NotNull String getCurrency) {
    public static PlotHelperTradeData deserialize(@NotNull Map<String, Object> serialized) {
        return new PlotHelperTradeData(ItemStackSerializer.fromBase64((String) serialized.get("itemstack")),
                (double) serialized.get("price"),
                (String) serialized.get("currency"));
    }

    public PlotHelperTradeData setItemStack(@NotNull ItemStack itemStack) {
        Objects.requireNonNull(itemStack, "'itemstack' cannot be null");
        return new PlotHelperTradeData(itemStack, getPrice, getCurrency);
    }

    public PlotHelperTradeData setPrice(double price) {
        return new PlotHelperTradeData(getItemStack, price, getCurrency);
    }

    public PlotHelperTradeData setCurrency(@NotNull String currency) {
        Objects.requireNonNull(currency, "'currency' cannot be null");
        return new PlotHelperTradeData(getItemStack, getPrice, currency);
    }

    public PlotHelperTradeData setBuy(boolean buy) {
        return new PlotHelperTradeData(getItemStack, getPrice, getCurrency);
    }

    @Nullable
    public TranslatableItem toTranslatableItem() {
        return TranslatableItem.byItemStack(getItemStack);
    }

    /**
     * Checks if is similar to another PlotHelperTradeData by matching.
     * ItemStack doesn't need to be the same (just to be the same TranslatableItem#getReference)
     * If ItemStack is not a TranslatableItem, it only
     *
     * @param other the PlotHelperTradeData to compare
     * @return True if is similar, false otherwise
     */
    public boolean isSimilar(@Nullable PlotHelperTradeData other) {
        if (other == null)
            return false;
        TranslatableItem currentTranslatableItem = toTranslatableItem();
        if (currentTranslatableItem == null) {
            return getItemStack.isSimilar(other.getItemStack) &&
                    Double.compare(getPrice, other.getPrice) == 0 &&
                    getCurrency.equals(other.getCurrency);
        }
        TranslatableItem otherTranslatableItem = other.toTranslatableItem();
        return currentTranslatableItem != null &&
                otherTranslatableItem != null &&
                currentTranslatableItem.identifier().equals(otherTranslatableItem.identifier()) &&
                Double.compare(getPrice, other.getPrice) == 0 &&
                getCurrency.equals(other.getCurrency);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("itemstack", ItemStackSerializer.toBase64(getItemStack));
        map.put("price", getPrice);
        map.put("currency", getCurrency);
        return map;
    }

    /**
     * Gives the involved item to a player
     *
     * @param player The player to claim the trade with.
     */
    public void give(@NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        TranslatableItem translatableItem = toTranslatableItem();
        if (translatableItem == null) {
            PlayerUtil.giveItemToInventoryOrDrop(player, new ItemStack(getItemStack));
            return;
        }
        ItemStack localized = translatableItem.localize(player).getClone();
        localized.setAmount(getItemStack.getAmount());
        PlayerUtil.giveItemToInventoryOrDrop(player, localized);
    }
}
