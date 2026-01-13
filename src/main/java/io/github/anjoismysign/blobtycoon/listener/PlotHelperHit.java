package io.github.anjoismysign.blobtycoon.listener;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.blobtycoon.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobtycoon.director.manager.TycoonListenerManager;
import io.github.anjoismysign.blobtycoon.entity.PlotProfile;
import io.github.anjoismysign.blobtycoon.entity.TycoonPlayer;
import io.github.anjoismysign.blobtycoon.entity.configuration.PlotHelperConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlotHelperHit extends BlobTycoonListener {
    public PlotHelperHit(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        if (event.getEntityType() != PlotHelperConfiguration.getInstance().getEntityType())
            return;
        Player player = (Player) event.getDamager();
        Entity entity = event.getEntity();
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
            player.closeInventory();
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", player)
                    .handle(player);
            return;
        }
        PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
        if (!profile.getPlotHelper().getUniqueId().equals(entity.getUniqueId()))
            return;
        profile.openPlotHelperInventory(player);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getPlotHelperHit()
                .register()
                &&
                PlotHelperConfiguration.getInstance().isEnabled();
    }
}
