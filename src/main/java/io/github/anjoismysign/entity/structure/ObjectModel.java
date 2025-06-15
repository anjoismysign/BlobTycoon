package io.github.anjoismysign.entity.structure;

import io.github.anjoismysign.BlobTycoonInternalAPI;
import io.github.anjoismysign.anjo.entities.Tuple2;
import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.entities.Cuboid;
import io.github.anjoismysign.bloblib.entities.message.BlobSound;
import io.github.anjoismysign.bloblib.entities.translatable.TranslatableItem;
import io.github.anjoismysign.bloblib.utilities.PlayerUtil;
import io.github.anjoismysign.entity.PlotProfile;
import io.github.anjoismysign.entity.PlotProprietorProfile;
import io.github.anjoismysign.entity.TycoonKey;
import io.github.anjoismysign.entity.TycoonPlayer;
import io.github.anjoismysign.entity.asset.ObjectAsset;
import io.github.anjoismysign.entity.asset.RackAsset;
import io.github.anjoismysign.util.InteractionModel;
import io.github.anjoismysign.util.TycoonStructrador;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

/**
 * Represents a model of a single block size, used in StorageModels
 */
public interface ObjectModel extends TycoonModel {
    static boolean isObjectModel(@NotNull Interaction interaction) {
        Objects.requireNonNull(interaction, "'interaction' cannot be null!");
        PersistentDataContainer container = interaction.getPersistentDataContainer();
        if (!container.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
            return false;
        if (!container.has(TycoonKey.KEY.getKey(), PersistentDataType.STRING))
            return false;
        if (!container.has(TycoonKey.TYPE.getKey(), PersistentDataType.STRING))
            return false;
        return container.has(TycoonKey.FACING.getKey(), PersistentDataType.STRING);
    }

    static void remove(@NotNull Player player,
                       @NotNull Interaction interaction,
                       int delay) {
        Objects.requireNonNull(player, "'player' cannot be null!");
        Objects.requireNonNull(interaction, "'interaction' cannot be null!");
        PersistentDataContainer container = interaction.getPersistentDataContainer();
        String id = container.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
        String key = container.get(TycoonKey.KEY.getKey(), PersistentDataType.STRING);
        String type = container.get(TycoonKey.TYPE.getKey(), PersistentDataType.STRING);
        BlockFace facing = BlockFace.valueOf(container
                .get(TycoonKey.FACING.getKey(), PersistentDataType.STRING));
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                .getTycoonPlayer(player);
        PlotProprietorProfile profile = tycoonPlayer.getProfile();
        PlotProfile plotProfile = profile.getPlotProfile();
        if (!plotProfile.getPlot().getData()
                .isInside(interaction.getLocation()))
            return;
        if (plotProfile.isPlacingQueued()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Plot-Still-Loading-Hint", player)
                    .handle(player);
            return;
        }
        PrimitiveAsset objectType = PrimitiveAsset.fromType(type);
        if (objectType == null)
            throw new NullPointerException("PrimitivePlotObject is null at '" +
                    type + "' type! (might be outdated?)");
        TycoonModelHolder<ObjectModel> holder = BlobTycoonInternalAPI.getInstance()
                .getObjectHolder(objectType, key);
        if (holder == null)
            throw new NullPointerException("ObjectModel is null at '" +
                    objectType + "' type with '" + key +
                    "' getKey! (had assets been deleted?)");
        ObjectModel model = holder.getModel();
        InteractionModel.of(interaction).getEntities()
                .forEach(Entity::remove);
        UUID uuid = UUID.fromString(id);
        ItemFrame removeButton = plotProfile.getRemoveButton(uuid);
        if (removeButton == null)
            throw new NullPointerException("Remove button not found! (was structure cloned?)");
        String rackKey = removeButton.getPersistentDataContainer()
                .get(TycoonKey.KEY.getKey(), PersistentDataType.STRING);
        // it seems that the compiler cannot infer until the field is not null
        TycoonModelHolder<StorageModel> nullable = BlobTycoonInternalAPI.getInstance()
                .getStructureModelHolder(PrimitiveAsset.RACK, rackKey);
        if (nullable == null)
            throw new NullPointerException("RackAsset is null at '" +
                    rackKey + "' getKey! (had assets been deleted?)");
        RackAsset rackAsset = (RackAsset) nullable;
        int amount = TycoonKey.getStorageAvailability(removeButton);
        TycoonKey.setStorageAvailability(removeButton, amount + 1);
        Location pos = interaction.getLocation().clone();
        interaction.remove();
        model.placeItemFrame(pos,
                player, uuid, facing, rackAsset);
        BlobSound sound = model.getRemoveSound();
        if (sound != null)
            if (delay > 0)
                Bukkit.getScheduler().runTaskLater(model.getPlugin(),
                        () -> sound.handle(player, pos), delay);
            else
                sound.handle(player, pos);
        model.getWhenRemoved().accept(player);
        PlayerUtil.giveItemToInventoryOrDrop(player, holder.display(player));
    }

    static void place(@NotNull ItemFrame objectFrame,
                      @NotNull Player player) {
        Objects.requireNonNull(objectFrame, "'objectFrame' cannot be null!");
        Objects.requireNonNull(player, "'player' cannot be null!");
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance()
                .getTycoonPlayer(player);
        PlotProprietorProfile profile = tycoonPlayer.getProfile();
        if (!profile.getPlotProfile().getPlot().getData()
                .isInside(objectFrame.getLocation())) {
            return;
        }
        if (player.isSneaking()) {
            ItemFrame removeButton = StorageModel.getRemoveButton(objectFrame, player);
            if (removeButton == null)
                return;
            String removeId = removeButton.getPersistentDataContainer()
                    .get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
            Tuple2<StorageModel, Cuboid> measure = StorageModel.measure(removeButton);
            if (measure == null)
                return;
            Cuboid cuboid = measure.second();
            int max = Math.max(cuboid.getHeight(), (Math.max(cuboid.getXWidth(), cuboid.getZWidth())));
            Location center = cuboid.getCenter();
            ItemStack hand = player.getInventory().getItemInMainHand();
            int handAmount = hand.getAmount();
            TranslatableItem translatableItem = TranslatableItem.byItemStack(hand);
            if (translatableItem == null)
                return;
            int placed = 0;
            for (Entity entity : center.getWorld().getNearbyEntities(center, max, max, max)) {
                if (handAmount <= 0)
                    return;
                if (!entity.getPersistentDataContainer()
                        .has(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING)
                        || !entity.getPersistentDataContainer()
                        .get(TycoonKey.ITEM_FRAME_TYPE.getKey(), PersistentDataType.STRING)
                        .equals(ItemFrameType.OBJECT_SLOT.name()))
                    continue;
                String slotId = entity.getPersistentDataContainer()
                        .get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
                if (!removeId.equals(slotId))
                    continue;
                ItemFrame objectSlot = (ItemFrame) entity;
                placed++;
                placeSingle(objectSlot, player, translatableItem, hand, profile, placed);
                handAmount--;
            }
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        TranslatableItem translatableItem = TranslatableItem.byItemStack(hand);
        if (translatableItem == null)
            return;
        placeSingle(objectFrame, player, translatableItem, hand, profile, 0);
    }

    private static void placeSingle(@NotNull ItemFrame objectSlot,
                                    @NotNull Player player,
                                    @NotNull TranslatableItem translatableItem,
                                    @NotNull ItemStack hand,
                                    @NotNull PlotProprietorProfile profile,
                                    int delay) {
        Map<PrimitiveAsset, TycoonModelHolder<?>> map = BlobTycoonInternalAPI.getInstance()
                .isLinked(translatableItem);
        if (map.isEmpty())
            return;
        if (map.size() > 1)
            throw new IllegalStateException("More than one PrimitivePlotObject is linked to " +
                    translatableItem + "!");
        map.forEach((objectAsset, value) -> {
            if (objectAsset.isStructure())
                return;
            PersistentDataContainer itemFrameContainer = objectSlot.getPersistentDataContainer();
            if (!itemFrameContainer.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                return;
            PlotProfile plotProfile = profile.getPlotProfile();
            if (plotProfile.isPlacingQueued()) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Plot-Still-Loading-Hint", player)
                        .handle(player);
                return;
            }
            String key = itemFrameContainer.get(TycoonKey.KEY.getKey(), PersistentDataType.STRING);
            TycoonModelHolder<?> holder = BlobTycoonInternalAPI.getInstance()
                    .getStructureModelHolder(PrimitiveAsset.RACK, key);
            RackAsset rack = (RackAsset) holder;
            String id = itemFrameContainer.get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING);
            UUID uuid = UUID.fromString(id);
            BlockFace universal = BlockFace.valueOf(itemFrameContainer
                    .get(TycoonKey.FACING.getKey(), PersistentDataType.STRING));
            BlockFace facing = plotProfile.getPlot().getData().localize(universal);
            StructureRotation structureRotation;
            switch (facing) {
                case WEST -> structureRotation = StructureRotation.CLOCKWISE_180;
                case SOUTH -> structureRotation = StructureRotation.CLOCKWISE_90;
                case EAST -> structureRotation = StructureRotation.NONE;
                default -> structureRotation = StructureRotation.COUNTERCLOCKWISE_90;
            }
            ItemFrame button = plotProfile.getRemoveButton(uuid);
            if (button == null)
                throw new NullPointerException("Remove button not found! (was structure cloned?)");
            PersistentDataContainer buttonContainer = button.getPersistentDataContainer();
            if (!buttonContainer.has(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING))
                throw new NullPointerException("Remove button not found! (was structure cloned?)");
            UUID objectUUID = UUID.fromString(buttonContainer
                    .get(TycoonKey.OBJECT_ID.getKey(), PersistentDataType.STRING));
            ObjectModel model = (ObjectModel) value.getModel();
            ObjectAsset object = (ObjectAsset) value;
            if (!rack.isCompatible(object, object.getKey())) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("BlobTycoon.Storage-Not-Compatible",
                                player)
                        .handle(player);
                return;
            }
            int amount = TycoonKey.getStorageAvailability(button);
            Location pos = objectSlot.getLocation().getBlock().getLocation()
                    .add(0.5, 0.5, 0.5);
            Material emptyMaterial = BlobTycoonInternalAPI.getInstance().getEmptyMaterial();
            new TycoonStructrador(model.getStructure(), model.getPlugin())
                    .simultaneousPlace(pos,
                            true,
                            structureRotation,
                            Mirror.NONE,
                            0,
                            1,
                            new Random(), block -> {
                                if (block.getType() == Material.LIGHT)
                                    block.setType(emptyMaterial);
                            });
            Interaction interaction = InteractionModel.findInteraction(pos);
            if (interaction == null) {
                interaction = (Interaction) player.getWorld().spawnEntity(pos.getBlock()
                        .getLocation()
                        .add(0.5, 0, 0.5), EntityType.INTERACTION);
                interaction.setInteractionHeight(1.0f);
                interaction.setInteractionWidth(1.0f);
            }
            BlockVector offset = plotProfile.getPlot().getData()
                    .toOffset(interaction.getLocation()).toBlockVector();
            PersistentDataContainer interactionContainer = interaction.getPersistentDataContainer();
            interactionContainer.set(TycoonKey.OBJECT_ID.getKey(),
                    PersistentDataType.STRING, objectUUID.toString());
            interactionContainer.set(TycoonKey.TYPE.getKey(),
                    PersistentDataType.STRING, model.getType());
            interactionContainer.set(TycoonKey.KEY.getKey(),
                    PersistentDataType.STRING, model.getKey());
            interactionContainer.set(TycoonKey.FACING.getKey(),
                    PersistentDataType.STRING, universal.name());
            BlobSound sound = model.getPlaceSound();
            if (sound != null)
                if (delay > 0)
                    Bukkit.getScheduler().runTaskLater(model.getPlugin(),
                            () -> sound.handle(player, pos), delay);
                else
                    sound.handle(player, pos);
            model.getWhenPlaced().accept(player);
            objectSlot.remove();
            TycoonKey.setStorageAvailability(button, amount - 1);
            hand.setAmount(hand.getAmount() - 1);
        });
    }

