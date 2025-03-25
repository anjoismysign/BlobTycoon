package us.mytheria.blobtycoon.ui;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.entities.ReloadableUI;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.TycoonPlayer;

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
