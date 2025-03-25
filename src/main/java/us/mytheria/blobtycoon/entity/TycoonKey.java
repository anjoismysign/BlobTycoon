package us.mytheria.blobtycoon.entity;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

public enum TycoonKey {
    STORAGE_AVAILABILITY(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "storage_availability")),
    TRANSIENT(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "transient")),
    ITEM_FRAME_TYPE(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "item_frame_type")),
    FACING(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "facing")),
    TYPE(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "type")),
    OBJECT_ID(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "object_id")),
    KEY(new NamespacedKey(Bukkit.getPluginManager().getPlugin("BlobTycoon"), "key"));
    private final NamespacedKey key;

    TycoonKey(NamespacedKey key) {
        this.key = key;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public static void setStorageAvailability(PersistentDataHolder holder,
                                              int amount) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.set(STORAGE_AVAILABILITY.getKey(), PersistentDataType.INTEGER, amount);
    }

    public static int getStorageAvailability(PersistentDataHolder holder) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        if (!container.has(STORAGE_AVAILABILITY.getKey(), PersistentDataType.INTEGER))
            throw new NullPointerException("No storage availability found");
        return container.get(STORAGE_AVAILABILITY.getKey(),
                PersistentDataType.INTEGER);
    }

    public static void transientize(PersistentDataHolder holder) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        container.set(TRANSIENT.getKey(), PersistentDataType.BYTE, (byte) 1);
    }

    public static boolean isTransient(PersistentDataHolder holder) {
        PersistentDataContainer container = holder.getPersistentDataContainer();
        return container.has(TRANSIENT.getKey(), PersistentDataType.BYTE);
    }
}
