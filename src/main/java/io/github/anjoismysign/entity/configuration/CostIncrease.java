package io.github.anjoismysign.entity.configuration;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public enum CostIncrease {
    ABSENT,
    CONSTANT,
    EXPONENTIAL,
    LINEAR;

    private static final Map<String, CostIncrease> costIncreaseMap = new HashMap<>();

    static {
        for (CostIncrease costIncrease : values()) {
            costIncreaseMap.put(costIncrease.name().toLowerCase(Locale.ROOT), costIncrease);
        }
    }

    public static CostIncrease byName(String name) {
        return costIncreaseMap.get(name.toLowerCase(Locale.ROOT));
    }
}
