package us.mytheria.blobtycoon.entity.valuable.drivers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.valuable.ValuableDriver;
import us.mytheria.blobtycoon.util.TycoonUnit;

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
