package us.mytheria.blobtycoon.entity;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record PlayerAttributeModifiers(Map<String, Object> getSerialized) {

    public static PlayerAttributeModifiers of(Player player) {
        Map<String, Object> map = new HashMap<>();
        for (Attribute attribute : Attribute.values()) {
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
            map.put(attribute.ordinal() + "", attributeMap);
        }
        return new PlayerAttributeModifiers(map);
    }

    public void apply(Player player) {
        for (Map.Entry<String, Object> entry : getSerialized.entrySet()) {
            Attribute attribute = Attribute.values()[NumberConversions.toInt(entry.getKey())];
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
