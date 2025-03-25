package us.mytheria.blobtycoon.entity;

public enum AssetType {
    STRUCTURE("Structures"),
    RACK_ASSET("rackAsset"),
    OBJECT_ASSET("objectAsset"),
    STRUCTURE_ASSET("structureAsset"),
    MECHANICS_DATA("mechanicsData"),
    VALUABLE("valuable"),
    TYCOON_PET("tycoonPet");

    private final String directoryName;

    AssetType(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }
}
