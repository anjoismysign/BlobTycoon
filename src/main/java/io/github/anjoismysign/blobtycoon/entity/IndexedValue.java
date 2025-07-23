package io.github.anjoismysign.blobtycoon.entity;

public record IndexedValue<T>(int getIndex,
                              T getValue) {
}
