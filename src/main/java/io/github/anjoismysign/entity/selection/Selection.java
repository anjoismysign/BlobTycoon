package io.github.anjoismysign.entity.selection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.Cuboid;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Selection(@NotNull World getWorld,
                        @NotNull BlockVector getMin,
                        @NotNull BlockVector getMax,
                        @NotNull List<Location> getEdges,
                        @NotNull double getDistance) {

    /**
     * Will get a Selection from two locations.
     *
     * @param loc1     The first location
     * @param loc2     The second location
     * @param distance The getDistance between each particle
     * @return The selection
     */
    public static Selection of(@NotNull Location loc1,
                               @NotNull Location loc2,
                               double distance) {
        Objects.requireNonNull(loc1);
        Objects.requireNonNull(loc2);
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same getWorld");
        }
        BlockVector pos1 = loc1.toVector().toBlockVector();
        BlockVector pos2 = loc2.toVector().toBlockVector();
        BlockVector min = Vector.getMinimum(pos1, pos2).toBlockVector();
        BlockVector max = Vector.getMaximum(pos1, pos2).toBlockVector();
        List<Location> edges = coverEdges(distance, loc1.getWorld(), min, max);
        return new Selection(loc1.getWorld(), min, max, edges, distance);
    }

    /**
     * Will get a Selection from a Cuboid instance.
     *
     * @param cuboid   The cuboid instance
     * @param distance The getDistance between each particle
     * @return The selection
     */
    public static Selection of(@NotNull Cuboid cuboid,
                               double distance) {
        Objects.requireNonNull(cuboid);
        Location point1 = cuboid.getPoint1();
        Location point2 = cuboid.getPoint2();
        return of(point1, point2, distance);
    }

    private static List<Location> coverEdges(double distance,
                                             World world,
                                             BlockVector min,
                                             BlockVector max) {
        List<Location> result = new ArrayList<>();
        int minX = min.getBlockX();
        int minY = min.getBlockY();
        int minZ = min.getBlockZ();
        int maxX = max.getBlockX();
        int maxY = max.getBlockY();
        int maxZ = max.getBlockZ();

        for (double x = minX; x <= maxX; x += distance) {
            for (double y = minY; y <= maxY; y += distance) {
                for (double z = minZ; z <= maxZ; z += distance) {
                    int components = 0;
                    if (x == minX || x == maxX) components++;
                    if (y == minY || y == maxY) components++;
                    if (z == minZ || z == maxZ) components++;
                    if (components >= 2) {
                        result.add(new Location(world, x, y, z));
                    }
                }
            }
        }
        return result;
    }

    private void forEach(Consumer<Block> consumer) {
        int minX = getMin.getBlockX();
        int minY = getMin.getBlockY();
        int minZ = getMin.getBlockZ();
        int maxX = getMax.getBlockX();
        int maxY = getMax.getBlockY();
        int maxZ = getMax.getBlockZ();

        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++)
                    consumer.accept(getWorld.getBlockAt(x, y, z));
            }
        }
    }

    /**
     * Will add a certain amount to the maximum point of the selection.
     *
     * @param x The amount to add to the getX coordinate
     * @param y The amount to add to the y coordinate
     * @param z The amount to add to the getZ coordinate
     * @return The new selection
     */
    @NotNull
    public Selection addMax(int x, int y, int z) {
        BlockVector newMax = getMax.clone().add(new Vector(x, y, z))
                .toBlockVector();
        Location loc1 = getMin.toLocation(getWorld);
        Location loc2 = newMax.toLocation(getWorld);
        return of(loc1, loc2, getDistance);
    }

    /**
     * Will add a certain amount to the minimum point of the selection.
     *
     * @param x The amount to add to the getX coordinate
     * @param y The amount to add to the y coordinate
     * @param z The amount to add to the getZ coordinate
     * @return The new selection
     */
    @NotNull
    public Selection addMin(int x, int y, int z) {
        BlockVector newMin = getMin.clone().add(new Vector(x, y, z))
                .toBlockVector();
        Location loc1 = newMin.toLocation(getWorld);
        Location loc2 = getMax.toLocation(getWorld);
        return of(loc1, loc2, getDistance);
    }

    /**
     * Will visualize the selection for a player for a certain duration.
     *
     * @param player   The player to visualize for
     * @param particle The particle to visualize with
     * @param duration The duration to visualize for
     */
    public void timeVisualize(@Nullable Player player, @NotNull Particle particle, long duration) {
        if (duration < 7)
            return;
        BukkitTask visualizer = visualize(player, particle);
        Bukkit.getScheduler().runTaskLaterAsynchronously(Bukkit.getPluginManager()
                .getPlugin("BlobTycoon"), visualizer::cancel, duration);
    }

    /**
     * Will visualize the selection for a player indefinitely.
     *
     * @param player   The player to visualize for
     * @param particle The particle to visualize with
     * @return The task, so it can be cancelled
     */
    public BukkitTask visualize(@Nullable Player player, @NotNull Particle particle) {
        BukkitTask task;
        if (player != null)
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    getEdges.forEach(location -> player
                            .spawnParticle(particle, location, 1,
                                    0, 0, 0, 0));
                }
            }.runTaskTimerAsynchronously(Bukkit.getPluginManager().getPlugin("BlobTycoon"), 0, 2);
        else
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    getEdges.forEach(location -> getWorld
                            .spawnParticle(particle, location, 1,
                                    0, 0, 0, 0));
                }
            }.runTaskTimer(Bukkit.getPluginManager().getPlugin("BlobTycoon"), 0, 2);
        return task;
    }
}
