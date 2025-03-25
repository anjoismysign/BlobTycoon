package us.mytheria.blobtycoon.entity.selection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.Cuboid;
import us.mytheria.blobtycoon.entity.configuration.SelectionConfiguration;
import us.mytheria.blobtycoon.exception.TycoonExceptionFactory;
import us.mytheria.blobtycoon.util.CuboidArea;
import us.mytheria.blobtycoon.util.Vectorator;

import java.util.Objects;
import java.util.UUID;

public class Selector {
    @NotNull
    private final UUID owner;
    @Nullable
    private Block selected;
    @Nullable
    private BlockVector size;
    @Nullable
    private BlockFace rotation;
    @Nullable
    private CuboidArea cuboidArea;
    @Nullable
    private Location pos;

    @Nullable
    private BukkitTask visualizer;

    @Nullable //NotNull if used Selector#of
    private BukkitTask checker;

    /**
     * Creates a new selector for the given player and structure holder.
     * Needs to update selected block and size before visualizing.
     * It's recommended updating through the setSize and setSelected methods,
     * it's not required to stop or visualize manually.
     *
     * @param player The player to create the selector for.
     * @return The new selector.
     */
    public static Selector of(@NotNull Player player) {
        Objects.requireNonNull(player);
        UUID id = player.getUniqueId();
        return new Selector(id);
    }

    private Selector(UUID owner) {
        this.owner = owner;
        this.rotation = BlockFace.NORTH;
    }

    @Nullable
    public Player getOwner() {
        return Bukkit.getPlayer(owner);
    }

    @NotNull
    public UUID getOwnerUniqueId() {
        return owner;
    }

    @Nullable
    public Block getSelected() {
        return selected;
    }

    public BlockFace getRotation() {
        return rotation;
    }

    public BlockVector getSize() {
        return size;
    }

    /**
     * Gets the cuboid getArea of the selector.
     * Should not be null if the selector has been visualized
     * at least once.
     *
     * @return The cuboid getArea of the selector.
     */
    @Nullable
    public CuboidArea getCuboidArea() {
        return cuboidArea;
    }

    /**
     * Gets the position of the selector.
     * Should not be null if the selector has been visualized
     * at least once.
     *
     * @return The position of the selector.
     */
    @Nullable
    public Location getPos() {
        return pos;
    }

    public void visualize() {
        if (getSelected() == null)
            return;
        if (getSize() == null)
            return;
        Player player = getOwner();
        if (player == null)
            return;
        BlockFace face = rotation;
        Location location = getSelected().getLocation();
        Location origin = location.clone();

        Vector vector = size;
        int rotation;
        switch (face) {
            case NORTH -> rotation = 0;
            case EAST -> {
                rotation = 270;
            }
            case SOUTH -> {
                rotation = 180;
            }
            case WEST -> {
                rotation = 90;
            }
            default -> throw TycoonExceptionFactory.getInstance()
                    .getInvalidCardinalDirectionExceptionFactory()
                    .notMain(face.name());
        }
        vector = Vectorator.of(vector).rotate(rotation);

        World world = location.getWorld();
        pos = location.clone();
        CuboidArea area = CuboidArea.of(location,
                new Vector(0, 0, 0), vector);
        this.cuboidArea = area;
        Cuboid cuboid = area.cuboid();
        SelectionConfiguration selectionConfiguration = SelectionConfiguration.getInstance();
        visualizer = Selection.of(cuboid, selectionConfiguration.getSelectorDistance())
                .visualize(player, selectionConfiguration.getSelectorParticle());
    }

    public void stop() {
        if (visualizer != null)
            visualizer.cancel();
        if (checker != null)
            checker.cancel();
    }

    public void setSize(BlockVector size) {
        BlockVector old = this.size;
        if (old == null)
            old = size;
        this.size = size;
        if (visualizer == null || visualizer.isCancelled()) {
            visualize();
            return;
        }
        if (old.equals(size))
            return;
        stop();
        visualize();
    }

    public void setSelected(@Nullable Block selected) {
        Block old = this.selected;
        if (old == null)
            old = selected;
        this.selected = selected;
        if (visualizer == null || visualizer.isCancelled()) {
            visualize();
            return;
        }
        if (old.getLocation().toVector().toBlockVector()
                .equals(selected.getLocation().toVector().toBlockVector()))
            return;
        stop();
        visualize();
    }

    public void setRotation(BlockFace rotation) {
        this.rotation = rotation;
    }
}
