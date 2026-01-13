package io.github.anjoismysign.blobtycoon.listener;

import io.github.anjoismysign.blobtycoon.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobtycoon.director.manager.TycoonListenerManager;
import io.github.anjoismysign.blobtycoon.entity.configuration.PlotHelperConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlotHelperPreventDamage extends BlobTycoonListener {
    public PlotHelperPreventDamage(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (BlobTycoonInternalAPI.getInstance().isPlotHelper(entity) == null)
            return;
        event.setCancelled(true);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getPlotHelperPreventDamage()
                .register()
                &&
                PlotHelperConfiguration.getInstance().isEnabled();
    }
}
