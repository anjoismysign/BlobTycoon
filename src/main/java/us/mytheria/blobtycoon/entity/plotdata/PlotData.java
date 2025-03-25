package us.mytheria.blobtycoon.entity.plotdata;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.BlobObject;
import us.mytheria.bloblib.entities.Cuboid;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.bloblib.utilities.VectorUtil;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.StructureDirection;
import us.mytheria.blobtycoon.util.PlotDiscriminator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Objects;

public record PlotData(String getIndex,
                       BlockVector getMinPointPosition,
                       Vector getHomePosition,
                       BoundingBox getBoundingBox,
                       BlockVector getSize,
                       StructureDirection getDirection,
                       String getWorldName,
                       BlockVector getPivotPosition)
        implements BlobObject {

    public String getKey() {
        return getIndex;
    }

    /**
     * Writes the plot to a file.
     *
     * @param directory the directory to write to
     * @return
     */
    public File saveToFile(@NotNull File directory) {
        Objects.requireNonNull(directory);
        if (!directory.isDirectory())
            throw new IllegalArgumentException("'" + directory.getPath() + "' is not a directory");
        File file = new File(directory, getIndex + ".yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection minPointSection = configuration.createSection("MinPoint");
        VectorUtil.toConfigurationSection(getMinPointPosition, minPointSection);
        minPointSection.set("World", getWorldName);
        ConfigurationSection sizeSection = configuration.createSection("Size");
        VectorUtil.toConfigurationSection(getSize, sizeSection);
        configuration.set("Direction", getDirection.name());
        ConfigurationSection homeSection = configuration.createSection("Home");
        VectorUtil.toConfigurationSection(getHomePosition, homeSection);

        try {
            configuration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @NotNull
    public static PlotData fromFile(@NotNull File file) {
        Objects.requireNonNull(file);
        String index = file.getName().replace(".yml", "");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        if (!configuration.isConfigurationSection("MinPoint"))
            throw new ConfigurationFieldException("'MinPoint' does not exist");
        ConfigurationSection minPointSection = configuration.getConfigurationSection("MinPoint");
        String worldName = minPointSection.getString("World");
        Vector vector = VectorUtil.fromConfigurationSection(minPointSection);
        BlockVector minPoint = vector.toBlockVector();
        ConfigurationSection sizeSection = configuration.getConfigurationSection("Size");
        BlockVector size = VectorUtil.fromConfigurationSection(sizeSection).toBlockVector();
        StructureDirection direction = StructureDirection.valueOf(configuration.getString("Direction"));
        ConfigurationSection homeSection = configuration.getConfigurationSection("Home");
        Vector home = VectorUtil.fromConfigurationSection(homeSection);
        BlockVector pivotPosition;
        switch (direction) {
            case SOUTH -> {
                pivotPosition = minPoint.clone().add(new Vector(size
                        .getBlockX(), 0, size.getBlockZ())).toBlockVector();
            }
            case EAST -> {
                home = new Vector(-home.getX(),
                        home.getY(), -home.getZ());
                pivotPosition = minPoint.clone().add(new Vector(size
                        .getBlockX(), 0, 0)).toBlockVector();
            }
            case WEST -> {
                home = new Vector(-home.getX(),
                        home.getY(), -home.getZ());
                pivotPosition = minPoint.clone().add(new Vector(0,
                        0, size.getBlockZ())).toBlockVector();
            }
            case NORTH -> {
                pivotPosition = minPoint;
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + direction);
            }
        }
        return new PlotData(index, minPoint, home,
                BoundingBox.of(minPoint, minPoint.clone().add(size)), size,
                direction, worldName, pivotPosition);
    }

    /**
     * Should create getHomePosition in front of the plot,
     * facing in front of the plot and backwards the "street",
     * assuming the floor is at y=-1.
     *
     * @param index    the getIndex of the plot
     * @param minPoint the first point of the plot
     * @param size     the getSize of the plot
     * @param home     the home of the plot
     * @return the plot
     */
    public static PlotData of(@NotNull String index,
                              @NotNull Location minPoint,
                              @NotNull BlockVector size,
                              @NotNull StructureDirection direction,
                              @Nullable BlockVector home) {
        World world = minPoint.getWorld();
        Objects.requireNonNull(world);
        if (home == null) {
            home = new BlockVector(size.getX() / 2, 1, -0.5);
        }
        BlockVector pivotPosition = minPoint.toVector().toBlockVector();
        return new PlotData(index, minPoint.toVector().toBlockVector(), home,
                BoundingBox.of(minPoint, minPoint.clone().add(size)), size,
                direction, world.getName(), pivotPosition);
    }

    private World getWorld() {
        World world = Bukkit.getWorld(getWorldName);
        if (world == null)
            throw new NullPointerException("World '" + getWorldName + "' does not exist");
        return world;
    }

    /**
     * Gets the pivot of the plot, used for offsetting.
     *
     * @return the pivot
     */
    @NotNull
    public Location getPivot() {
        World world = getWorld();
        return getPivotPosition.toLocation(world);
    }

    /**
     * Gets the minimum point of the plot.
     *
     * @return the minimum point
     */
    @NotNull
    public Location getMinPoint() {
        World world = getWorld();
        return getMinPointPosition.toLocation(world);
    }

    @NotNull
    public Location getHomeLocation() {
        Location home = fromOffset(getHomePosition, false);
        home.setYaw(getDirection.getYaw());
        return home;
    }

    /**
     * Saves the current state in a Structure and returns it.
     *
     * @param includeEntities whether to include entities in the structure
     * @return the structure
     */
    @NotNull
    public Structure saveStructure(boolean includeEntities) {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("Must be called on the main thread");
        Structure structure = Bukkit.getStructureManager().createStructure();
        Vector max = getMinPointPosition().clone().add(getSize());
        structure.fill(getMinPoint(), getSize, includeEntities);
        return structure;
    }

    /**
     * @return the center of the plot
     */
    @NotNull
    public Location getCenter() {
        return getBoundingBox.getCenter().toLocation(getMinPoint().getWorld());
    }

    /**
     * @return the maximum size of the plot
     */
    public int getMax() {
        return Math.max(getSize.getBlockX(), Math.max(getSize.getBlockY(), getSize.getBlockZ()));
    }

    /**
     * @return all entities in the plot
     */
    public Collection<Entity> getAllEntities() {
        Location center = getCenter();
        int max = getMax();
        return center.getWorld().getNearbyEntities(center, max, max, max);
    }

    /**
     * Will remove all entities in the plot.
     */
    public void removeEntities() {
        Collection<Entity> nearby = getAllEntities();
        if (nearby.isEmpty())
            return;
        for (Entity entity : nearby) {
            if (PlotDiscriminator.dontRemove(entity))
                continue;
            if (!isInside(entity.getLocation()))
                continue;
            entity.remove();
        }
    }

    /**
     * Will get a Cuboid instance of the plot.
     *
     * @return the cuboid
     */
    public Cuboid toCuboid() {
        World world = getMinPoint().getWorld();
        Objects.requireNonNull(world);
        return toCuboid(world);
    }

    /**
     * Will get a Cuboid instance of the plot.
     *
     * @param world the getWorld to get the cuboid in
     * @return the cuboid
     */
    public Cuboid toCuboid(World world) {
        return new Cuboid(getBoundingBox.getMin().toLocation(world),
                getBoundingBox.getMax().toLocation(world));
    }

    /**
     * Checks if PlotLoader is done
     *
     * @return true if PlotLoader is done
     */
    public boolean plotLoaderIsDone() {
        return BlobTycoonInternalAPI.getInstance().plotLoaderIsDone(getIndex);
    }

    public boolean isInside(Location location) {
        return getBoundingBox.contains(location.toVector());
    }

    public Location fromOffset(@NotNull Vector offset) {
        return fromOffset(offset, true);
    }

    public Location fromOffset(@NotNull Vector offset,
                               boolean fix) {
        Objects.requireNonNull(offset, "'offset' cannot be null");
        float yaw = 0;
        StructureDirection direction = getDirection;
        if (fix) {
            if (direction == StructureDirection.EAST || direction == StructureDirection.WEST)
                offset = new Vector(-offset.getX(),
                        offset.getY(), -offset.getZ());
            yaw += direction.getYaw();
        }

        Vector localized = localizeVector(offset);
        Location location = getPivot().clone().add(localized);
        location.setYaw(yaw);
        return location;
    }

    public Vector toOffset(@NotNull Location location) {
        Objects.requireNonNull(location, "'location' cannot be null");
        return universalizeVector(location.toVector().subtract(getPivot().toVector()));
    }

    /**
     * Will rotate said BlockFace clockwise if needed to be localized.
     *
     * @param face the face to localize
     * @return the localized face
     */
    public BlockFace localize(BlockFace face) {
        switch (getDirection) {
            case NORTH -> {
                return face;
            }
            case EAST -> {
                switch (face) {
                    case NORTH -> {
                        return BlockFace.EAST;
                    }
                    case EAST -> {
                        return BlockFace.SOUTH;
                    }
                    case SOUTH -> {
                        return BlockFace.WEST;
                    }
                    case WEST -> {
                        return BlockFace.NORTH;
                    }
                    default -> {
                        throw new IllegalStateException("Unexpected value: " + face);
                    }
                }
            }
            case SOUTH -> {
                switch (face) {
                    case NORTH -> {
                        return BlockFace.SOUTH;
                    }
                    case EAST -> {
                        return BlockFace.WEST;
                    }
                    case SOUTH -> {
                        return BlockFace.NORTH;
                    }
                    case WEST -> {
                        return BlockFace.EAST;
                    }
                    default -> {
                        throw new IllegalStateException("Unexpected value: " + face);
                    }
                }
            }
            case WEST -> {
                switch (face) {
                    case NORTH -> {
                        return BlockFace.WEST;
                    }
                    case EAST -> {
                        return BlockFace.NORTH;
                    }
                    case SOUTH -> {
                        return BlockFace.EAST;
                    }
                    case WEST -> {
                        return BlockFace.SOUTH;
                    }
                    default -> {
                        throw new IllegalStateException("Unexpected value: " + face);
                    }
                }
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + getDirection);
            }
        }
    }

    /**
     * Will rotate said BlockFace counter-clockwise
     * if needed to be universalized.
     *
     * @param face the face to universalize
     * @return the universalized face
     */
    public BlockFace universalize(BlockFace face) {
        switch (getDirection) {
            case NORTH -> {
                return face;
            }
            case EAST -> {
                switch (face) {
                    case NORTH -> {
                        return BlockFace.WEST;
                    }
                    case EAST -> {
                        return BlockFace.NORTH;
                    }
                    case SOUTH -> {
                        return BlockFace.EAST;
                    }
                    case WEST -> {
                        return BlockFace.SOUTH;
                    }
                    default -> {
                        throw new IllegalStateException("Unexpected value: " + face);
                    }
                }
            }
            case SOUTH -> {
                switch (face) {
                    case NORTH -> {
                        return BlockFace.SOUTH;
                    }
                    case EAST -> {
                        return BlockFace.WEST;
                    }
                    case SOUTH -> {
                        return BlockFace.NORTH;
                    }
                    case WEST -> {
                        return BlockFace.EAST;
                    }
                    default -> {
                        throw new IllegalStateException("Unexpected value: " + face);
                    }
                }
            }
            case WEST -> {
                switch (face) {
                    case NORTH -> {
                        return BlockFace.EAST;
                    }
                    case EAST -> {
                        return BlockFace.SOUTH;
                    }
                    case SOUTH -> {
                        return BlockFace.WEST;
                    }
                    case WEST -> {
                        return BlockFace.NORTH;
                    }
                    default -> {
                        throw new IllegalStateException("Unexpected value: " + face);
                    }
                }
            }
            default -> {
                throw new IllegalStateException("Unexpected value: " + getDirection);
            }
        }
    }

    private Vector universalizeVector(Vector original) {
        return switch (getDirection) {
            case EAST -> new Vector(-original.getZ(), original.getY(), original.getX());
            case SOUTH -> new Vector(-original.getX(), original.getY(), -original.getZ());
            case WEST -> new Vector(original.getZ(), original.getY(), -original.getX());
            case NORTH -> original;
        };
    }

    private Vector localizeVector(Vector universal) {
        return switch (getDirection) {
            case EAST -> new Vector(universal.getZ(), universal.getY(), -universal.getX());
            case SOUTH -> new Vector(-universal.getX(), universal.getY(), -universal.getZ());
            case WEST -> new Vector(-universal.getZ(), universal.getY(), universal.getX());
            case NORTH -> universal;
        };
    }

}
