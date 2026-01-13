package io.github.anjoismysign.blobtycoon.listener;

import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.events.TranslatableItemCloneEvent;
import io.github.anjoismysign.blobtycoon.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobtycoon.director.manager.TycoonListenerManager;
import io.github.anjoismysign.blobtycoon.entity.structure.PrimitiveAsset;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

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
