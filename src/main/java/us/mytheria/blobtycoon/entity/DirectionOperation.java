package us.mytheria.blobtycoon.entity;

import org.bukkit.block.structure.StructureRotation;
import us.mytheria.blobtycoon.exception.TycoonExceptionFactory;

/**
 * Represents an operation to perform on a structure.
 *
 * @param getCurrentDirection the current getDirection of the structure
 * @param getGoalDirection    the goal getDirection of the structure
 */
public record DirectionOperation(StructureDirection getCurrentDirection,
                                 StructureDirection getGoalDirection) {

    public StructureRotation perform() {
        if (getCurrentDirection == getGoalDirection) {
            return StructureRotation.NONE;
        }
        if (getCurrentDirection != StructureDirection.NORTH && getCurrentDirection != StructureDirection.SOUTH && getCurrentDirection != StructureDirection.EAST && getCurrentDirection != StructureDirection.WEST)
            throw new IllegalArgumentException("Invalid getCurrentDirection getDirection: " + getCurrentDirection);
        if (getGoalDirection != StructureDirection.NORTH && getGoalDirection != StructureDirection.SOUTH && getGoalDirection != StructureDirection.EAST && getGoalDirection != StructureDirection.WEST)
            throw new IllegalArgumentException("Invalid getGoalDirection getDirection: " + getGoalDirection);
        switch (getCurrentDirection) {
            case NORTH -> {
                switch (getGoalDirection) {
                    case SOUTH -> {
                        return StructureRotation.CLOCKWISE_180;
                    }
                    case EAST -> {
                        return StructureRotation.CLOCKWISE_90;
                    }
                    default -> {
                        return StructureRotation.COUNTERCLOCKWISE_90;
                    }
                }
            }
            case SOUTH -> {
                switch (getGoalDirection) {
                    case NORTH -> {
                        return StructureRotation.CLOCKWISE_180;
                    }
                    case EAST -> {
                        return StructureRotation.COUNTERCLOCKWISE_90;
                    }
                    default -> {
                        return StructureRotation.CLOCKWISE_90;
                    }
                }
            }
            case EAST -> {
                switch (getGoalDirection) {
                    case SOUTH -> {
                        return StructureRotation.CLOCKWISE_90;
                    }
                    case NORTH -> {
                        return StructureRotation.COUNTERCLOCKWISE_90;
                    }
                    default -> {
                        return StructureRotation.CLOCKWISE_180;
                    }
                }
            }
            case WEST -> {
                switch (getGoalDirection) {
                    case SOUTH -> {
                        return StructureRotation.COUNTERCLOCKWISE_90;
                    }
                    case EAST -> {
                        return StructureRotation.CLOCKWISE_180;
                    }
                    default -> {
                        return StructureRotation.CLOCKWISE_90;
                    }
                }
            }
        }
        throw TycoonExceptionFactory.getInstance()
                .getInvalidCardinalDirectionExceptionFactory()
                .notMain(getCurrentDirection.name());
    }
}
