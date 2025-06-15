package io.github.anjoismysign.listener.structuremodel;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.selection.StructureModelSelector;
import io.github.anjoismysign.entity.structure.PrimitiveAsset;
import io.github.anjoismysign.entity.structure.StructureModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;
import io.github.anjoismysign.listener.BlobTycoonListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class StructureModelSelection extends BlobTycoonListener {
    private static StructureModelSelection instance;
    private final Map<String, StructureModelSelector> selectors;
    private final Set<UUID> removing;

    public static StructureModelSelection getInstance() {
        return instance;
    }

    public static StructureModelSelection getInstance(TycoonListenerManager listenerManager) {
        if (instance == null)
            instance = new StructureModelSelection(listenerManager);
        return instance;
    }

    private StructureModelSelection(TycoonListenerManager listenerManager) {
        super(listenerManager);
        selectors = new HashMap<>();
        removing = new HashSet<>();
        reload();
    }

    @EventHandler
    public void place(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.RIGHT_CLICK_AIR)
            return;
        Player player = event.getPlayer();
        StructureModelSelector selector = selectors.get(player.getName());
        if (selector == null)
            return;
        if (selector.getSelected() == null)
            return;
        if (selector.getCuboidArea() == null)
            return;
        TycoonModelHolder<StructureModel> holder = selector.getHolder();
        selector.stop();
        selectors.remove(player.getName());
        holder.getModel().place(selector.getSelected(),
                selector.getCuboidArea(),
                selector.getPos(),
                BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player),
                player.getInventory().getItemInMainHand());
    }

    @EventHandler
    public void onHeld(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID uuid = player.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player != Bukkit.getPlayer(uuid)) {
                    selectors.remove(playerName);
                    cancel();
                    return;
                }
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (removing.contains(player.getUniqueId()))
                    return;
                if (selectors.containsKey(playerName))
                    selectors.remove(playerName).stop();
                TranslatableItem translatableItem = TranslatableItem.byItemStack(hand);
                if (translatableItem == null) {
                    if (selectors.containsKey(playerName))
                        selectors.remove(playerName).stop();
                    return;
                }
                Map<PrimitiveAsset, TycoonModelHolder<?>> map = BlobTycoonInternalAPI.getInstance()
                        .isLinked(translatableItem);
                if (map.isEmpty()) {
                    if (selectors.containsKey(playerName))
                        selectors.remove(playerName).stop();
                    return;
                }
                if (map.size() > 1)
                    throw new IllegalStateException("More than one PrimitivePlotObject is linked to " +
                            translatableItem + "!");
                map.forEach((type, value) -> {
                    if (!type.isStructure()) {
                        if (selectors.containsKey(playerName))
                            selectors.remove(playerName).stop();
                        return;
                    }
                    TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
                    if (tycoonPlayer == null || tycoonPlayer.getProfile() == null)
                        return;
                    PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
                    if (plotProfile.isPlacingQueued()) {
                        if (selectors.containsKey(playerName))
                            selectors.remove(playerName).stop();
                        BlobLibMessageAPI.getInstance()
                                .getMessage("BlobTycoon.Plot-Still-Loading-Hint", player)
                                .handle(player);
                        return;
                    }
                    TycoonModelHolder<StructureModel> holder = (TycoonModelHolder<StructureModel>) value;
                    Block block = player.getTargetBlock(null, 5);
                    StructureModelSelector selector = StructureModelSelector.of(player, holder);
                    selectors.put(playerName, selector);
                });
            }
        }.runTaskTimer(getConfigManager().getPlugin(), 0, 1);
    }

    public boolean checkIfShouldRegister() {
        return true;
    }

    public void remove(Player player) {
        removing.add(player.getUniqueId());
        Bukkit.getScheduler().runTask(getConfigManager().getPlugin(), () -> {
            removing.remove(player.getUniqueId());
        });
    }
}
