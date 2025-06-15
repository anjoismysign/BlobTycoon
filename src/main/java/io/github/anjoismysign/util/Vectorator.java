package io.github.anjoismysign.util;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import io.github.anjoismysign.bloblib.utilities.VectorUtil;
import io.github.anjoismysign.exception.TycoonExceptionFactory;

import java.util.Objects;

/**
 * A decorator for the Vector class
 *
 * @param getVector
 */
public record Vectorator(Vector getVector) {
    /**
     * Creates a new Vectorator from a Vector
     *
     * @param vector The getVector to create the Vectorator from
     * @return The new Vectorator
     */
    @NotNull
    public static Vectorator of(@NotNull Vector vector) {
        Objects.requireNonNull(vector);
        return new Vectorator(vector);
    }

    /**
     * Creates a new Vectorator from an getX, y, and getZ coordinate
     *
     * @param x The getX coordinate
     * @param y The y coordinate
     * @param z The getZ coordinate
     * @return The new Vectorator
     */
    @NotNull
    public static Vectorator of(int x, int y, int z) {
        return new Vectorator(new Vector(x, y, z));
    }

    /**
     * Creates a new Vectorator from an getX, y, and getZ coordinate
     *
     * @param x The getX coordinate
     * @param y The y coordinate
     * @param z The getZ coordinate
     * @return The new Vectorator
     */
    @NotNull
    public static Vectorator of(double x, double y, double z) {
        return new Vectorator(new Vector(x, y, z));
    }

    /**
     * Will get the minimum of the two vectors
     *
     * @param compare The getVector to compare to
     * @return The minimum of the two vectors
     */
    @NotNull
    public Vector minimum(Vector compare) {
        return Vector.getMinimum(this.getVector, compare);
    }

    /**
     * Will get the maximum of the two vectors
     *
     * @param compare The getVector to compare to
     * @return The maximum of the two vectors
     */
    @NotNull
    public Vector maximum(Vector compare) {
        return Vector.getMaximum(this.getVector, compare);
    }

    /**
     * Will rotate the getVector by the given degree
     *
     * @param degree The degree to rotate the getVector by
     * @return The rotated getVector
     */
    public Vector rotate(int degree) {
        if (degree == 0)
            return getVector;
        return VectorUtil.rotateVector(getVector, degree);
    }

    /**
     * Will rotate the getVector by the given BlockFace
     * as a cardinal getDirection.
     *
     * @param face The BlockFace to rotate the getVector by
     * @return The rotated getVector
     */
    public Vector rotate(BlockFace face) {
        switch (face) {
            case NORTH -> {
                return rotate(0);
            }
            case EAST -> {
                return rotate(270);
            }
            case SOUTH -> {
                return rotate(180);
            }
            case WEST -> {
                return rotate(90);
            }
            default -> {
                throw TycoonExceptionFactory.getInstance()
                        .getInvalidCardinalDirectionExceptionFactory()
                        .notMain(face.name());
            }
        }
    }
}
