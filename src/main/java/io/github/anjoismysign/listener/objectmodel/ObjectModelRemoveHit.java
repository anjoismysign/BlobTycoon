package io.github.anjoismysign.listener.objectmodel;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.structure.ObjectModel;
import io.github.anjoismysign.listener.BlobTycoonListener;

public class ObjectModelRemoveHit extends BlobTycoonListener {
    public ObjectModelRemoveHit(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onRemove(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        if (event.getEntityType() != EntityType.INTERACTION)
            return;
        Interaction interaction = (Interaction) event.getEntity();
        if (!ObjectModel.isObjectModel(interaction))
            return;
        event.setCancelled(true);
        Player player = (Player) event.getDamager();
        ObjectModel.remove(player, interaction, 0);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getObjectModelRemoveHit()
                .register();
    }
}
