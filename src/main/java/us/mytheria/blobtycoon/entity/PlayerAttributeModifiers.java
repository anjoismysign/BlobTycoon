package us.mytheria.blobtycoon.entity;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PlayerAttributeModifiers(Map<String, Object> getSerialized) {

    private static List<Attribute> attributes() {
        RegistryAccess access = RegistryAccess.registryAccess();
        Registry<@NotNull Attribute> registry = access.getRegistry(RegistryKey.ATTRIBUTE);
        return registry.stream().toList();
    }

    public static PlayerAttributeModifiers of(Player player) {
        Map<String, Object> map = new HashMap<>();
        for (Attribute attribute : attributes()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null)
                continue;
            Collection<AttributeModifier> modifiers = instance.getModifiers();
            if (modifiers.isEmpty())
                continue;
            Map<String, Object> attributeMap = new HashMap<>();
            instance.getModifiers().forEach(modifier -> {
                Map<String, Object> serialized = modifier.serialize();
                attributeMap.put(modifier.getName(), serialized);
            });
            map.put(attribute.getKey().toString(), attributeMap);
        }
        return new PlayerAttributeModifiers(map);
    }

    public void apply(Player player) {
        for (Map.Entry<String, Object> entry : getSerialized.entrySet()) {
            String key = entry.getKey();
            @Nullable Attribute attribute = attributes().stream().filter(x -> x.getKey().toString().equals(key)).findFirst().orElse(null);
            if (attribute == null)
                continue;
            Map<String, Object> attributeMap = (Map<String, Object>) entry.getValue();
            for (Map.Entry<String, Object> attributeEntry : attributeMap.entrySet()) {
                try {
                    AttributeModifier modifier = AttributeModifier.deserialize((Map<String, Object>) attributeEntry.getValue());
                    player.getAttribute(attribute).addModifier(modifier);
                } catch (IllegalArgumentException exception) {
                    if (exception.getMessage().contains("already applied"))
                        continue;
                    throw exception;
                }
            }
        }
    }
}
