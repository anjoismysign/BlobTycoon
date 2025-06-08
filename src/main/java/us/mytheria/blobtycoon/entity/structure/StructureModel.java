package us.mytheria.blobtycoon.entity.structure;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.Cuboid;
import us.mytheria.bloblib.entities.message.BlobSound;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.Plot;
import us.mytheria.blobtycoon.entity.PlotExpansion;
import us.mytheria.blobtycoon.entity.PlotObject;
import us.mytheria.blobtycoon.entity.PlotProfile;
import us.mytheria.blobtycoon.entity.PlotProprietorProfile;
import us.mytheria.blobtycoon.entity.StructureData;
import us.mytheria.blobtycoon.entity.TycoonKey;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.entity.configuration.SelectionConfiguration;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;
import us.mytheria.blobtycoon.entity.selection.Selection;
import us.mytheria.blobtycoon.exception.TycoonExceptionFactory;
import us.mytheria.blobtycoon.util.CuboidArea;
import us.mytheria.blobtycoon.util.PlotDiscriminator;
import us.mytheria.blobtycoon.util.TycoonStructrador;
import us.mytheria.blobtycoon.util.Vectorator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public interface StructureModel extends TycoonModel {
    private static TycoonExceptionFactory getExceptionFactory() {
        return TycoonExceptionFactory.getInstance();
    }

    @NotNull
    StructureData getStructureData();

    /**
     * Will remove the structure from the plot.
     *
     * @param tycoonPlayer The player that's removing the structure.
     * @param player       The player that's removing the structure.
     * @param itemFrame    The item frame that's being removed.
     * @param face         The face of the item frame.
     * @param objectId     The object id of the item frame.
     * @return True if the structure was removed, false otherwise.
     */
    default boolean remove(@NotNull TycoonPlayer tycoonPlayer,
                           @NotNull Player player,
                           @NotNull ItemFrame itemFrame,
                           @NotNull BlockFace face,
                           @NotNull String objectId) {
        PlotProprietorProfile profile = tycoonPlayer.getProfile();
        PlotProfile plotProfile = profile.getPlotProfile();
        Plot plot = plotProfile.getPlot();
        PlotData plotData = plot.getData();
        PlotExpansion expansion = plotProfile.getCurrentExpansion();
        face = plotData.localize(face);

        Location location = itemFrame.getLocation();
        World world = location.getWorld();
        Location origin = location.clone();
        Vector removePivot = Vectorator.of(getStructureData().getRemovePivot()).rotate(face);
        Vector removeRelativeOffset = getStructureData().getRemoveRelativeOffset() == null ?
                new Vector(0, 0, 0) :
                Vectorator.of(getStructureData().getRemoveRelativeOffset()).rotate(face);
        location.add(removePivot);
        location.add(removeRelativeOffset);
        origin.add(removeRelativeOffset);
        Cuboid cuboid = new Cuboid(location, origin);
        Material emptyMaterial = BlobTycoonInternalAPI.getInstance().getEmptyMaterial();
        SelectionConfiguration selectionConfiguration = SelectionConfiguration.getInstance();
        Selection.of(cuboid, selectionConfiguration.getRemoveDistance())
                .addMax(1, 1, 1).timeVisualize(player, selectionConfiguration
                        .getRemoveParticle(), selectionConfiguration.getRemoveDuration());
        cuboid.forEachBlock(block -> block.setType(emptyMaterial, false));
        for (Entity entity : cuboid.getEntities()) {
            if (PlotDiscriminator.dontRemove(entity))
                continue;
            if (entity.getType() == EntityType.ITEM_FRAME) {
                ItemFrame itemFrame1 = (ItemFrame) entity;
                PersistentDataContainer container = itemFrame1.getPersistentDataContainer();
                if (!container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                    continue;
                String id = container.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
                if (!id.equals(objectId))
                    continue;
            }
            entity.remove();
        }
        getWhenRemoved().accept(player);
        BlobSound removeSound = getRemoveSound();
        if (removeSound != null)
            removeSound.handle(player, location);
        expansion.removeObject(UUID.fromString(objectId));
        return true;
    }

    /**
     * Will place the structure in the plot.
     *
     * @param at           The block to place the structure at.
     * @param area         The area to place the structure in.
     * @param pos          The position to place the structure at.
     * @param tycoonPlayer The player that's placing the structure.
     * @param hand         The item in the player's hand.
     */
    default void place(Block at, CuboidArea area, Location pos, TycoonPlayer tycoonPlayer,
                       ItemStack hand) {
        PlotProprietorProfile proprietorProfile = tycoonPlayer.getProfile();
        if (proprietorProfile == null) {
            return;
        }
        PlotProfile plotProfile = proprietorProfile.getPlotProfile();
        if (plotProfile.isPlacingQueued())
            return;
        Plot plot = plotProfile.getPlot();
        PlotData plotData = plot.getData();
        Player player = tycoonPlayer.getPlayer();
        if (!plotData.isInside(at.getLocation())) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Not-Inside-Plot", player)
                    .handle(player);
            return;
        }
        PlotExpansion expansion = plotProfile.getCurrentExpansion();
        BlockFace face = player.getFacing();
        BlockFace universal = plotData.universalize(face);
        Cuboid cuboid = area.cuboid();
        Location loc1 = area.getLoc1();
        Location loc2 = area.getLoc2();
        Location center = cuboid.getCenter().clone();
        int x1 = loc1.getBlockX();
        int x2 = loc2.getBlockX();
        int floor = loc1.getBlockY();
        int ceiling = loc2.getBlockY();
        int z1 = loc1.getBlockZ();
        int z2 = loc2.getBlockZ();
        int doPaste = 0;
        BlobTycoonInternalAPI api = BlobTycoonInternalAPI.getInstance();
        outerLoop:
        for (int x = x1; x < x2; x++) {
            for (int y = floor; y < ceiling; y++) {
                for (int z = z1; z < z2; z++) {
                    Block checkBlock = at.getWorld().getBlockAt(x, y, z);
                    if (!checkBlock.isEmpty() || checkBlock.getType() != Material.AIR) {
                        Material type = checkBlock.getType();
                        if (!api.isValidFloor(checkBlock.getType())
                                && !type.isAir()
                                && type != Material.LIGHT) {
                            doPaste++;
                            break outerLoop;
                        }
                    }
                }
            }
        }
        if (doPaste == 0) {
            StructureRotation structureRotation;
            switch (face) {
                case WEST -> structureRotation = StructureRotation.CLOCKWISE_90;
                case SOUTH -> structureRotation = StructureRotation.NONE;
                case EAST -> structureRotation = StructureRotation.COUNTERCLOCKWISE_90;
                default -> structureRotation = StructureRotation.CLOCKWISE_180;
            }
            Set<BlockVector> set = new HashSet<>();
            UUID objectId = plotProfile.generateObjectId();
            Material emptyMaterial = BlobTycoonInternalAPI.getInstance().getEmptyMaterial();
            new TycoonStructrador(getStructure(), getPlugin())
                    .simultaneousPlace(pos.add(0, 1, 0),
                            true,
                            structureRotation,
                            Mirror.NONE,
                            0,
                            1,
                            new Random(), block -> {
                                if (block.getType() == Material.LIGHT)
                                    block.setType(emptyMaterial);
                            });
            hand.setAmount(hand.getAmount() - 1);
            getWhenPlaced().accept(player);
            BlobSound placeSound = getPlaceSound();
            if (placeSound != null)
                placeSound.handle(player, pos);
            Location removeButtonLocation = null;
            for (Entity entity : cuboid.getEntities()) {
                if (!(entity instanceof ItemFrame itemFrame)) {
                    continue;
                }
                PersistentDataContainer container = itemFrame.getPersistentDataContainer();
                if (container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                    continue;
                Material itemType = itemFrame.getItem().getType();
                if (itemType != Material.BARRIER)
                    continue;
                container.set(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING, ItemFrameType.REMOVE_BUTTON.name());
                container.set(TycoonKey.FACING.getKey(), PersistentDataType.STRING, universal.name());
                container.set(TycoonKey.TYPE.getKey(), PersistentDataType.STRING, getType());
                container.set(TycoonKey.KEY.getKey(), PersistentDataType.STRING, getKey());
                container.set(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING, objectId.toString());
                removeButtonLocation = itemFrame.getLocation();
                itemFrame.setItem(BlobTycoonInternalAPI.getInstance().getRemove(player));
            }
            BlockVector offset = plotData.toOffset(removeButtonLocation)
                    .toBlockVector();
            PlotObject plotObject = new PlotObject(objectId, offset);
            expansion.addObject(plotObject);
        } else {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Structure-No-Space", player)
                    .handle(player);
        }
    }
}
