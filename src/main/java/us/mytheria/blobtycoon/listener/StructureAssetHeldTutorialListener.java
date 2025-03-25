package us.mytheria.blobtycoon.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.structure.PrimitiveAsset;

public class StructureAssetHeldTutorialListener extends BlobTycoonListener {

    public StructureAssetHeldTutorialListener(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        ItemStack hand = player.getInventory().getItem(event.getNewSlot());
        TranslatableItem translatableItem = TranslatableItem.byItemStack(hand);
        if (translatableItem == null)
            return;
        var map = BlobTycoonInternalAPI.getInstance().isLinked(translatableItem);
        if (map.size() > 1)
            throw new IllegalStateException("Item is linked to multiple assets: " + translatableItem);
        map.forEach((primitiveAsset, tycoonModelHolder) -> {
            if (primitiveAsset != PrimitiveAsset.STRUCTURE)
                return;
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.StructureAsset-Held-Tutorial", player)
                    .handle(player);
        });
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager().getStructureAssetHeldTutorial().register();
    }
}
