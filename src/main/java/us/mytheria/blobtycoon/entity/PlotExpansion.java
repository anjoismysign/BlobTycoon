package us.mytheria.blobtycoon.entity;

import org.bson.types.Binary;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.utilities.Structrador;
import us.mytheria.blobtycoon.BlobTycoon;
import us.mytheria.blobtycoon.entity.plotdata.PlotData;
import us.mytheria.blobtycoon.util.TycoonStructrador;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A PlotExpansion is basically a terrain plot.
 * A Plot can hold multiple terrains but only one can be
 * loaded at a time.
 * PlotExpansion will later hold PlotHelpers which are
 * NPCs that help the player in the plot.
 */
public class PlotExpansion {
    private final BlobTycoon plugin;
    private Structrador structrador;
    private StructureDirection direction;
    private Map<UUID, PlotObject> plotObjects;
    private int index;

    @NotNull
    public static PlotExpansion deserialize(Map<String, Object> map, BlobTycoon plugin) {
        Structrador structrador;
        try {
            Binary byteArray = (Binary) map.get("Structure");
            structrador = new TycoonStructrador(byteArray.getData(), plugin);
        } catch ( Exception exception ) {
            throw new RuntimeException("Failed to deserialize PlotExpansion", exception);
        }
        StructureDirection direction;
        if (!map.containsKey("Direction"))
            direction = StructureDirection.NORTH;
        else {
            int ordinal = (int) map.get("Direction");
            direction = StructureDirection.byOrdinal(ordinal);
        }
        Map<UUID, PlotObject> plotObjects;
        if (!map.containsKey("PlotObjects"))
            plotObjects = new HashMap<>();
        else {
            Map<String, String> serialized = (Map<String, String>) map.get("PlotObjects");
            plotObjects = deserializePlotObjects(serialized);
        }
        int index = (int) map.get("Index");
        PlotExpansion expansion = new PlotExpansion(structrador,
                direction, index, plugin);
        expansion.plotObjects = plotObjects;
        return expansion;
    }

    public PlotExpansion(Structrador structrador,
                         StructureDirection direction,
                         int index,
                         BlobTycoon plugin) {
        this.structrador = structrador;
        this.direction = direction;
        this.index = index;
        this.plugin = plugin;
        this.plotObjects = new HashMap<>();
    }

    public void restructure(PlotData plotData) {
        this.structrador = new TycoonStructrador(plotData
                .saveStructure(true), plugin);
        this.direction = plotData.getDirection();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> inner = new HashMap<>();
        byte[] structure = structrador.toByteArray();
        Binary binary = new Binary(structure);
        inner.put("Structure", binary);
        inner.put("Direction", direction.getOrdinal());
        inner.put("PlotObjects", serializePlotObjects());
        inner.put("Index", index);
        return inner;
    }

    private Map<String, String> serializePlotObjects() {
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<UUID, PlotObject> entry : plotObjects.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().serializeOffset());
        }
        return map;
    }

    private static Map<UUID, PlotObject> deserializePlotObjects(Map<String, String> flat) {
        Map<UUID, PlotObject> plotObjects = new HashMap<>();
        for (Map.Entry<String, String> entry : flat.entrySet()) {
            UUID uuid = UUID.fromString(entry.getKey());
            String offset = entry.getValue();
            plotObjects.put(uuid, PlotObject.of(uuid, offset));
        }
        return plotObjects;
    }

    public Structrador getStructrador() {
        return structrador;
    }

    public StructureDirection getDirection() {
        return direction;
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public PlotObject getObject(UUID uuid) {
        return plotObjects.get(uuid);
    }

    public void addObject(@NotNull PlotObject plotObject) {
        Objects.requireNonNull(plotObject);
        plotObjects.put(plotObject.getId(), plotObject);
    }

    public void removeObject(@NotNull UUID uuid) {
        plotObjects.remove(uuid);
    }

    /**
     * Checks if the given BlockVector belongs to a PlotObject.
     *
     * @param vector The BlockVector to check.
     * @return The PlotObject if it exists, null otherwise.
     */
    @Nullable
    public PlotObject belongsToAnObject(@NotNull BlockVector vector) {
        return plotObjects.values().stream()
                .filter(plotObject -> plotObject.getOffset().equals(vector))
                .findFirst()
                .orElse(null);
    }
}
