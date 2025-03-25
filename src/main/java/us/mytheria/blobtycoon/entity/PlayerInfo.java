package us.mytheria.blobtycoon.entity;

import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public record PlayerInfo(double getHealth,
                         double getMaxHealth,
                         int getFoodLevel,
                         float getSaturation,
                         float getExhaustion,
                         int getLevel,
                         float getExp,
                         int getTotalExperience,
                         float getFallDistance,
                         int getFireTicks,
                         int getRemainingAir,
                         int getMaximumAir,
                         boolean getAllowFlight,
                         boolean getFlying,
                         String getGameMode,
                         float getWalkSpeed,
                         float getFlySpeed,
                         PlayerAttributeModifiers getAttributeModifiers) {

    public static PlayerInfo of(Player player) {
        return new PlayerInfo(player.getHealth(),
                player.getAttribute(Attribute.MAX_HEALTH).getValue(),
                player.getFoodLevel(),
                player.getSaturation(),
                player.getExhaustion(),
                player.getLevel(),
                player.getExp(),
                player.getTotalExperience(),
                player.getFallDistance(),
                player.getFireTicks(),
                player.getRemainingAir(),
                player.getMaximumAir(),
                player.getAllowFlight(),
                player.isFlying(),
                player.getGameMode().name(),
                player.getWalkSpeed(),
                player.getFlySpeed(),
                PlayerAttributeModifiers.of(player));
    }

    public static PlayerInfo deserialize(Map<String, Object> map) {
        return new PlayerInfo((double) map.get("Health"),
                (double) map.get("MaxHealth"),
                (int) map.get("FoodLevel"),
                ((Double) map.get("Saturation")).floatValue(),
                ((Double) map.get("Exhaustion")).floatValue(),
                (int) map.get("Level"),
                ((Double) map.get("Exp")).floatValue(),
                (int) map.get("TotalExperience"),
                ((Double) map.get("FallDistance")).floatValue(),
                (int) map.get("FireTicks"),
                (int) map.get("RemainingAir"),
                (int) map.get("MaximumAir"),
                (boolean) map.get("AllowFlight"),
                (boolean) map.get("Flying"),
                (String) map.get("GameMode"),
                ((Double) map.get("WalkSpeed")).floatValue(),
                ((Double) map.get("FlySpeed")).floatValue(),
                new PlayerAttributeModifiers((Map<String, Object>) map.get("AttributeModifiers")));
    }

    public void apply(Player player) {
        getAttributeModifiers.apply(player);
        player.setHealth(getHealth);
        player.setFoodLevel(getFoodLevel);
        player.setSaturation(getSaturation);
        player.setExhaustion(getExhaustion);
        player.setLevel(getLevel);
        player.setExp(getExp);
        player.setTotalExperience(getTotalExperience);
        player.setFallDistance(getFallDistance);
        player.setFireTicks(getFireTicks);
        player.setRemainingAir(getRemainingAir);
        player.setMaximumAir(getMaximumAir);
        player.setAllowFlight(getAllowFlight);
        player.setFlying(getFlying);
        player.setGameMode(GameMode.valueOf(getGameMode));
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("Health", getHealth);
        map.put("MaxHealth", getMaxHealth);
        map.put("FoodLevel", getFoodLevel);
        map.put("Saturation", (double) getSaturation);
        map.put("Exhaustion", (double) getExhaustion);
        map.put("Level", getLevel);
        map.put("Exp", (double) getExp);
        map.put("TotalExperience", getTotalExperience);
        map.put("FallDistance", (double) getFallDistance);
        map.put("FireTicks", getFireTicks);
        map.put("RemainingAir", getRemainingAir);
        map.put("MaximumAir", getMaximumAir);
        map.put("AllowFlight", getAllowFlight);
        map.put("Flying", getFlying);
        map.put("GameMode", getGameMode);
        map.put("WalkSpeed", (double) getWalkSpeed);
        map.put("FlySpeed", (double) getFlySpeed);
        map.put("AttributeModifiers", getAttributeModifiers.getSerialized());
        return map;
    }
}
