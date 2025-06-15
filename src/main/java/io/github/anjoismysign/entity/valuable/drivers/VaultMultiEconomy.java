package io.github.anjoismysign.entity.valuable.drivers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibEconomyAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.valuable.ValuableDriver;
import io.github.anjoismysign.util.TycoonUnit;

import java.util.Objects;

public class VaultMultiEconomy implements ValuableDriver {
    private final BlobLibEconomyAPI economyAPI;

    public VaultMultiEconomy() {
        economyAPI = BlobLibEconomyAPI.getInstance();
    }

    public boolean supportsDeposits() {
        return true;
    }

    public boolean supportsWithdraws() {
        return true;
    }

    public boolean deposit(@NotNull Player player, @NotNull String currency, double amount) {
        try {
            return economyAPI.getElasticEconomy().getImplementation(currency)
                    .withdrawPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean withdraw(@NotNull Player player, @NotNull String currency, double amount) {
        try {
            return economyAPI.getElasticEconomy().getImplementation(currency)
                    .depositPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            return false;
        }
    }

    public ItemStack display(@NotNull Player player, @NotNull String currency) {
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null)
            Objects.requireNonNull(tycoonPlayer);
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        return TranslatableItem.by("BlobTycoon.Vault-MultiEconomy-Display")
                .localize(player.getLocale())
                .modder()
                .replace("%currency%", currency)
                .replace("%balance%", TycoonUnit.THOUSANDS_SEPARATOR.format(plotProfile.getValuable(currency)))
                .get()
                .getClone();
    }
}
