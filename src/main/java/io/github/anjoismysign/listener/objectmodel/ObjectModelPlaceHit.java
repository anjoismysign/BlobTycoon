package io.github.anjoismysign.listener.objectmodel;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.TycoonKey;
import io.github.anjoismysign.entity.structure.ItemFrameType;
import io.github.anjoismysign.entity.structure.ObjectModel;
import io.github.anjoismysign.listener.BlobTycoonListener;

public class ObjectModelPlaceHit extends BlobTycoonListener {
    public ObjectModelPlaceHit(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onPlace(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        if (event.getEntityType() != EntityType.ITEM_FRAME)
            return;
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        PersistentDataContainer container = itemFrame.getPersistentDataContainer();
        if (!container.has(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING))
            return;
        ItemFrameType itemFrameType = ItemFrameType.valueOf(container.get(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING));
        if (itemFrameType != ItemFrameType.OBJECT_SLOT)
            return;
        event.setCancelled(true);
        Player player = (Player) event.getDamager();
        ObjectModel.place(itemFrame, player);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getObjectModelPlaceHit()
                .register();
    }
}
