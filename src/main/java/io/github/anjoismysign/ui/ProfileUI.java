package io.github.anjoismysign.ui;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.ReloadableUI;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.entity.TycoonPlayer;

public class ProfileUI implements ReloadableUI {
    protected ProfileUI() {
    }

    @Override
    public void reload(@NotNull BlobLibInventoryAPI inventoryAPI) {
        var profileCreateRegistry = inventoryAPI.getInventoryDataRegistry("BlobTycoon-New-Profile");
        profileCreateRegistry.onClick("Create", inventoryClickEvent -> {
            Player player = (Player) inventoryClickEvent.getWhoClicked();
            TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
            if (tycoonPlayer == null) {
                player.closeInventory();
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                return;
            }
            player.closeInventory();
            tycoonPlayer.createProfile(HumanEntity::closeInventory);
        });
    }
}
