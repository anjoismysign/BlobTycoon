package us.mytheria.blobtycoon.entity;

import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ChunkCoordinates(int getX, int getZ) {
    public static ChunkCoordinates of(@NotNull Chunk chunk) {
        Objects.requireNonNull(chunk, "'chunk' cannot be null");
        return new ChunkCoordinates(chunk.getX(), chunk.getZ());
    }

    public static ChunkCoordinates fromString(@NotNull String string) {
        Objects.requireNonNull(string, "'string' cannot be null");
        String[] split = string.split(":");
        if (split.length != 2)
            throw new IllegalArgumentException("Invalid string format");
        return new ChunkCoordinates(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }

    public boolean isChunk(@NotNull Chunk chunk) {
        Objects.requireNonNull(chunk, "'chunk' cannot be null");
        return chunk.getX() == getX() && chunk.getZ() == getZ();
    }

    public String toString() {
        return getX + ":" + getZ;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ChunkCoordinates other)) {
            return false;
        } else {
            if (this.getX() != other.getX()) {
                return false;
            } else {
                return this.getZ() == other.getZ();
            }
        }
    }
}
