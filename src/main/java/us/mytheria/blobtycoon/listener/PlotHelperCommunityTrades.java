package us.mytheria.blobtycoon.listener;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.configuration.PlotHelperConfiguration;

public class PlotHelperCommunityTrades extends BlobTycoonListener {

    public PlotHelperCommunityTrades(TycoonListenerManager listenerManager) {
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
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        PlotProfile find = BlobTycoonInternalAPI.getInstance().isPlotHelper(entity);
        if (find == null)
            return;
        if (plotProfile.getPlotHelper().getUniqueId().equals(entity.getUniqueId()))
            return;
        BlobTycoonInternalAPI.getInstance().openCommunityTrade(player, find);
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
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        PlotProfile find = BlobTycoonInternalAPI.getInstance().isPlotHelper(entity);
        if (find == null)
            return;
        if (plotProfile.getPlotHelper().getUniqueId().equals(entity.getUniqueId()))
            return;
        BlobTycoonInternalAPI.getInstance().openCommunityTrade(player, find);
    }

    public boolean checkIfShouldRegister() {
        return PlotHelperConfiguration.getInstance().getMerchantConfiguration().enableCommunityTrades();
    }
}
