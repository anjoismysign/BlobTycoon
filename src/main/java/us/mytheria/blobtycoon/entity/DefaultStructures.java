package us.mytheria.blobtycoon.entity;


import us.mytheria.bloblib.utilities.Structrador;

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
