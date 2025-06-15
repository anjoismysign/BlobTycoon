package io.github.anjoismysign.entity;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.utilities.ItemStackUtil;

import java.util.HashMap;
import java.util.Map;

public record ProfileInventory(@Nullable ItemStack[] getInventory,
                               @Nullable ItemStack[] getArmor,
                               @Nullable Integer getHeldItemSlot,
                               boolean wasGiven,
                               Map<String, Object> getSerialized) {
    public static ProfileInventory empty() {
        return new ProfileInventory(null,
                null,
                null,
                true,
                new HashMap<>());
    }

    /**
     * Creates a new ProfileInventory which has not been given to a player yet.
     *
     * @param inventory    The getInventory to create. Null to not apply
     * @param armor        The getArmor to create. Null to not apply
     * @param heldItemSlot The held item slot to create. Null to not apply
     * @return The new ProfileInventory
     */
    public static ProfileInventory of(@Nullable ItemStack[] inventory,
                                      @Nullable ItemStack[] armor,
                                      @Nullable Integer heldItemSlot) {
        return new ProfileInventory(inventory, armor, heldItemSlot,
                false, new HashMap<>());
    }

    /**
     * Creates a new ProfileInventory which has not been given to a player yet.
     *
     * @param map The map to deserialize from.
     * @return The new ProfileInventory
     */
    public static ProfileInventory deserialize(Map<String, Object> map) {
        if (map == null || map.isEmpty())
            return empty();
        Integer heldItemSlot = map.get("HeldItemSlot") == null ?
                null : (Integer) map.get("HeldItemSlot");
        return of(ItemStackUtil.itemStackArrayFromBase64
                        ((String) map.get("Inventory")),
                ItemStackUtil.itemStackArrayFromBase64
                        ((String) map.get("Armor")),
                heldItemSlot);
    }

    /**
     * Gives the getInventory to a player.
     * If the getInventory is null, it will not give anything.
     * If getInventory was already given, it will not give anything.
     *
     * @param player The player to give the getInventory to.
     * @return The new ProfileInventory
     */
    public ProfileInventory give(Player player) {
        if (wasGiven) return this;
        PlayerInventory playerInventory = player.getInventory();
        if (getInventory != null)
            playerInventory.setContents(getInventory);
        if (getArmor != null)
            playerInventory.setArmorContents(getArmor);
        if (getHeldItemSlot != null)
            playerInventory.setHeldItemSlot(getHeldItemSlot);
        return new ProfileInventory(getInventory, getArmor, getHeldItemSlot,
                true, getSerialized);
    }

    @Nullable
    public ProfileInventory serialize(Player player) {
        Map<String, Object> map = new HashMap<>();
        PlayerInventory playerInventory = player.getInventory();
        map.put("Inventory", ItemStackUtil.itemStackArrayToBase64(playerInventory.getContents()));
        map.put("Armor", ItemStackUtil.itemStackArrayToBase64(playerInventory.getArmorContents()));
        map.put("HeldItemSlot", playerInventory.getHeldItemSlot());
        return new ProfileInventory(getInventory, getArmor, getHeldItemSlot, wasGiven, map);
    }
}
