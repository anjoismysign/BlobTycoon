package us.mytheria.blobtycoon.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import us.mytheria.bloblib.entities.translatable.TranslatableItem;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.event.ProfileLoadEvent;

public class TranslateOnProfileLoad extends BlobTycoonListener {
    public TranslateOnProfileLoad(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onLoad(ProfileLoadEvent event) {
        Player player = event.getTycoonPlayer().getPlayer();
        String locale = player.getLocale();
        for (ItemStack stack : player.getInventory().getContents()) {
            TranslatableItem.localize(stack, locale);
        }
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getTranslateOnProfileLoad()
                .register();
    }
}
