package us.mytheria.blobtycoon.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.entities.inventory.BlobInventory;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.PlotProprietorProfile;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.event.ProfileLoadEvent;

public class NewProfileKit extends BlobTycoonListener {
    public NewProfileKit(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onLoad(ProfileLoadEvent event) {
        TycoonPlayer tycoonPlayer = event.getTycoonPlayer();
        PlotProprietorProfile profile = tycoonPlayer.getProfile();
        PlotProfile plotProfile = profile.getPlotProfile();
        if (!plotProfile.isFresh())
            return;
        plotProfile.setFresh(false);
        Player player = tycoonPlayer.getPlayer();
        BlobInventory blobInventory = BlobLibInventoryAPI.getInstance()
                .getBlobInventory("New-Profile-Kit", player);
        ItemStack[] contents = blobInventory.getInventory().getContents();
        int length = contents.length;
        for (int i = 0; i < length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null)
                continue;
            int amount = itemStack.getAmount();
            TranslatableItem translatableItem = TranslatableItem.byItemStack(itemStack);
            if (translatableItem != null)
                itemStack = translatableItem.localize(player).getClone();
            itemStack.setAmount(amount);
            tycoonPlayer.getPlayer().getInventory().setItem(i, itemStack);
        }

    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getNewProfileKit()
                .register();
    }
}
