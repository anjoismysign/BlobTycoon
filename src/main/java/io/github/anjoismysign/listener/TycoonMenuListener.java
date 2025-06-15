package io.github.anjoismysign.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerLocaleChangeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.inventory.InventoryDataRegistry;
import io.github.anjoismysign.bloblib.entities.inventory.MetaInventoryButton;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.event.ProfileLoadEvent;

import java.util.Objects;
import java.util.UUID;

public class TycoonMenuListener extends BlobTycoonListener {
    private InventoryDataRegistry<MetaInventoryButton> registry;
    private int menuSlot;
    private TranslatableItem menu, selectPlot;

    public TycoonMenuListener(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @Override
    public void reload() {
        this.unregister();
        if (this.checkIfShouldRegister()) {
            boolean isPlayerInventory = getConfigManager().getTycoonMenu()
                    .getBoolean("Is-Player-Inventory");
            if (!isPlayerInventory)
                register();
            menuSlot = getConfigManager().getTycoonMenu().getInt("Slot");
            menu = TranslatableItem.by(getConfigManager().getTycoonMenu()
                    .getString("Menu-TranslatableItem"));
            selectPlot = TranslatableItem.by(getConfigManager().getTycoonMenu()
                    .getString("Select-Plot-TranslatableItem"));
            String key = getConfigManager().getTycoonMenu()
                    .getString("MetaBlobInventory");
            registry = BlobLibInventoryAPI.getInstance()
                    .getMetaInventoryDataRegistry(key);
            registry.onClick("My-Plot", clickEvent -> {
                Player player = (Player) clickEvent.getWhoClicked();
                TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                        .getTycoonPlayer(player.getUniqueId());
                Objects.requireNonNull(tycoonPlayer);
                PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
                player.closeInventory();
                player.teleport(plotProfile.getPlot().getData().getHomeLocation());
            });
            registry.onClick("Change-Plot", clickEvent -> {
                Player player = (Player) clickEvent.getWhoClicked();
                TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                        .getTycoonPlayer(player.getUniqueId());
                Objects.requireNonNull(tycoonPlayer);
                PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
                UUID uuid = player.getUniqueId();
                player.closeInventory();
                BlobLibInventoryAPI.getInstance().customSelector(
                        "Change-Plot",
                        player,
                        "Plots",
                        "Plots",
                        //The expansions supplier
                        plotProfile::getExpansions,
                        //Once clicked an expansion, will proceed to load
                        expansion -> {
                            if (player != Bukkit.getPlayer(uuid))
                                return;
                            int index = expansion.getIndex();
                            player.closeInventory();
                            if (plotProfile.getSelectedExpansionIndex() == index) {
                                BlobLibMessageAPI.getInstance()
                                        .getMessage("BlobTycoon.Plot-Already-Loaded",
                                                player)
                                        .handle(player);
                                return;
                            }
                            plotProfile.loadExpansion(index, () -> {
                            });
                        },
                        //Localizes the selectPlot translatable item
                        expansion -> selectPlot
                                .localize(player.getLocale())
                                .modder()
                                .replace("%n%", expansion.getIndex() + 1 + "")
                                .get()
                                .get(),
                        //Will open the main Tycoon Menu
                        returning -> {
                            BlobLibInventoryAPI.getInstance().trackMetaInventory(returning,
                                    registry.getKey()).getInventory().open(returning);
                        },
                        null,
                        null);
            });
        } else {
            registry = null;
        }
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager().getTycoonMenu().register();
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        TranslatableItem item = TranslatableItem.byItemStack(event.getCurrentItem());
        if (item == null)
            return;
        if (!item.identifier().equals(menu.identifier()))
            return;
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        event.setCursor(null);
        BlobLibInventoryAPI.getInstance().trackMetaInventory(player,
                registry.getKey()).getInventory().open(player);
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        TranslatableItem item = TranslatableItem.byItemStack(event.getOffHandItem());
        if (item == null)
            return;
        if (!item.identifier().equals(menu.identifier()))
            return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getAction() == Action.PHYSICAL)
            return;
        TranslatableItem translatableItem = TranslatableItem
                .byItemStack(event.getItem());
        if (translatableItem == null)
            return;
        if (!translatableItem.identifier().equals(menu.identifier()))
            return;
        Player player = event.getPlayer();
        event.setCancelled(true);
        BlobLibInventoryAPI.getInstance().trackMetaInventory(player,
                registry.getKey()).getInventory().open(player);
    }

    @EventHandler
    public void onLink(ProfileLoadEvent event) {
        TycoonPlayer tycoonPlayer = event.getTycoonPlayer();
        Player player = tycoonPlayer.getPlayer();
        player.getInventory().setItem(menuSlot,
                menu.localize(player.getLocale()).get());
    }

    @EventHandler
    public void onLocale(PlayerLocaleChangeEvent event) {
        Player player = event.getPlayer();
        player.getInventory().setItem(menuSlot,
                menu.localize(player.getLocale()).getClone());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        TranslatableItem item = TranslatableItem.byItemStack(event.getItemDrop().getItemStack());
        if (item == null)
            return;
        if (!item.identifier().equals(menu.identifier()))
            return;
        event.setCancelled(true);
    }
}