    default void placeItemFrame(@NotNull Location location,
                                @NotNull Player player,
                                @NotNull UUID objectId,
                                @NotNull BlockFace universalFacing,
                                @NotNull RackAsset rackAsset) {
        Objects.requireNonNull(location);
        Objects.requireNonNull(player);
        Objects.requireNonNull(objectId);
        Objects.requireNonNull(universalFacing);
        Objects.requireNonNull(rackAsset);
        ItemFrame itemFrame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);
        Bukkit.getOnlinePlayers().forEach(onlinePlayer -> onlinePlayer.hideEntity(getPlugin(), itemFrame));
        itemFrame.setSilent(true);
        itemFrame.setVisible(false);
        itemFrame.setFacingDirection(BlockFace.UP);
        itemFrame.setItem(BlobTycoonInternalAPI.getInstance().getObjectSlot(player));
        PersistentDataContainer container = itemFrame.getPersistentDataContainer();
        container.set(TycoonKey.TYPE.getKey(),
                PersistentDataType.STRING, PrimitiveAsset.RACK.getType());
        container.set(TycoonKey.KEY.getKey(),
                PersistentDataType.STRING, rackAsset.getKey());
        container.set(TycoonKey.OBJECT_ID.getKey(),
                PersistentDataType.STRING, objectId.toString());
        container.set(TycoonKey.FACING.getKey(),
                PersistentDataType.STRING, universalFacing.name());
        container.set(TycoonKey.ITEM_FRAME_TYPE.getKey(),
                PersistentDataType.STRING, ItemFrameType.OBJECT_SLOT.name());
        Bukkit.getScheduler().runTaskLater(getPlugin(),
                () -> Bukkit.getOnlinePlayers().forEach(onlinePlayer ->
                        onlinePlayer.showEntity(getPlugin(), itemFrame)), 2L);
    }
}
