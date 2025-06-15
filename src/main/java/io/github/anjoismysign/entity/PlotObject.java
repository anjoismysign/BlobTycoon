package io.github.anjoismysign.entity;

import org.bukkit.util.BlockVector;

import java.util.Map;
import java.util.UUID;

public record PlotObject(UUID getId,
                         BlockVector getOffset) {
    public Map<String, String> serialize() {
        return Map.of(
                "Offset", serializeOffset(),
                "Id", getId.toString()
        );
    }

    public String serializeOffset() {
        return getOffset.getBlockX() + "," + getOffset.getBlockY() + "," + getOffset.getBlockZ();
    }

    public static PlotObject of(UUID uuid, String offset) {
        String[] offsetArray = offset.split(",");
        return new PlotObject(uuid,
                new BlockVector(Integer.parseInt(offsetArray[0]),
                        Integer.parseInt(offsetArray[1]),
                        Integer.parseInt(offsetArray[2])));
    }

    public static PlotObject of(Map<String, String> map) {
        UUID id = UUID.fromString(map.get("Id"));
        return of(id, map.get("Offset"));
    }
}
