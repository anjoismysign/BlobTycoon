package io.github.anjoismysign.entity;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibListenerAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.itemstack.ItemStackBuilder;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.BlobTycoonValuableAPI;
import io.github.anjoismysign.entity.valuable.ValuableDriver;

import java.util.Objects;
import java.util.Set;

public class ValuableTeller {
    private static ValuableTeller instance;
    private BlobLibInventoryAPI inventoryAPI;

    public static ValuableTeller getInstance() {
        if (instance == null) {
            instance = new ValuableTeller();
        }
        return instance;
    }

    private ValuableTeller() {
        inventoryAPI = BlobLibInventoryAPI.getInstance();
    }

    public void withdraw(Player player) {
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null) {
            CommandSender sender = Bukkit.getConsoleSender();
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", sender)
                    .toCommandSender(sender);
            return;
        }
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        inventoryAPI.customSelector("Teller-Withdraw",
                player,
                "Valuables",
                "Valuables",
                () -> plotProfile.getValuables().keySet().stream()
                        .filter(valuable -> plotProfile.getValuable(valuable) > 0)
                        .toList(),
                valuable -> {
                    if (BlobTycoonValuableAPI.getInstance()
                            .getLinkedDriver(valuable) == null) {
                        player.closeInventory();
                        BlobLibMessageAPI.getInstance()
                                .getMessage("BlobTycoon.Not-A-Valuable", player)
                                .modder()
                                .replace("%o%", valuable)
                                .get()
                                .handle(player);
                        return;
                    }
                    player.closeInventory();
                    BlobLibListenerAPI.getInstance()
                            .addChatListener(player, 300,
                                    input -> {
                                        TycoonPlayer manage = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
                                        Objects.requireNonNull(manage, "add player#isValid && player#isOnline to BlobLib listeners");
                                        PlotProfile managePlot = manage.getProfile().getPlotProfile();
                                        try {
                                            double parsed = Double.parseDouble(input);
                                            if (!managePlot.hasValuableAmount(valuable, parsed)) {
                                                BlobLibMessageAPI.getInstance()
                                                        .getMessage("Withdraw.Insufficient-Balance", player)
                                                        .handle(player);
                                                return;
                                            }
                                            managePlot.withdrawValuable(valuable, parsed);
                                            BlobTycoonValuableAPI.getInstance()
                                                    .getLinkedDriver(valuable)
                                                    .withdraw(player, valuable, parsed);
                                            withdraw(player);
                                        } catch (NumberFormatException exception) {
                                            Set<String> allKeywords = BlobTycoonInternalAPI.getInstance().getAllKeywords();
                                            Set<String> halfKeywords = BlobTycoonInternalAPI.getInstance().getHalfKeywords();
                                            if (allKeywords.contains(input)) {
                                                double amount = managePlot.getValuable(valuable);
                                                if (!managePlot.hasValuableAmount(valuable, amount)) {
                                                    BlobLibMessageAPI.getInstance()
                                                            .getMessage("Withdraw.Insufficient-Balance", player)
                                                            .handle(player);
                                                    return;
                                                }
                                                managePlot.withdrawValuable(valuable, amount);
                                                BlobTycoonValuableAPI.getInstance()
                                                        .getLinkedDriver(valuable)
                                                        .withdraw(player, valuable, amount);
                                                withdraw(player);
                                            } else if (halfKeywords.contains(input)) {
                                                double amount = managePlot.getValuable(valuable) / 2;
                                                if (!managePlot.hasValuableAmount(valuable, amount)) {
                                                    BlobLibMessageAPI.getInstance()
                                                            .getMessage("Withdraw.Insufficient-Balance", player)
                                                            .handle(player);
                                                    return;
                                                }
                                                managePlot.withdrawValuable(valuable, amount);
                                                BlobTycoonValuableAPI.getInstance()
                                                        .getLinkedDriver(valuable)
                                                        .withdraw(player, valuable, amount);
                                                withdraw(player);
                                            } else {
                                                BlobLibMessageAPI.getInstance()
                                                        .getMessage("Builder.Number-Exception", player)
                                                        .handle(player);
                                            }
                                        }
                                    },
                                    "Withdraw.Amount-Timeout",
                                    "Withdraw.Amount");

                },
                valuable -> {
                    ValuableDriver valuableDriver = BlobTycoonValuableAPI.getInstance()
                            .getLinkedDriver(valuable);
                    if (valuableDriver != null)
                        return valuableDriver.display(player, valuable);
                    return ItemStackBuilder.build(Material.BARRIER)
                            .displayName("&cInvalid")
                            .lore("&7Not a valuable: &f" + valuable)
                            .build();
                },
                null,
                null,
                null);
    }
}
