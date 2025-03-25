package us.mytheria.blobtycoon.entity.tycoonattribute;

import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record TycoonAttributeModifier(double getAmount,
                                      @NotNull TycoonAttributeOperation getOperation) {

    public static TycoonAttributeModifier of(double amount,
                                             @NotNull TycoonAttributeOperation operation) {
        Objects.requireNonNull(operation, "'operation' cannot be null");
        return new TycoonAttributeModifier(amount, operation);
    }

    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap();
        data.put("operation", getOperation.ordinal());
        data.put("amount", getAmount);
        return data;
    }

    @NotNull
    public static TycoonAttributeModifier deserialize(Map<String, Object> data) {
        return new TycoonAttributeModifier(
                (double) data.get("amount"),
                TycoonAttributeOperation.values()[NumberConversions.toInt(data.get("operation"))]
        );
    }
}
