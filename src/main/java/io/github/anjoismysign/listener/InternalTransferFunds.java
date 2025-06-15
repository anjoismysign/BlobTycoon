package io.github.anjoismysign.listener;

import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.event.TradeSaleFailEvent;

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
