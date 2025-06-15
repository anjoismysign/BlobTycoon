package io.github.anjoismysign.entity.valuable.drivers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.api.BlobLibTranslatableAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.utilities.PlayerUtil;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.valuable.ValuableDriver;
import io.github.anjoismysign.util.TycoonUnit;

import java.util.Objects;

public class BlobLibTranslatableItem implements ValuableDriver {
    private final BlobLibTranslatableAPI translatableAPI;

    public BlobLibTranslatableItem() {
        translatableAPI = BlobLibTranslatableAPI.getInstance();
    }

    public boolean supportsDeposits() {
        return false;
    }

    public boolean supportsWithdraws() {
        return true;
    }

    public boolean deposit(Player player, String currency, double amount) {
        return false;
    }

    public boolean withdraw(Player player, String currency, double amount) {
        if (amount < 1)
            return false;
        /*
         * even if amount is 6.9, truncatedAmount will be 6
         * this is because double to int casting rounds towards '0'
         */
        int truncatedAmount = (int) amount;
        if (truncatedAmount > 2240) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Transaction-Too-Big", player)
                    .handle(player);
            return false;
        }
        try {
            TranslatableItem translatableItem = translatableAPI
                    .getTranslatableItem(currency, player);
            if (translatableItem == null) {
                Bukkit.getPluginManager().getPlugin("BlobTycoon")
                        .getLogger().severe("'BlobLib-TranslatableItem' driver " +
                                "attempted to withdraw a non-existent TranslatableItem: " +
                                "'" + currency + "'");
                return false;
            }
            ItemStack itemStack = translatableItem.getClone();
            itemStack.setAmount(truncatedAmount);
            PlayerUtil.giveItemToInventoryOrDrop(player,
                    itemStack);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public ItemStack display(@NotNull Player player, @NotNull String currency) {
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null)
            Objects.requireNonNull(tycoonPlayer);
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        long balance = (long) plotProfile.getValuable(currency);
        ItemStack base = TranslatableItem.by("BlobTycoon.Vault-MultiEconomy-Display")
                .localize(player.getLocale())
                .modder()
                .replace("%currency%", currency)
                .replace("%balance%", TycoonUnit.THOUSANDS_SEPARATOR.format(balance))
                .get()
                .getClone();
        ItemMeta baseMeta = base.getItemMeta();
        Objects.requireNonNull(baseMeta);
        ItemStack clone = TranslatableItem.by(currency)
                .getClone();
        ItemMeta itemMeta = clone.getItemMeta();
        Objects.requireNonNull(itemMeta);
        itemMeta.setDisplayName(baseMeta.getDisplayName());
        itemMeta.setLore(baseMeta.getLore());
        clone.setItemMeta(itemMeta);
        return clone;
    }
}
