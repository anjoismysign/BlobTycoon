package us.mytheria.blobtycoon.entity.structure;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.BlobObject;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.entity.MechanicsProcessorHolder;
import us.mytheria.blobtycoon.entity.ScalarEarnerHolder;
import us.mytheria.blobtycoon.entity.Sellable;

public interface TycoonModelHolder<T extends TycoonModel> extends BlobObject,
        MechanicsProcessorHolder, ScalarEarnerHolder {
    /**
     * Will get the getModel that's being held.
     *
     * @return The structure getModel
     */
    @NotNull
    T getModel();

    /**
     * Will apply to the TranslatableItem.
     *
     * @param item The translatable item to apply to.
     * @return The displayed model. Null if not modified.
     */
    @Nullable
    ItemStack apply(TranslatableItem item);

    /**
     * Will display the TranslatableItem to a player.
     *
     * @param player The player to display to.
     * @return The displayed model.
     */
    @NotNull
    default ItemStack display(Player player) {
        TranslatableItem item = getModel().getTranslatableItem();
        ItemStack clone = item.getClone(false);
        TranslatableItem.localize(clone, player.getLocale());
        return clone;
    }

    /**
     * Weather or not the structure is getSellable.
     *
     * @return True if the structure is getSellable, false otherwise.
     */
    default boolean isSellable() {
        return getSellable() != null;
    }

    /**
     * Gets the getSellable object.
     *
     * @return The getSellable object
     */
    @Nullable
    Sellable getSellable();
}
