package us.mytheria.blobtycoon.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import us.mytheria.bloblib.entities.Cuboid;

public record CuboidArea(Location getLoc1,
                         Location getLoc2,
                         Cuboid cuboid) {

    public static CuboidArea of(Location location, Vector pos1, Vector pos2) {
        int floor = location.getBlockY();
        int x1 = location.getBlockX() + pos1.getBlockX();
        int y1 = location.getBlockY() + pos1.getBlockY();
        int z1 = location.getBlockZ() + pos1.getBlockZ();
        int x2 = x1 + pos2.getBlockX();
        int y2 = y1 + pos2.getBlockY();
        int z2 = z1 + pos2.getBlockZ();
        Location min = new Location(location.getWorld(), x1, y1, z1);
        Location max = new Location(location.getWorld(), x2, y2, z2);
        Location loc1 = Vector.getMinimum(min.toVector(), max.toVector())
                .toLocation(location.getWorld());
        Location loc2 = Vector.getMaximum(min.toVector(), max.toVector())
                .toLocation(location.getWorld());
        Cuboid area = new Cuboid(loc1, loc2);

        return new CuboidArea(loc1, loc2, area);
    }
}
