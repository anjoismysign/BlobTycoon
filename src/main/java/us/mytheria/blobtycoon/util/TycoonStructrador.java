package us.mytheria.blobtycoon.util;

import me.anjoismysign.anjo.entities.Uber;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.structure.Palette;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.ChainedTask;
import us.mytheria.bloblib.entities.ChainedTaskProgress;
import us.mytheria.bloblib.utilities.BlockFaceUtil;
import us.mytheria.bloblib.utilities.Structrador;
import us.mytheria.bloblib.utilities.VectorUtil;

import java.io.File;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TycoonStructrador extends Structrador {
    public TycoonStructrador(Structure structure, JavaPlugin plugin) {
        super(structure, plugin);
    }

    public TycoonStructrador(File file, JavaPlugin plugin) {
        super(file, plugin);
    }

    public TycoonStructrador(InputStream inputStream, JavaPlugin plugin) {
        super(inputStream, plugin);
    }

    public TycoonStructrador(byte[] bytes, JavaPlugin plugin) {
        super(bytes, plugin);
    }

    public TycoonStructrador(Blob blob, JavaPlugin plugin) {
        super(blob, plugin);
    }

    @Override
    @NotNull
    public ChainedTask chainedPlace(Location location, boolean includeEntities,
                                    StructureRotation structureRotation, Mirror mirror,
                                    int palette, float integrity, Random random,
                                    int maxPlacedPerPeriod, long period,
                                    Consumer<BlockState> placedBlockConsumer,
                                    Consumer<Entity> placedEntityConsumer) {
        /*
         * The location of the returned block states and entities
         * are offsets relative to the structure's position that
         * is provided once the structure is placed into the world.
         */
        CompletableFuture<Void> future = new CompletableFuture<>();
        List<Palette> palettes = structure.getPalettes();
        List<BlockState> blocks = palettes.stream().map(Palette::getBlocks)
                .flatMap(List::stream).toList();
        List<Entity> entities = structure.getEntities();
        int blocksSize = blocks.size();
        int entitiesSize = entities.size();
        int totalSize = blocksSize + entitiesSize;
        ChainedTask chainedTask = new ChainedTask(future, null,
                (totalSize / maxPlacedPerPeriod) * period);
        ChainedTaskProgress progress = new ChainedTaskProgress(totalSize, chainedTask);
        Iterator<BlockState> blockIterator = blocks.iterator();
        World world = location.getWorld();
        if (world == null)
            throw new IllegalArgumentException("Location must have a world.");
        int degree;
        Vector blockOffset;
        Vector entityOffset;
        BlockVector size = structure.getSize();
        float extraYaw;
        switch (structureRotation) {
            case NONE -> {
                degree = 0;
                blockOffset = new Vector(0, 0, 0);
                entityOffset = new Vector(0, 0, 0);
                extraYaw = 0;
            }
            case CLOCKWISE_90 -> {
                degree = 270;
                blockOffset = new Vector(size.getX() - 1, 0, 0);
                entityOffset = new Vector(size.getX(), 0, 0);
                extraYaw = 90;
            }
            case CLOCKWISE_180 -> {
                degree = 180;
                blockOffset = new Vector(size.getX() - 1, 0, size.getZ() - 1);
                entityOffset = new Vector(size.getX(), 0, size.getZ());
                extraYaw = 180;
            }
            case COUNTERCLOCKWISE_90 -> {
                degree = 90;
                blockOffset = new Vector(0, 0, size.getZ() - 1);
                entityOffset = new Vector(0, 0, size.getZ());
                extraYaw = 270;
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + structureRotation);
            }
        }
        try {
            // Will place blocks
            CompletableFuture<Void> paletteFuture = new CompletableFuture<>();
            BukkitRunnable placeTask = new BukkitRunnable() {
                @Override
                public void run() {
                    Uber<Integer> placed = Uber.drive(0);
                    while (blockIterator.hasNext() && placed.thanks() < maxPlacedPerPeriod) {
                        BlockState next = blockIterator.next();
                        Vector nextVector = next.getLocation().toVector();
                        Vector result = VectorUtil.rotateVector(nextVector, degree);
                        Location blockLocation = location.clone()
                                .add(result.getX() + blockOffset.getX(),
                                        result.getY() + blockOffset.getY(),
                                        result.getZ() + blockOffset.getZ());
                        next = next.copy(blockLocation);
                        next.update(true, false);
                        BlockState current = blockLocation.getBlock().getState();
                        BlockData data = current.getBlockData();
                        if (data instanceof Directional directional) {
                            directional.setFacing(BlockFaceUtil
                                    .rotateCardinalDirection(directional.getFacing(),
                                            structureRotation));
                            current.setBlockData(directional);
                            current.update(true, false);
                        }
                        placedBlockConsumer.accept(current);
                        placed.talk(placed.thanks() + 1);
                        progress.run();
                    }
                    if (!blockIterator.hasNext()) {
                        paletteFuture.complete(null);
                        this.cancel();
                    }
                }
            };
            chainedTask.setTask(placeTask.runTaskTimer(plugin, 1L, period));
            //Once blocks are placed, will place entities
            paletteFuture.whenComplete((aVoid, throwable) -> {
                if (!includeEntities) {
                    future.complete(null);
                    return;
                }
                Iterator<Entity> entityIterator = entities.iterator();
                BukkitRunnable entityTask = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Uber<Integer> placed = Uber.drive(0);
                        while (entityIterator.hasNext() && placed.thanks() < maxPlacedPerPeriod) {
                            Entity next = entityIterator.next();
                            Location nextLocation = next.getLocation();
                            Vector nextVector = nextLocation.toVector();
                            Vector result = VectorUtil.floatRotateVector(nextVector, degree);
                            Location entityLocation = location.clone()
                                    .add(result.getX() + entityOffset.getX(),
                                            result.getY() + entityOffset.getY(),
                                            result.getZ() + entityOffset.getZ());
                            entityLocation.setYaw(nextLocation.getYaw() + extraYaw);
                            boolean isSilent = next.isSilent();
                            next.setSilent(true);
                            Entity added = world.addEntity(next);
                            added.teleport(entityLocation);
                            if (added instanceof Hanging hanging) {
                                BlockFace facing = hanging.getFacing();
                                hanging.setFacingDirection(
                                        BlockFaceUtil.rotateCardinalDirection(facing, structureRotation),
                                        true);
                            }
                            added.setSilent(isSilent);
                            placedEntityConsumer.accept(added);
                            placed.talk(placed.thanks() + 1);
                            progress.run();
                        }
                        if (!entityIterator.hasNext()) {
                            future.complete(null);
                            this.cancel();
                        }
                    }
                };
                chainedTask.setTask(entityTask.runTaskTimer(plugin, 1L, period));
            });
        } catch (Throwable e) {
            future.completeExceptionally(e);
            e.printStackTrace();
        }
        return chainedTask;
    }
}
