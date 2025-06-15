package io.github.anjoismysign.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import io.github.anjoismysign.bloblib.entities.inventory.BlobInventory;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.event.ProfileLoadEvent;

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
        BlobInventory inventory = BlobInventory.ofKeyOrThrow("New-Profile-Kit", player.getLocale());
        ItemStack[] contents = inventory.getInventory().getContents();
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
