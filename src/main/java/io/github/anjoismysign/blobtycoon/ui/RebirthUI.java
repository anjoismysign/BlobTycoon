package io.github.anjoismysign.blobtycoon.ui;

import net.milkbowl.vault.economy.IdentityEconomy;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.ReloadableUI;
import io.github.anjoismysign.bloblib.vault.multieconomy.ElasticEconomy;
import io.github.anjoismysign.blobtycoon.BlobTycoonInternalAPI;
import io.github.anjoismysign.blobtycoon.entity.PlotProfile;
import io.github.anjoismysign.blobtycoon.entity.TycoonPlayer;
import io.github.anjoismysign.blobtycoon.entity.configuration.CostIncreaseConfiguration;
import io.github.anjoismysign.blobtycoon.entity.configuration.RebirthConfiguration;

public class RebirthUI implements ReloadableUI {
    protected RebirthUI() {
    }

    @Override
    public void reload(@NotNull BlobLibInventoryAPI inventoryAPI) {
        var rebirthRegistry = inventoryAPI.getInventoryDataRegistry("Rebirth");
        rebirthRegistry.onClick("Rebirth", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null || tycoonPlayer.getProfile() == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            PlotProfile profile = tycoonPlayer.getProfile().getPlotProfile();
            int rebirths = profile.getRebirths();
            CostIncreaseConfiguration configuration = RebirthConfiguration.getInstance().getCostIncreaseConfiguration();
            double price = configuration.getCost(rebirths);
            String currency = configuration.getCostCurrency();
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            IdentityEconomy economy = elasticEconomy.getImplementation(currency);
            if (!economy.has(player, price)) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Withdraw.Insufficient-Balance", player)
                        .handle(player);
                return;
            }
            economy.withdrawPlayer(player, price);
            profile.rebirth();
        });
    }
}
