package io.github.anjoismysign.entity.structure;

import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.anjo.entities.Tuple2;
import io.github.anjoismysign.anjo.entities.Uber;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.Cuboid;
import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.bloblib.exception.ConfigurationFieldException;
import io.github.anjoismysign.entity.Plot;
import io.github.anjoismysign.entity.PlotExpansion;
import io.github.anjoismysign.entity.PlotObject;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.TycoonKey;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.asset.RackAsset;
import io.github.anjoismysign.entity.configuration.SelectionConfiguration;
import io.github.anjoismysign.entity.plotdata.PlotData;
import io.github.anjoismysign.entity.selection.Selection;
import io.github.anjoismysign.util.CuboidArea;
import io.github.anjoismysign.util.PlotDiscriminator;
import io.github.anjoismysign.util.TycoonStructrador;
import io.github.anjoismysign.util.Vectorator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public interface StorageModel extends StructureModel {
    static int READ(ConfigurationSection section) {
        if (!section.isInt("Max-Storage"))
            throw new ConfigurationFieldException("'Max-Storage' is not valid or set");
        return section.getInt("Max-Storage");
    }

    /**
     * Measures the storage model based on its remove button
     *
     * @param removeButton the remove button
     * @return the storage model and the area of the storage model
     */
    static Tuple2<StorageModel, Cuboid> measure(@NotNull ItemFrame removeButton) {
        Objects.requireNonNull(removeButton, "'removeButton' cannot be null");
        PersistentDataContainer container = removeButton.getPersistentDataContainer();
        if (!container.has(TycoonKey.TYPE.getKey(), PersistentDataType.STRING))
            return null;
        BlockFace facing = BlockFace.valueOf(container
                .get(TycoonKey.FACING.getKey(), PersistentDataType.STRING));
        String type = container.get(TycoonKey.TYPE.getKey(), PersistentDataType.STRING);
        if (!container.has(TycoonKey.KEY.getKey(), PersistentDataType.STRING))
            return null;
        String key = container.get(TycoonKey.KEY.getKey(), PersistentDataType.STRING);
        PrimitiveAsset primitive = PrimitiveAsset.fromType(type);
        if (primitive == null)
            return null;
        if (primitive != PrimitiveAsset.RACK)
            return null;
        TycoonModelHolder<?> holder = BlobTycoonInternalAPI.getInstance()
                .getStructureModelHolder(primitive, key);
        RackAsset rack = (RackAsset) holder;

        Location location = removeButton.getLocation();
        World world = location.getWorld();
        Location origin = location.clone();
        Vector removePivot = Vectorator.of(rack.getModel().getStructureData().getRemovePivot()).rotate(facing);
        Vector removeRelativeOffset = rack.getModel().getStructureData().getRemoveRelativeOffset() == null ?
                new Vector(0, 0, 0) :
                Vectorator.of(rack.getModel().getStructureData().getRemoveRelativeOffset()).rotate(facing);
        location.add(removePivot);
        origin.add(removeRelativeOffset);
        Cuboid cuboid = new Cuboid(location, origin);
        return new Tuple2<>(rack.getModel(), cuboid);
    }

    /**
     * Get the remove button from an object frame
     *
     * @param objectFrame the object frame
     * @param player      the player
     * @return the remove button
     */
    @Nullable
    static ItemFrame getRemoveButton(@NotNull ItemFrame objectFrame, @NotNull Player player) {
        Objects.requireNonNull(objectFrame, "'objectFrame' cannot be null");
        Objects.requireNonNull(player, "'player' cannot be null");
        PersistentDataContainer container = objectFrame.getPersistentDataContainer();
        if (!container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
            return null;
        String objectId = container.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                .getTycoonPlayer(player);
        if (tycoonPlayer == null)
            return null;
        PlotProprietorProfile proprietorProfile = tycoonPlayer.getProfile();
        if (proprietorProfile == null)
            return null;
        PlotProfile plotProfile = proprietorProfile.getPlotProfile();
        Collection<Entity> all = plotProfile.getPlot().getData().getAllEntities();
        for (Entity entity : all) {
            if (entity.getType() != EntityType.ITEM_FRAME)
                continue;
            ItemFrame removeButton = (ItemFrame) entity;
            if (removeButton.getItem() == null)
                continue;
            if (!removeButton.getPersistentDataContainer()
                    .has(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING)
                    || !removeButton.getPersistentDataContainer()
                    .get(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING)
                    .equals(ItemFrameType.REMOVE_BUTTON.name()))
                continue;
            PersistentDataContainer removeButtonContainer = removeButton.getPersistentDataContainer();
            if (!removeButtonContainer.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                continue;
            String removeButtonId = removeButtonContainer.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
            if (!removeButtonId.equals(objectId))
                continue;
            return removeButton;
        }
        return null;
    }

    default void serialize(ConfigurationSection section) {
        section.set("Max-Storage", getMaxStorage());
    }

    int getMaxStorage();

    @Override
    default boolean remove(@NotNull TycoonPlayer tycoonPlayer,
                           @NotNull Player player,
                           @NotNull ItemFrame itemFrame,
                           @NotNull BlockFace face,
                           @NotNull String objectId) {
        int availability = TycoonKey.getStorageAvailability(itemFrame);
        if (availability < getMaxStorage()) {
            if (player.isSneaking()) {
                Location location = itemFrame.getLocation();
                World world = location.getWorld();
                Location origin = location.clone();
                Vector removePivot = Vectorator.of(getStructureData().getRemovePivot()).rotate(face);
                Vector removeRelativeOffset = getStructureData().getRemoveRelativeOffset() == null ?
                        new Vector(0, 0, 0) :
                        Vectorator.of(getStructureData().getRemoveRelativeOffset()).rotate(face);
                location.add(removePivot);
                origin.add(removeRelativeOffset);
                Cuboid cuboid = new Cuboid(location, origin);
                int max = 0;
                if (max < cuboid.getHeight()) {
                    max = cuboid.getHeight();
                }
                if (max < cuboid.getXWidth()) {
                    max = cuboid.getXWidth();
                }
                if (max < cuboid.getZWidth()) {
                    max = cuboid.getZWidth();
                }
                Uber<Integer> removed = Uber.drive(0);
                cuboid.getCenter().getWorld().getNearbyEntities(cuboid.getCenter(),
                        max, max, max).forEach(entity -> {
                    if (entity.getType() != EntityType.INTERACTION)
                        return;
                    Interaction interaction = (Interaction) entity;
                    if (!ObjectModel.isObjectModel(interaction))
                        return;
                    String interactionId = interaction.getPersistentDataContainer()
                            .get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
                    if (!objectId.equals(interactionId))
                        return;
                    removed.talk(removed.thanks() + 1);
                    ObjectModel.remove(player, interaction, removed.thanks());
                });
                return false;
            }
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Storage-Supplied", player)
                    .handle(player);
            return false;
        }

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
        origin.add(removeRelativeOffset);
        Cuboid cuboid = new Cuboid(location, origin);
        Material emptyMaterial = BlobTycoonInternalAPI.getInstance().getEmptyMaterial();
        SelectionConfiguration selectionConfiguration = SelectionConfiguration.getInstance();
        Selection.of(cuboid, selectionConfiguration.getRemoveDistance())
                .addMax(1, 0, 1).timeVisualize(player, selectionConfiguration
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

    @Override
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
                    if (!checkBlock.isEmpty()) {
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
                if (entity.getType() != EntityType.ITEM_FRAME)
                    continue;
                ItemFrame itemFrame = (ItemFrame) entity;
                PersistentDataContainer container = itemFrame.getPersistentDataContainer();
                if (container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                    continue;
                Material itemType = itemFrame.getItem().getType();
                if (itemType == Material.STRUCTURE_VOID) {
                    container.set(TycoonKey.TYPE.getKey(), PersistentDataType.STRING, getType());
                    container.set(TycoonKey.KEY.getKey(), PersistentDataType.STRING, getKey());
                    container.set(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING, objectId.toString());
                    container.set(TycoonKey.FACING.getKey(), PersistentDataType.STRING, universal.name());
                    container.set(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING, ItemFrameType.OBJECT_SLOT.name());
                    itemFrame.setItem(BlobTycoonInternalAPI.getInstance().getObjectSlot(player));
                    continue;
                }
                if (itemType != Material.BARRIER)
                    continue;
                container.set(TycoonKey.TYPE.getKey(), PersistentDataType.STRING, getType());
                container.set(TycoonKey.KEY.getKey(), PersistentDataType.STRING, getKey());
                container.set(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING, objectId.toString());
                container.set(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING, ItemFrameType.REMOVE_BUTTON.name());
                container.set(TycoonKey.FACING.getKey(), PersistentDataType.STRING, universal.name());
                TycoonKey.setStorageAvailability(itemFrame, getMaxStorage());
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
