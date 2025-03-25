package us.mytheria.blobtycoon.entity;

public record IndexedValue<T>(int getIndex,
                              T getValue) {
}
