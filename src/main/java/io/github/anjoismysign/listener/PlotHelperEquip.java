package io.github.anjoismysign.listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;

public class PlotHelperEquip extends BlobTycoonListener {
    public PlotHelperEquip(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (BlobTycoonInternalAPI.getInstance().isPlotHelper(entity) == null)
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        if (BlobTycoonInternalAPI.getInstance().isPlotHelper(entity) == null)
            return;
        event.setCancelled(true);
    }

    public boolean checkIfShouldRegister() {
        return true;
    }
}
