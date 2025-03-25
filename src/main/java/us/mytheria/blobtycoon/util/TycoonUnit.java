package us.mytheria.blobtycoon.util;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public enum TycoonUnit {
    THOUSANDS_SEPARATOR(value -> ThousandsSeparator.getInstance().format(value));

    private final Function<Double, String> formatter;

    TycoonUnit(Function<Double, String> formatter) {
        this.formatter = formatter;
    }

    public String format(double value) {
        return formatter.apply(value);
    }

    private static final Map<String, TycoonUnit> typeToEnumMap = new HashMap<>();

    static {
        for (TycoonUnit obj : TycoonUnit.values()) {
            typeToEnumMap.put(obj.name(), obj);
        }
    }

    @Nullable
    public static TycoonUnit getTycoonUnit(String tycoonUnit) {
        return typeToEnumMap.get(tycoonUnit);
    }
}
