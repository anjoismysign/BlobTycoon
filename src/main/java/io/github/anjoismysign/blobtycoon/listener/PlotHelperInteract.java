package io.github.anjoismysign.blobtycoon.listener;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.blobtycoon.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobtycoon.director.manager.TycoonListenerManager;
import io.github.anjoismysign.blobtycoon.entity.PlotProfile;
import io.github.anjoismysign.blobtycoon.entity.TycoonPlayer;
import io.github.anjoismysign.blobtycoon.entity.configuration.PlotHelperConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlotHelperInteract extends BlobTycoonListener {
    public PlotHelperInteract(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getRightClicked().getType() != PlotHelperConfiguration.getInstance().getEntityType())
            return;
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
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
        // player is hitting their own plot helper
        profile.openPlotHelperInventory(player);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getPlotHelperInteract()
                .register()
                &&
                PlotHelperConfiguration.getInstance().isEnabled();
    }
}
