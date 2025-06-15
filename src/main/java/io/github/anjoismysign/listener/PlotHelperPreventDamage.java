package io.github.anjoismysign.listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.configuration.PlotHelperConfiguration;

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
