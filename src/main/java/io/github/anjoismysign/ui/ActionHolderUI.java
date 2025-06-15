package io.github.anjoismysign.ui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.entities.ReloadableUI;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.entity.TycoonPlayer;

import java.util.Objects;

public class ActionHolderUI implements ReloadableUI {
    protected ActionHolderUI() {
    }

    @Override
    public void reload(@NotNull BlobLibInventoryAPI inventoryAPI) {
        var actionHolderAllowed = inventoryAPI.getInventoryDataRegistry("Action-Holder-Allowed");
        actionHolderAllowed.onClick("Remove", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            Objects.requireNonNull(tycoonPlayer, "tycoonPlayer is null!");
            Runnable runnable = tycoonPlayer.getRemoveStructure().thanks();
            Objects.requireNonNull(runnable, "runnable is null!");
            player.closeInventory();
            runnable.run();
        });
        actionHolderAllowed.onClick("Use", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            Objects.requireNonNull(tycoonPlayer, "tycoonPlayer is null!");
            Runnable runnable = tycoonPlayer.getUseStructure().thanks();
            Objects.requireNonNull(runnable, "runnable is null!");
            player.closeInventory();
            runnable.run();
        });

        var actionHolderDenied = inventoryAPI.getInventoryDataRegistry("Action-Holder-Denied");
        actionHolderDenied.onClick("Remove", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            Objects.requireNonNull(tycoonPlayer, "tycoonPlayer is null!");
            Runnable runnable = tycoonPlayer.getRemoveStructure().thanks();
            Objects.requireNonNull(runnable, "runnable is null!");
            player.closeInventory();
            runnable.run();
        });
    }
}
