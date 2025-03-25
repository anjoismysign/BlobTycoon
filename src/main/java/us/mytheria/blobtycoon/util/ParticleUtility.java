package us.mytheria.blobtycoon.util;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ParticleUtility {
    private static List<Location> cover(Block block,
                                        double distance) {
        Location location = block.getLocation();
        List<Location> result = new ArrayList<>();
        World world = location.getWorld();
        double minX = location.getBlockX();
        double minY = location.getBlockY();
        double minZ = location.getBlockZ();
        double maxX = location.getBlockX() + 1;
        double maxY = location.getBlockY() + 1;
        double maxZ = location.getBlockZ() + 1;

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

    /**
     * Will spawn END_ROD particles around the block
     * using 0.5 as the getDistance between each particle.
     *
     * @param block The block to spawn particles around
     */
    public static void aroundBlock(Block block) {
        aroundBlock(block, 0.5, Particle.FLAME);
    }

    /**
     * Will spawn particles around the block
     *
     * @param block    The block to spawn particles around
     * @param distance The getDistance between each particle
     */
    public static void aroundBlock(Block block, double distance, Particle particle) {
        List<Location> result = cover(block, distance);
        World world = block.getWorld();
        result.forEach(loc -> world.spawnParticle(particle, loc, 1,
                0, 0, 0, 0));
    }

    /**
     * Will spawn particles around the block.
     * Will only spawn particles for the player.
     * Can be called asynchronously.
     *
     * @param block    The block to spawn particles around
     * @param distance The getDistance between each particle
     * @param particle The particle to spawn
     * @param player   The player to spawn particles for
     */
    public static void aroundBlock(Block block,
                                   double distance,
                                   Particle particle,
                                   Player player) {
        List<Location> result = cover(block, distance);
        result.forEach(loc -> player.spawnParticle(particle, loc, 1,
                0, 0, 0, 0));
    }
}
