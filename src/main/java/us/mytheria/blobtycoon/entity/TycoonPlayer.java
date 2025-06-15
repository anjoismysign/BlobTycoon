package us.mytheria.blobtycoon.entity;

import me.anjoismysign.anjo.entities.Uber;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.BlobSerializable;
import us.mytheria.bloblib.entities.MinecraftTimeUnit;
import us.mytheria.bloblib.vault.multieconomy.ElasticEconomy;
import us.mytheria.blobtycoon.BlobTycoon;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.playerconfiguration.PlayerConfiguration;
import us.mytheria.blobtycoon.exception.PlotProfileNotCompletedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A TycoonPlayer only holds
 */
public class TycoonPlayer implements BlobSerializable,
        PlayerConfiguration, TradeUser {
    private final BlobCrudable crudable;
    private final String lastKnownName, identification;
    private final Map<Integer, String> profiles;
    private final Map<Integer, String> profileNames;
    private final Map<String, String> configuration;
    @NotNull
    private final TradeUserInfo tradeUserInfo;
    @NotNull
    private final Uber<Runnable> removeStructure;
    @NotNull
    private final Uber<Runnable> useStructure;
    private final TycoonManagerDirector director;
    private int selectedProfile;
    private PlotProprietorProfile profile;
    private boolean lockProfile;

    public TycoonPlayer(BlobCrudable crudable, TycoonManagerDirector director) {
        this.director = director;
        BlobTycoon plugin = director.getPlugin();
        this.crudable = crudable;
        Document document = crudable.getDocument();
        this.tradeUserInfo = new TradeUserInfo();
        this.selectedProfile = document.containsKey("SelectedProfile") ?
                document.getInteger("SelectedProfile") :
                0;
        this.removeStructure = Uber.fly();
        this.useStructure = Uber.fly();
        Player player = getPlayer();
        lastKnownName = player.getName();
        identification = player.getUniqueId().toString();
        this.profiles = document.containsKey("Profiles") ?
                (Map<Integer, String>) document.get("Profiles") :
                new HashMap<>();
        this.profileNames = document.containsKey("ProfileNames") ?
                (Map<Integer, String>) document.get("ProfileNames") :
                new HashMap<>();
        this.configuration = document.containsKey("Configuration") ?
                (Map<String, String>) document.get("Configuration") :
                new HashMap<>();
        crudable.hasInteger("SelectedProfile").ifPresent(integer -> selectedProfile = integer);
        CompletableFuture<PlotProfile> future = new CompletableFuture<>();
        PlotProfileManager plotProfileManager = director.getPlotProfileManager();
        UUID uuid = player.getUniqueId();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String selectedProfileIdentification = getSelectedProfileIdentification();
            if (selectedProfileIdentification == null) {
                /*
                 * Creates a random profile
                 */
                CompletableFuture<PlotProfile> profileFuture = plotProfileManager
                        .createRandom(this);
                profileFuture.whenComplete((profile, throwable) -> {
                    if (throwable != null) {
                        future.completeExceptionally(throwable);
                        return;
                    }
                    String randomProfileName = BlobTycoonInternalAPI.getInstance().getRandomProfileName(profileNames.values().stream().toList());
                    profile.addProprietor(new PlotProprietorProfile(randomProfileName,
                            player.getName(), getIdentification(),
                            ProfileInventory.empty(), profile, null));
                    future.complete(profile);
                });
            } else {
                PlotProfile cached = plotProfileManager.isCached(selectedProfileIdentification)
                        .orElse(null);
                if (cached != null)
                    future.complete(cached);
                else {
                    CompletableFuture<PlotProfile> profileFuture = plotProfileManager
                            .download(selectedProfileIdentification, this);
                    profileFuture.whenComplete((profile, throwable) -> {
                        if (throwable != null) {
                            future.completeExceptionally(throwable);
                            return;
                        }
                        future.complete(profile);
                    });
                }
            }
        }, 1);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (future.isDone())
                return;
            future.completeExceptionally(new PlotProfileNotCompletedException("The plot profile took too long to load."));
        }, (long) MinecraftTimeUnit.TICKS.convert(5, MinecraftTimeUnit.SECONDS));
        future.whenComplete((plotProfile, throwable) -> {
            if (throwable != null) {
                if (throwable instanceof PlotProfileNotCompletedException) {
                } else
                    throwable.printStackTrace();
                return;
            }
            try {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player != Bukkit.getPlayer(uuid) || !plotProfile.isValid())
                        return;
                    if (profiles.isEmpty())
                        addProfile(plotProfile);
                    plotProfile.join(player);
                });
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @NotNull
    public String getLastKnownName() {
        return lastKnownName;
    }

    @Override
    public BlobCrudable blobCrudable() {
        return crudable;
    }

    @Override
    public BlobCrudable serializeAllAttributes() {
        Document document = crudable.getDocument();
        document.put("SelectedProfile", selectedProfile);
        document.put("Profiles", profiles);
        document.put("ProfileNames", profileNames);
        document.put("Configuration", configuration);
        return crudable;
    }

    @NotNull
    public String getIdentification() {
        return identification;
    }

    /**
     * Will return the PlotProfile associated with the given identification.
     *
     * @return The PlotProfile associated with the given identification.
     */
    @Nullable
    private String getSelectedProfileIdentification() {
        if (profiles.isEmpty())
            return null;
        return profiles.get(selectedProfile);
    }

    /**
     * Will link a PlotProprietorProfile to this TycoonPlayer.
     * If attempting to link a profile while another is already linked,
     * it will return false.
     *
     * @param plotProfile The profile to link.
     * @return True if the profile was successfully linked.
     */
    public boolean linkProfile(PlotProprietorProfile plotProfile) {
        if (lockProfile)
            return false;
        profile = plotProfile;
        lockProfile = true;
        String profileName = getProfile().getProfileName();
        profileNames.put(selectedProfile, profileName);
        return true;
    }

    /**
     * Will unlink any PlotProprietorProfile associated with this TycoonPlayer.
     */
    public void unlinkProfile() {
        profile = null;
        lockProfile = false;
    }

    /**
     * Gets the PlotProprietorProfile associated with this TycoonPlayer.
     * If no profile is still loaded, it will return null.
     *
     * @return The PlotProprietorProfile associated with this TycoonPlayer.
     * Null if no profile is loaded.
     */
    @Nullable
    public PlotProprietorProfile getProfile() {
        return profile;
    }

    /**
     * What should be run when the "Remove" button is clicked.
     *
     * @return The action to run when the "Remove" button is clicked.
     */
    public Uber<Runnable> getRemoveStructure() {
        return removeStructure;
    }

    /**
     * What should be run when the "Use" button is clicked.
     *
     * @return The action to run when the "Use" button is clicked.
     */
    public Uber<Runnable> getUseStructure() {
        return useStructure;
    }

    /**
     * Adds a profile to the list of profiles.
     *
     * @param plotProfile The profile to add.
     */
    public void addProfile(@NotNull PlotProfile plotProfile,
                           boolean setAsSelected) {
        Objects.requireNonNull(plotProfile, "'plotProfile' cannot be null");
        Set<Integer> keys = profiles.keySet();
        int lowestNotInMap = 0;
        for (int i = 0; i <= keys.size(); i++) {
            if (!keys.contains(i)) {
                lowestNotInMap = i;
                break;
            }
        }
        if (keys.contains(lowestNotInMap))
            throw new IllegalStateException("The lowest not in map is already in the map.");
        profiles.put(lowestNotInMap, plotProfile.getIdentification());
        if (setAsSelected)
            selectedProfile = lowestNotInMap;
    }

    private void removeProfile(int index) {
        profiles.remove(index);
        profileNames.remove(index);
    }

    public void addProfile(@NotNull PlotProfile plotProfile) {
        addProfile(plotProfile, false);
    }

    /**
     * Creates a profile for the player.
     *
     * @param ifFail The action to run if the profile creation fails.
     * @return True if the profile was successfully created.
     */
    public boolean createProfile(@Nullable Consumer<Player> ifFail) {
        Player player = getPlayer();
        if (player == null)
            return false;
        if (profiles.size() >= director.getConfigManager().getMaxProfilesPerPlayer()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Max-Profiles-Reached", player)
                    .handle(player);
            return false;
        }
        PlotProprietorProfile proprietorProfile = getProfile();
        if (proprietorProfile == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", player)
                    .handle(player);
            return false;
        }
        PlotProfile old = proprietorProfile.getPlotProfile();
        if (old.isPlacingQueued()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Plot-Still-Loading", player)
                    .handle(player);
            return false;
        }
        UUID uuid = player.getUniqueId();
        old.close(player, () -> {
            unlinkProfile();
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            elasticEconomy.getAllImplementations().forEach(identityEconomy -> {
                identityEconomy.withdrawPlayer(player, identityEconomy.getBalance(player));
            });
            CompletableFuture<PlotProfile> randomFuture = director
                    .getPlotProfileManager().createRandom(this);
            randomFuture.whenComplete((plotProfile, throwable) -> {
                if (throwable != null) {
                    if (ifFail != null && player == Bukkit.getPlayer(uuid))
                        ifFail.accept(player);
                    return;
                }
                if (player != Bukkit.getPlayer(uuid)) {
                    plotProfile.freePlot();
                    return;
                }
                String randomProfileName = BlobTycoonInternalAPI.getInstance().getRandomProfileName(profileNames.values().stream().toList());
                plotProfile.addProprietor(new PlotProprietorProfile(randomProfileName,
                        player.getName(), getIdentification(),
                        ProfileInventory.empty(), plotProfile, null));
                addProfile(plotProfile, true);
                Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
                    plotProfile.join(player);
                });
            });
        });
        return true;
    }

    /**
     * Switches the profile to the given profile.
     *
     * @param profile The profile to switch to.
     * @param ifFail  The action to run if the profile switch fails.
     * @return True if the profile was successfully switched.
     */
    public ProfileSwitchResult switchProfile(int profile,
                                             @Nullable Consumer<Player> ifFail,
                                             boolean delete) {
        if (!profiles.containsKey(profile))
            return ProfileSwitchResult.DOES_NOT_EXIST;
        if (selectedProfile == profile)
            return ProfileSwitchResult.ALREADY_LOADED;
        Player player = getPlayer();
        if (player == null)
            return ProfileSwitchResult.PLAYER_NOT_FOUND;
        PlotProprietorProfile proprietorProfile = getProfile();
        if (proprietorProfile == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", player)
                    .handle(player);
            return ProfileSwitchResult.PLAYER_NOT_IN_CACHE;
        }
        PlotProfile old = proprietorProfile.getPlotProfile();
        String oldIdentification = old.getIdentification();
        if (old.isPlacingQueued()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Plot-Still-Loading", player)
                    .handle(player);
            return ProfileSwitchResult.PLACING_QUEUED;
        }
        UUID uuid = player.getUniqueId();
        Runnable onSwitch = () -> {
            if (delete)
                director.getPlotProfileManager().deleteObject(oldIdentification);
            unlinkProfile();
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            elasticEconomy.getAllImplementations().forEach(identityEconomy -> {
                identityEconomy.withdrawPlayer(player, identityEconomy.getBalance(player));
            });
            selectedProfile = profile;
            PlotProfileManager plotProfileManager = director.getPlotProfileManager();
            PlotProfile cached = plotProfileManager.isCached(getSelectedProfileIdentification())
                    .orElse(null);
            if (cached != null) {
                if (player != Bukkit.getPlayer(uuid))
                    return;
                Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
                    cached.join(player);
                });
                return;
            }
            CompletableFuture<PlotProfile> download = director
                    .getPlotProfileManager().download(getSelectedProfileIdentification(), this);
            download.whenComplete((plotProfile, throwable) -> {
                if (throwable != null) {
                    if (ifFail != null && player == Bukkit.getPlayer(uuid))
                        ifFail.accept(player);
                    throwable.printStackTrace();
                    return;
                }
                if (player != Bukkit.getPlayer(uuid)) {
                    plotProfile.freePlot();
                    return;
                }
                Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
                    plotProfile.join(player);
                });
            });
        };
        old.close(player, onSwitch, onSwitch);
        return ProfileSwitchResult.SUCCESS;
    }

    /**
     * Joins to the profile of an existent player.
     *
     * @param existent The existent player to join to.
     * @return True if the player was successfully joined.
     */
    public boolean joinProfile(@NotNull Player existent) {
        Objects.requireNonNull(existent, "'existent' cannot be null");
        Player player = getPlayer();
        if (player == null)
            return false;
        if (profiles.size() >= director.getConfigManager().getMaxProfilesPerPlayer()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Max-Profiles-Reached", player)
                    .handle(player);
            return false;
        }
        PlotProprietorProfile proprietorProfile = getProfile();
        if (proprietorProfile == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", player)
                    .handle(player);
            return false;
        }
        PlotProfile old = proprietorProfile.getPlotProfile();
        if (old.isPlacingQueued()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Plot-Still-Loading", player)
                    .handle(player);
            return false;
        }
        Runnable onSwitch = () -> {
            unlinkProfile();
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            elasticEconomy.getAllImplementations().forEach(identityEconomy -> {
                identityEconomy.withdrawPlayer(player, identityEconomy.getBalance(player));
            });
            TycoonPlayer existentTycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(existent);
            if (existentTycoonPlayer == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                throw new NullPointerException("The existent player is not inside the plugin cache.");
            }
            PlotProprietorProfile plotProprietorProfile = existentTycoonPlayer.getProfile();
            if (plotProprietorProfile == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Inside-Plugin-Cache", player)
                        .handle(player);
                throw new NullPointerException("The existent player is not linked.");
            }
            PlotProfile plotProfile = plotProprietorProfile.getPlotProfile();
            String randomProfileName = BlobTycoonInternalAPI.getInstance().getRandomProfileName(profileNames.values().stream().toList());
            plotProfile.addProprietor(new PlotProprietorProfile(randomProfileName,
                    player.getName(), getIdentification(),
                    ProfileInventory.empty(), plotProfile, null));
            addProfile(plotProfile, true); //selects the profile
            Bukkit.getScheduler().runTask(director.getPlugin(), () -> {
                plotProfile.join(player);
            });
        };
        old.close(player, onSwitch, onSwitch);
        return true;
    }

    public ProfileDeleteResult deleteSelectedProfile() {
        Player player = getPlayer();
        if (player == null)
            return ProfileDeleteResult.PLAYER_NOT_FOUND;
        if (profiles.size() == 1) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Only-Profile", player)
                    .handle(player);
            return ProfileDeleteResult.ONLY_PROFILE;
        }
        PlotProprietorProfile proprietorProfile = getProfile();
        if (proprietorProfile == null) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("Player.Not-Inside-Plugin-Cache", player)
                    .handle(player);
            return ProfileDeleteResult.PLAYER_NOT_IN_CACHE;
        }
        int loadProfileIndex = 0;
        for (Map.Entry<Integer, String> entry : profiles.entrySet()) {
            if (entry.getKey() == selectedProfile)
                continue;
            loadProfileIndex = entry.getKey();
            break;
        }
        PlotProfile remove = proprietorProfile.getPlotProfile();
        if (remove.isPlacingQueued()) {
            BlobLibMessageAPI.getInstance()
                    .getMessage("BlobTycoon.Plot-Still-Loading", player)
                    .handle(player);
            return ProfileDeleteResult.PLACING_QUEUED;
        }
        boolean shouldRemove = remove.getProprietors().size() == 1;
        removeProfile(selectedProfile);
        switchProfile(loadProfileIndex, null, shouldRemove);
        return ProfileDeleteResult.SUCCESS;
    }

    public List<ProfileData> getProfileData() {
        List<ProfileData> profileData = new ArrayList<>();
        profiles.forEach((index, id) -> {
            String profileName = profileNames.get(index);
            profileData.add(ProfileData.of(id, profileName, index));
        });
        return profileData;
    }

    public void setConfiguration(@NotNull String key,
                                 @NotNull String value) {
        Objects.requireNonNull(key, "'key' cannot be null");
        Objects.requireNonNull(value, "'value' cannot be null");
        configuration.put(key, value);
    }

    @Nullable
    public String getConfiguration(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        return configuration.get(key);
    }

    @NotNull
    public TradeUserInfo getTradeUserInfo() {
        return tradeUserInfo;
    }
}
