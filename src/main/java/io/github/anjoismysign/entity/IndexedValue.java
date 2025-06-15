package io.github.anjoismysign.entity;

public record IndexedValue<T>(int getIndex,
                              T getValue) {
}
