package us.mytheria.blobtycoon.listener.structuremodel;

import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import us.mytheria.bloblib.api.BlobLibInventoryAPI;
import us.mytheria.bloblib.utilities.PlayerUtil;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.manager.TycoonListenerManager;
import us.mytheria.blobtycoon.entity.ActionHolder;
import us.mytheria.blobtycoon.entity.PlotProprietorProfile;
import us.mytheria.blobtycoon.entity.TycoonKey;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.structure.ItemFrameType;
import us.mytheria.blobtycoon.entity.structure.PrimitiveAsset;
import us.mytheria.blobtycoon.entity.structure.StructureModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;
import us.mytheria.blobtycoon.event.StructureModelRemoveEvent;
import us.mytheria.blobtycoon.listener.BlobTycoonListener;

public class StructureModelRemoveHit extends BlobTycoonListener {
    public StructureModelRemoveHit(TycoonListenerManager listenerManager) {
        super(listenerManager);
        reload();
    }

    @EventHandler
    public void onRemove(EntityDamageByEntityEvent event) {
        if (event.getDamager().getType() != EntityType.PLAYER)
            return;
        if (event.getEntityType() != EntityType.ITEM_FRAME)
            return;
        ItemFrame itemFrame = (ItemFrame) event.getEntity();
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
        Player player = (Player) event.getDamager();
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                .getTycoonPlayer(player);
        PlotProprietorProfile profile = tycoonPlayer.getProfile();
        if (!profile.getPlotProfile().getPlot().getData()
                .isInside(itemFrame.getLocation()))
            return;
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
                .getStructureModelRemoveHit()
                .register();
    }
}
