package us.mytheria.blobtycoon.listener;

import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.blobrp.events.ShopArticleSaleFailEvent;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;

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
