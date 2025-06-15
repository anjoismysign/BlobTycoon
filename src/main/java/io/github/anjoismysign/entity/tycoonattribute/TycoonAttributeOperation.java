package io.github.anjoismysign.entity.tycoonattribute;


import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum TycoonAttributeOperation {
    ADD,
    ADD_SCALAR;

    private static final Map<String, TycoonAttributeOperation> nameMap = new HashMap<>();

    static {
        for (TycoonAttributeOperation obj : TycoonAttributeOperation.values()) {
            nameMap.put(obj.name(), obj);
        }
    }

    @Nullable
    public static TycoonAttributeOperation fromName(String type) {
        return nameMap.get(type);
    }
}
