package us.mytheria.blobtycoon.entity.selection;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.Cuboid;
import us.mytheria.bloblib.utilities.PlayerUtil;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.*;
import us.mytheria.blobtycoon.entity.configuration.SelectionConfiguration;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;
import us.mytheria.blobtycoon.entity.structure.StructureModel;
import us.mytheria.blobtycoon.entity.structure.TycoonModelHolder;
import us.mytheria.blobtycoon.exception.TycoonExceptionFactory;
import us.mytheria.blobtycoon.util.CuboidArea;
import us.mytheria.blobtycoon.util.Vectorator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class StructureModelSelector {
    @NotNull
    private final UUID owner;
    @Nullable
    private Block selected;
    @Nullable
    private CuboidArea cuboidArea;
    @Nullable
    private Location pos;

    @NotNull
    private final TycoonModelHolder<StructureModel> holder;

    @Nullable
    private BukkitTask visualizer;

    @Nullable //NotNull if used Selector#of
    private BukkitTask checker;

    /**
     * Creates a new selector for the given player and structure holder.
     * Needs to update selected block before visualizing.
     *
     * @param player The player to create the selector for.
     * @param holder The structure holder to create the selector for.
     * @return The new selector.
     */
    public static StructureModelSelector of(@NotNull Player player,
                                            @NotNull TycoonModelHolder<StructureModel> holder) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(holder);
        UUID id = player.getUniqueId();
        StructureModelSelector selector = new StructureModelSelector(id, holder);
        Material emptyMaterial = BlobTycoonInternalAPI.getInstance().getEmptyMaterial();
        Set<Material> transparent = new HashSet<>();
        transparent.add(emptyMaterial);
        transparent.add(Material.AIR);
        transparent.add(Material.LIGHT);
        BukkitTask checker = new BukkitRunnable() {
            @Override
            public void run() {
                Player owner = Bukkit.getPlayer(id);
                if (owner == null) {
                    cancel();
                    return;
                }
                Block adjacentBlock = PlayerUtil.getAdjacentBlock(player, transparent);
                if (adjacentBlock == null) {
                    selector.selected = player.getLocation().getBlock();
                    return;
                }
                if (selector.selected == null) {
                    selector.selected = adjacentBlock;
                    selector.visualize();
                    return;
                }
                if (selector.visualizer == null)
                    return;
                else {
                    if (adjacentBlock.getLocation().toVector().toBlockVector()
                            .equals(selector.selected.getLocation()
                                    .toVector().toBlockVector())) {
                        return;
                    }
                    selector.visualizer.cancel();
                    selector.selected = adjacentBlock;
                    selector.visualize();
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("BlobTycoon"), 0, 1);
        selector.checker = checker;
        return selector;
    }

    private StructureModelSelector(UUID owner, TycoonModelHolder<StructureModel> holder) {
        this.owner = owner;
        this.holder = holder;
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
        Player player = getOwner();
        if (player != null) {
            BlockFace face = player.getFacing();
            StructureData structureData = getHolder().getModel().getStructureData();
            Vector selectorOffset = structureData.getSelectorOffset() == null ?
                    new Vector() : structureData.getSelectorOffset();
            selectorOffset = Vectorator.of(selectorOffset).rotate(face);
            return selected == null ? null : selected.getLocation().add(selectorOffset).getBlock();
        }
        return selected;
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
        Player player = getOwner();
        if (player == null)
            return;
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null)
            return;
        PlotProprietorProfile proprietorProfile = tycoonPlayer.getProfile();
        if (proprietorProfile == null) {
            return;
        }
        PlotProfile plotProfile = proprietorProfile.getPlotProfile();
        if (plotProfile.isPlacingQueued())
            return;
        BlockFace face = player.getFacing();
        StructureData structureData = getHolder().getModel().getStructureData();
        Location location = getSelected().getLocation();
        Location origin = location.clone();

        Vector removePivot = structureData.getRemovePivot();
        Vector removeRelativeOffset = structureData.getRemoveRelativeOffset() == null ?
                new Vector() : structureData.getRemoveRelativeOffset();
        Vector translation = structureData.getTranslation() == null ?
                new Vector() : structureData.getTranslation();
        Vector pos1 = new Vector(-1, 0, -1);
        Vector pos2 = structureData.getArea();
        int rotation;
        switch (face) {
            case NORTH -> rotation = 0;
            case EAST -> {
                rotation = 270;
                pos1.add(new Vector(0, 0, -1));
            }
            case SOUTH -> {
                rotation = 180;
                pos1.add(pos1);
            }
            case WEST -> {
                rotation = 90;
                pos1.add(new Vector(-1, 0, 0));
            }
            default -> throw TycoonExceptionFactory.getInstance()
                    .getInvalidCardinalDirectionExceptionFactory()
                    .notMain(face.name());
        }
        removePivot = Vectorator.of(removePivot).rotate(rotation);
        removeRelativeOffset = Vectorator.of(removeRelativeOffset).rotate(rotation);
        translation = Vectorator.of(translation).rotate(rotation);
        location.add(removePivot);
        pos1 = Vectorator.of(pos1).rotate(rotation);
        pos2 = Vectorator.of(pos2).rotate(rotation);

        World world = location.getWorld();
        location.setY(origin.getBlockY() - 1);
        pos = location.clone();
        Location to = location.clone();
        to.add(removeRelativeOffset);
        to.add(translation);
        CuboidArea area = CuboidArea.of(to, pos1, pos2);
        this.cuboidArea = area;
        Cuboid selectionCuboid = area.cuboid();

        Plot plot = plotProfile.getPlot();
        PlotData plotData = plot.getData();
        SelectionConfiguration selectionConfiguration = SelectionConfiguration.getInstance();
        Particle particle = selectionConfiguration.getAllowedParticle();
        double distance = selectionConfiguration.getAllowedDistance();
        if (!plotData.isInside(selected.getLocation())) {
            particle = selectionConfiguration.getDeniedParticle();
            distance = selectionConfiguration.getDeniedDistance();
        }
        Cuboid cuboid = cuboidArea.cuboid();
        Location loc1 = cuboidArea.getLoc1();
        Location loc2 = cuboidArea.getLoc2();
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
                    Block checkBlock = selected.getWorld().getBlockAt(x, y, z);
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
        if (doPaste > 0) {
            particle = selectionConfiguration.getDeniedParticle();
            distance = selectionConfiguration.getDeniedDistance();
        }

        visualizer = Selection.of(selectionCuboid, distance)
                .visualize(player, particle);
    }

    public void stop() {
        if (visualizer != null)
            visualizer.cancel();
        if (checker != null)
            checker.cancel();
    }

    @NotNull
    public TycoonModelHolder<StructureModel> getHolder() {
        return holder;
    }
}
