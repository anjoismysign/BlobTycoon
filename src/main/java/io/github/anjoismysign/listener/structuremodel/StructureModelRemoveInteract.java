package io.github.anjoismysign.listener.structuremodel;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import io.github.anjoismysign.bloblib.api.BlobLibInventoryAPI;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.utilities.PlayerUtil;
import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.director.manager.TycoonListenerManager;
import io.github.anjoismysign.entity.ActionHolder;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.TycoonKey;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.structure.ItemFrameType;
import io.github.anjoismysign.entity.structure.PrimitiveAsset;
import io.github.anjoismysign.entity.structure.StructureModel;
import io.github.anjoismysign.entity.structure.TycoonModelHolder;
import io.github.anjoismysign.event.StructureModelRemoveEvent;
import io.github.anjoismysign.listener.BlobTycoonListener;

public class StructureModelRemoveInteract extends BlobTycoonListener {
    public StructureModelRemoveInteract(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onRemove(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        if (event.getRightClicked().getType() != EntityType.ITEM_FRAME)
            return;
        ItemFrame itemFrame = (ItemFrame) event.getRightClicked();
        PersistentDataContainer container = itemFrame.getPersistentDataContainer();
        if (!container.has(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING))
            return;
        ItemFrameType itemFrameType = ItemFrameType.valueOf(container.get(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING));
        if (itemFrameType != ItemFrameType.REMOVE_BUTTON)
            return;
        if (!container.has(TycoonKey.TYPE.getKey(), PersistentDataType.STRING))
            return;
        if (!container.has(TycoonKey.KEY.getKey(), PersistentDataType.STRING))
            return;
        if (!container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
            return;
        if (!container.has(TycoonKey.FACING.getKey(), PersistentDataType.STRING))
            return;
        event.setCancelled(true);
        String type = container.get(TycoonKey.TYPE.getKey(), PersistentDataType.STRING);
        String key = container.get(TycoonKey.KEY.getKey(), PersistentDataType.STRING);
        String id = container.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
        Player player = event.getPlayer();
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                .getTycoonPlayer(player);
        PlotProprietorProfile profile = tycoonPlayer.getProfile();
        PlotProfile plotProfile = profile.getPlotProfile();
        if (!plotProfile.getPlot().getData()
                .isInside(itemFrame.getLocation()))
            return;
        if (plotProfile.isPlacingQueued()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Plot-Still-Loading-Hint", player)
                    .handle(player);
            return;
        }
        BlockFace facing = BlockFace.valueOf(container
                .get(TycoonKey.FACING.getKey(), PersistentDataType.STRING));
        PrimitiveAsset plotObject = PrimitiveAsset.fromType(type);
        if (plotObject == null)
            throw new NullPointerException("PrimitivePlotObject is null at '" +
                    type + "' type! (might be outdated?)");
        TycoonModelHolder<StructureModel> holder = BlobTycoonInternalAPI.getInstance()
                .getStructureModelHolder(plotObject, key);
        if (holder == null)
            throw new NullPointerException("StructureModel is null at '" +
                    plotObject + "' type with '" + key +
                    "' getKey! (had assets been deleted?)");
        ActionHolder actionHolder = (ActionHolder) holder;
        boolean isActionHolderEnabled = actionHolder.isActionHolderEnabled();
        Runnable remove = () -> {
            StructureModelRemoveEvent removeEvent = new StructureModelRemoveEvent(tycoonPlayer,
                    holder, itemFrame.getLocation());
            Bukkit.getPluginManager().callEvent(removeEvent);
            if (removeEvent.isCancelled())
                return;
            if (!holder.getModel().remove(tycoonPlayer, player, itemFrame, facing, id))
                return;
            PlayerUtil.giveItemToInventoryOrDrop(player, holder.display(player));
            StructureModelSelection.getInstance().remove(player);
            tycoonPlayer.getRemoveStructure().talk(null);
            tycoonPlayer.getUseStructure().talk(null);
        };
        if (isActionHolderEnabled) {
            tycoonPlayer.getRemoveStructure().talk(remove);
            if (actionHolder.canProcess()) {
                tycoonPlayer.getUseStructure().talk(() ->
                        actionHolder.process(player));
                BlobLibInventoryAPI.getInstance().trackInventory(player, "Action-Holder-Allowed")
                        .getInventory().open(player);
            } else {
                BlobLibInventoryAPI.getInstance().trackInventory(player, "Action-Holder-Denied")
                        .getInventory().open(player);
            }
        } else {
            remove.run();
        }
    }

    public boolean checkIfShouldRegister() {
        return getConfigManager()
                .getStructureModelRemoveInteract()
                .register();
    }
}
