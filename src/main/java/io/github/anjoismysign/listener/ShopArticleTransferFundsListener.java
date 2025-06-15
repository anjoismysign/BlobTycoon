package io.github.anjoismysign.listener;

import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.blobrp.events.ShopArticleSaleFailEvent;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;

import java.util.Optional;

public class ShopArticleTransferFundsListener extends BlobTycoonListener {

    public ShopArticleTransferFundsListener(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    public void reload() {
        HandlerList.unregisterAll(this);
        if (getConfigManager().getShopArticleTransferFunds().register())
            Bukkit.getPluginManager().registerEvents(this, getConfigManager().getPlugin());
    }

    @Override
    public boolean checkIfShouldRegister() {
        return false;
    }

    @EventHandler
    public void onSaleFail(ShopArticleSaleFailEvent event) {
        if (event.isFixed())
            return;
        Player player = event.getPlayer();
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
            return;
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        String currency = event.getCurrency() == null ? "default" : event.getCurrency();
        double amount = event.getAmount();
        if (!plotProfile.withdrawValuable(currency, amount))
            return;
        IdentityEconomy economy = BlobLibEconomyAPI.getInstance().getElasticEconomy().map(Optional.ofNullable(currency));
        economy.depositPlayer(player, amount);
        event.setFixed(true);
    }
}
