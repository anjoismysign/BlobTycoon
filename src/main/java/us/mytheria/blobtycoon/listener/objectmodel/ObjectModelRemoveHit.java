package us.mytheria.blobtycoon.listener.objectmodel;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.structure.ObjectModel;
import us.mytheria.blobtycoon.listener.BlobTycoonListener;

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
