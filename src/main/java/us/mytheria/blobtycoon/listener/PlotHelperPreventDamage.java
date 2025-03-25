package us.mytheria.blobtycoon.listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.configuration.PlotHelperConfiguration;

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
