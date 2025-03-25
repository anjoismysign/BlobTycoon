package us.mytheria.blobtycoon.director.manager;

import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.entities.ObjectManager;
import us.mytheria.bloblib.utilities.HandyDirectory;
import us.mytheria.blobtycoon.director.TycoonManager;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.AssetType;
import us.mytheria.blobtycoon.entity.asset.ObjectAsset;
import us.mytheria.blobtycoon.entity.asset.RackAsset;
import us.mytheria.blobtycoon.entity.asset.StructureAsset;
import us.mytheria.blobtycoon.entity.mechanics.MechanicsData;
import us.mytheria.blobtycoon.entity.valuable.Valuable;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ExpansionManager extends TycoonManager {
    private final File expansionDirectory, expansionOutputFile;

    public ExpansionManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
        expansionDirectory = new File(getPlugin().getDataFolder(), "expansion");
        expansionOutputFile = new File(expansionDirectory, "output");
        if (!expansionDirectory.exists())
            expansionDirectory.mkdirs();
        if (!expansionOutputFile.exists())
            expansionOutputFile.mkdirs();
        reload();
    }

    public void reload() {
        HandyDirectory handyDirectory = HandyDirectory.of(expansionDirectory);
        for (File file : handyDirectory.listFiles("zip")) {
            if (!loadExpansion(file))
                getPlugin().getLogger().severe("Failed to load expansion: " + file.getName());
        }
        TycoonManagerDirector director = getManagerDirector();
    }

    /**
     * Loads an expansion from a file.
     *
     * @param expansion The file to load the expansion from.
     * @return {@code true} if the expansion was loaded successfully, {@code false} otherwise.
     */
    public boolean loadExpansion(@NotNull File expansion) {
        Objects.requireNonNull(expansion);
        TycoonManagerDirector director = getManagerDirector();
        if (!director.loadBlobLibExpansion(expansion))
            return false;
        HandyDirectory handyDirectory = HandyDirectory.of(expansionOutputFile);
        Map<AssetType, List<File>> assets = new HashMap<>();
        Map<AssetType, File> assetsDirectory = new HashMap<>();
        for (File directory : handyDirectory.listDirectories()) {
            if (directory.getName().equals(AssetType.STRUCTURE.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("nbt").stream().toList();
                assets.put(AssetType.STRUCTURE, list);
                assetsDirectory.put(AssetType.STRUCTURE, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.RACK_ASSET.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.RACK_ASSET, list);
                assetsDirectory.put(AssetType.RACK_ASSET, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.OBJECT_ASSET.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.OBJECT_ASSET, list);
                assetsDirectory.put(AssetType.OBJECT_ASSET, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.STRUCTURE_ASSET.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.STRUCTURE_ASSET, list);
                assetsDirectory.put(AssetType.STRUCTURE_ASSET, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.MECHANICS_DATA.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.MECHANICS_DATA, list);
                assetsDirectory.put(AssetType.MECHANICS_DATA, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.VALUABLE.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.VALUABLE, list);
                assetsDirectory.put(AssetType.VALUABLE, directory);
                continue;
            }
            if (directory.getName().equals(AssetType.TYCOON_PET.getDirectoryName())) {
                HandyDirectory subDirectory = HandyDirectory.of(directory);
                List<File> list = subDirectory.listRecursively("yml").stream().toList();
                assets.put(AssetType.TYCOON_PET, list);
                assetsDirectory.put(AssetType.TYCOON_PET, directory);
            }
        }
        if (assets.isEmpty()) {
            getPlugin().getLogger().warning("No assets found in expansion: " + expansion.getName());
            return true;
        }
        List<File> mechanicsData = assets.get(AssetType.MECHANICS_DATA);
        if (mechanicsData != null && !mechanicsData.isEmpty()) {
            ObjectManager<MechanicsData> objectManager = director.getMechanicsDataDirector().getObjectManager();
            mechanicsData.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.MECHANICS_DATA)).deleteRecursively();
        }
        List<File> valuable = assets.get(AssetType.VALUABLE);
        if (valuable != null && !valuable.isEmpty()) {
            ObjectManager<Valuable> objectManager = director.getValuableDirector().getObjectManager();
            valuable.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.VALUABLE)).deleteRecursively();
        }
        List<File> structure = assets.get(AssetType.STRUCTURE);
        if (structure != null && !structure.isEmpty()) {
            StructureTracker tracker = director.getStructureTracker();
            structure.forEach(tracker::load);
            HandyDirectory.of(assetsDirectory.get(AssetType.STRUCTURE)).deleteRecursively();
        }
        List<File> rackAsset = assets.get(AssetType.RACK_ASSET);
        if (rackAsset != null && !rackAsset.isEmpty()) {
            ObjectManager<RackAsset> objectManager = director.getRackAssetDirector().getObjectManager();
            rackAsset.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.RACK_ASSET)).deleteRecursively();
        }
        List<File> objectAsset = assets.get(AssetType.OBJECT_ASSET);
        if (objectAsset != null && !objectAsset.isEmpty()) {
            ObjectManager<ObjectAsset> objectManager = director.getObjectAssetDirector().getObjectManager();
            objectAsset.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.OBJECT_ASSET)).deleteRecursively();
        }
        List<File> structureAsset = assets.get(AssetType.STRUCTURE_ASSET);
        if (structureAsset != null && !structureAsset.isEmpty()) {
            ObjectManager<StructureAsset> objectManager = director.getStructureAssetDirector().getObjectManager();
            structureAsset.forEach(file -> {
                objectManager.loadFile(file, e -> {
                });
            });
            HandyDirectory.of(assetsDirectory.get(AssetType.STRUCTURE_ASSET)).deleteRecursively();
        }
        List<File> tycoonPet = assets.get(AssetType.TYCOON_PET);
        if (tycoonPet != null && !tycoonPet.isEmpty()) {
            tycoonPet.forEach(file -> {
                director.getBlobPetsMiddleman().addExpansion(file);
            });
            director.getBlobPetsMiddleman().setExpansionDirectory(assetsDirectory.get(AssetType.TYCOON_PET));
        }
        return true;
    }
}
