package us.mytheria.blobtycoon.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.bloblib.events.TranslatableItemCloneEvent;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.structure.PrimitiveAsset;

import java.util.Map;

public class TranslationItemApplier extends BlobTycoonListener {
    public TranslationItemApplier(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTranslate(TranslatableItemCloneEvent event) {
        TranslatableItem translatableItem = event.getTranslatableItem();
        Map<PrimitiveAsset, ItemStack> map = BlobTycoonInternalAPI
                .getInstance().hasDisplay(translatableItem);
        if (map.isEmpty())
            return;
        if (map.size() > 1)
            throw new IllegalStateException("More than one PrimitivePlotObject is linked to " +
                    translatableItem + "!");
        ItemStack itemStack = map.values().iterator().next();
        event.setClone(itemStack);
    }

    public boolean checkIfShouldRegister() {
        return true;
    }
}
