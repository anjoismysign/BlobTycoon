package us.mytheria.blobtycoon.entity;

import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum StructureDirection {
    NORTH(0, BlockFace.NORTH, 0),
    EAST(1, BlockFace.EAST, 90),
    SOUTH(2, BlockFace.SOUTH, 180),
    WEST(3, BlockFace.WEST, 270);
    private final int ordinal;
    private final BlockFace blockFace;
    private final float yaw;
    private static final Map<Integer, StructureDirection> BY_ORDINAL = new HashMap<>();
    private static final Map<BlockFace, StructureDirection> BY_BLOCK_FACE = new HashMap<>();

    static {
        for (StructureDirection direction : values()) {
            BY_ORDINAL.put(direction.getOrdinal(), direction);
            BY_BLOCK_FACE.put(direction.getBlockFace(), direction);
        }
    }

    StructureDirection(int ordinal,
                       @NotNull BlockFace blockFace,
                       float yaw) {
        this.ordinal = ordinal;
        this.blockFace = blockFace;
        this.yaw = yaw;
    }

    @Nullable
    public static StructureDirection byOrdinal(int ordinal) {
        return BY_ORDINAL.get(ordinal);
    }

    @Nullable
    public static StructureDirection byBlockFace(BlockFace blockFace) {
        return BY_BLOCK_FACE.get(blockFace);
    }

    public int getOrdinal() {
        return ordinal;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public float getYaw() {
        return yaw;
    }
}
