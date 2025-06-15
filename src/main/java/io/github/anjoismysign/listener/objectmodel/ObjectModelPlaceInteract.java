package io.github.anjoismysign.listener.objectmodel;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.TycoonKey;
import io.github.anjoismysign.entity.structure.ItemFrameType;
import io.github.anjoismysign.entity.structure.ObjectModel;
import io.github.anjoismysign.listener.BlobTycoonListener;

public class ObjectModelPlaceInteract extends BlobTycoonListener {
    public ObjectModelPlaceInteract(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onPlace(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getRightClicked().getType() != EntityType.ITEM_FRAME)
            return;
        ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
        PersistentDataContainer container = itemFrame.getPersistentDataContainer();
        if (!container.has(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING))
            return;
        ItemFrameType itemFrameType = ItemFrameType.valueOf(container.get(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING));
        if (itemFrameType != ItemFrameType.OBJECT_SLOT)
            return;
        event.setCancelled(true);
        ObjectModel.place(itemFrame, event.getPlayer());
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getObjectModelPlaceInteract()
                .register();
    }
}
