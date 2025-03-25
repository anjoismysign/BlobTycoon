package us.mytheria.blobtycoon.listener;

import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.event.TradeSaleFailEvent;

public class InternalTransferFunds extends BlobTycoonListener {

    public InternalTransferFunds(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onTrade(TradeSaleFailEvent event) {
        if (event.isFixed())
            return;
        TycoonPlayer tycoonPlayer = event.getTycoonPlayer();
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            return;
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        String currency = event.getCurrency();
        double amount = event.getAmount();
        if (!plotProfile.withdrawValuable(currency, amount))
            return;
        Player player = tycoonPlayer.getPlayer();
        IdentityEconomy economy = BlobLibEconomyAPI.getInstance().getElasticEconomy().getImplementation(currency);
        economy.depositPlayer(player, amount);
        event.setFixed(true);
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager().getInternalTransferFunds().register();
    }
}
