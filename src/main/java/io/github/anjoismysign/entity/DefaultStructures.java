package io.github.anjoismysign.entity;


import io.github.anjoismysign.bloblib.utilities.Structrador;

public enum DefaultStructures {
    STOCK(DefaultStructuresInitializer.getStock()),
    CLEAN(DefaultStructuresInitializer.getClean());
    private final Structrador structrador;

    DefaultStructures(Structrador structrador) {
        this.structrador = structrador;
    }

    public Structrador getStructrador() {
        return structrador;
    }
}
