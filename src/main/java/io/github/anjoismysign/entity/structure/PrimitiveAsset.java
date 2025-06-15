package io.github.anjoismysign.entity.structure;

import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.entity.tycoonattribute.TycoonAttributeOperation;

import java.util.HashMap;
import java.util.Map;

public enum PrimitiveAsset {
    STRUCTURE("bt_structure", true),
    RACK("bt_rack", true),
    OBJECT("bt_object", false);
    private final String type;
    private final boolean isStructure;

    PrimitiveAsset(String type, boolean isStructure) {
        this.type = type;
        this.isStructure = isStructure;
    }

    public String getType() {
        return type;
    }

    public boolean isStructure() {
        return isStructure;
    }

    private static final Map<String, TycoonAttributeOperation> nameMap = new HashMap<>();

    @Nullable
    public static TycoonAttributeOperation fromName(String type) {
        return nameMap.get(type);
    }

    private static final Map<String, PrimitiveAsset> typeMap = new HashMap<>();

    @Nullable
    public static PrimitiveAsset fromType(String type) {
        return typeMap.get(type);
    }

    static {
        for (PrimitiveAsset obj : PrimitiveAsset.values()) {
            typeMap.put(obj.getType(), obj);
        }
        for (TycoonAttributeOperation obj : TycoonAttributeOperation.values()) {
            nameMap.put(obj.name(), obj);
        }
    }
}
