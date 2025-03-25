package us.mytheria.blobtycoon.entity;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.SerializableProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A PlotProprietorProfile is a player who owns a PlotProfile.
 */
public class PlotProprietorProfile implements SerializableProfile {
    //Non-transient, non-final attributes
    private final String profileName, lastKnownName, identification;
    //Transient final attributes
    transient private final PlotProfile plotProfile;
    //Transient non-final attributes
    transient ProfileInventory profileInventory;
    @Nullable
    transient PlayerInfo playerInfo;

    public PlotProprietorProfile(@NotNull String profileName, @NotNull String lastKnownName,
                                 @NotNull String identification,
                                 @NotNull ProfileInventory inventory,
                                 @NotNull PlotProfile plotProfile,
                                 @Nullable PlayerInfo playerInfo) {
        this.profileName = Objects.requireNonNull(profileName);
        this.lastKnownName = Objects.requireNonNull(lastKnownName);
        this.identification = Objects.requireNonNull(identification);
        this.plotProfile = Objects.requireNonNull(plotProfile);
        this.profileInventory = Objects.requireNonNull(inventory);
        this.playerInfo = playerInfo;
    }

    /**
     * Deserialize a PlotProprietor from a map.
     * It assumes that player is not online
     *
     * @param map The map to deserialize from.
     */
    public PlotProprietorProfile(Map<String, Object> map,
                                 PlotProfile plotProfile) {
        this((String) map.get("ProfileName"),
                (String) map.get("LastKnownName"),
                (String) map.get("Identification"),
                ProfileInventory.deserialize(map.get("ProfileInventory") == null ?
                        null : (Map<String, Object>) map.get("ProfileInventory")),
                plotProfile,
                map.get("PlayerInfo") == null ? null :
                        PlayerInfo.deserialize((Map<String, Object>) map.get("PlayerInfo")));
    }

    public PlotProprietorProfile updateName(@NotNull Player player) {
        return new PlotProprietorProfile(profileName,
                Objects.requireNonNull(player).getName(),
                identification, profileInventory, plotProfile, playerInfo);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("ProfileName", profileName);
        map.put("LastKnownName", lastKnownName);
        map.put("Identification", identification);
        map.put("ProfileInventory", profileInventory.getSerialized());
        map.put("PlayerInfo", playerInfo == null ? null : playerInfo.serialize());
        return map;
    }

    @NotNull
    public String getProfileName() {
        return profileName;
    }

    @NotNull
    public String getLastKnownName() {
        return lastKnownName;
    }

    @NotNull
    public String getIdentification() {
        return identification;
    }

    @NotNull
    public PlotProfile getPlotProfile() {
        return plotProfile;
    }

    /**
     * Applies the profile to the given player.
     *
     * @param player The player to apply the profile to.
     */
    public void apply(@NotNull Player player) {
        if (playerInfo != null)
            playerInfo.apply(Objects.requireNonNull(player));
        profileInventory.give(player);
    }

    /**
     * Serializes the player to the profile.
     *
     * @param player The player to serialize.
     */
    public void serialize(@NotNull Player player) {
        profileInventory = profileInventory.serialize(Objects.requireNonNull(player));
        playerInfo = PlayerInfo.of(Objects.requireNonNull(player));
    }
}
