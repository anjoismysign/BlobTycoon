package us.mytheria.blobtycoon.listener.objectmodel;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.structure.ObjectModel;
import us.mytheria.blobtycoon.listener.BlobTycoonListener;

public class ObjectModelRemoveInteract extends BlobTycoonListener {
    public ObjectModelRemoveInteract(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onRemove(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked().getType() != EntityType.INTERACTION)
            return;
        Interaction interaction = (Interaction) event.getRightClicked();
        if (!ObjectModel.isObjectModel(interaction))
            return;
        event.setCancelled(true);
        ObjectModel.remove(event.getPlayer(), interaction, 0);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getObjectModelRemoveInteract()
                .register();
    }
}
