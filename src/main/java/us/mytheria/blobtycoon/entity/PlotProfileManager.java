package us.mytheria.blobtycoon.entity;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.api.BlobLibMessageAPI;
import us.mytheria.bloblib.entities.BlobCrudable;
import us.mytheria.bloblib.entities.DocumentDecorator;
import us.mytheria.bloblib.managers.BlobPlugin;
import us.mytheria.bloblib.storage.BlobCrudManager;
import us.mytheria.bloblib.storage.IdentifierType;
import us.mytheria.bloblib.storage.StorageType;
import us.mytheria.bloblib.utilities.BlobCrudManagerFactory;
import us.mytheria.blobtycoon.director.TycoonManager;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.exception.NoAvailablePlotsException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlotProfileManager extends TycoonManager implements Listener {
    protected final HashMap<String, PlotProfile> cache;
    protected BlobCrudManager<BlobCrudable> crudManager;
    private final BlobPlugin plugin;
    private final String tag;
    private final Function<ProfileInitData, PlotProfile> generator;

    public PlotProfileManager(TycoonManagerDirector director, boolean logActivity) {
        super(director);
        plugin = director.getPlugin();
        String crudableName = "PlotProfile";
        tag = plugin.getName() + "-" + crudableName;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        cache = new HashMap<>();
        this.generator = profileInitData ->
                new PlotProfile(profileInitData.crudable(), director,
                        profileInitData.plot());
        crudManager = BlobCrudManagerFactory.UUID(plugin, crudableName, uuid -> new BlobCrudable(uuid.toString()), logActivity);
    }

    public CompletableFuture<PlotProfile> createRandom(TycoonPlayer tycoonPlayer) {
        CompletableFuture<PlotProfile> profileFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                BlobCrudable crudable = crudManager.createRandomOrFail();
                Plot plot = getManagerDirector().getPlotManager().getFirstAvailable();
                PlotProfile applied = generator.apply(new ProfileInitData(crudable, plot));
                cache.put(applied.getIdentification(), applied);
                profileFuture.complete(applied);
            } catch (NoAvailablePlotsException e) {
                Player player = tycoonPlayer.getPlayer();
                if (player.hasPermission("blobtycoon.admin")) {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobTycoon.No-Available-Plots-Administrator", player)
                            .handle(player);
                } else
                    BlobLibMessageAPI.getInstance().getMessage(
                                    "BlobTycoon.No-Available-Plots", player)
                            .handle(player);
            } catch (Exception e) {
                profileFuture.completeExceptionally(e);
            }
        });
        return profileFuture;
    }

    public CompletableFuture<PlotProfile> download(String identification, TycoonPlayer tycoonPlayer) {
        CompletableFuture<PlotProfile> profileFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                BlobCrudable crudable = crudManager.read(identification);
                Plot plot = getManagerDirector().getPlotManager().getFirstAvailable();
                PlotProfile applied = generator.apply(new ProfileInitData(crudable, plot));
                cache.put(applied.getIdentification(), applied);
                profileFuture.complete(applied);
            } catch (NoAvailablePlotsException e) {
                Player player = tycoonPlayer.getPlayer();
                if (player.hasPermission("blobtycoon.admin")) {
                    BlobLibMessageAPI.getInstance()
                            .getMessage("BlobTycoon.No-Available-Plots-Administrator", player)
                            .handle(player);
                } else
                    BlobLibMessageAPI.getInstance().getMessage(
                                    "BlobTycoon.No-Available-Plots", player)
                            .handle(player);
            } catch (Exception e) {
                profileFuture.completeExceptionally(e);
            }
        });
        return profileFuture;
    }

    @Override
    public void unload() {
        getAll().forEach(PlotProfile::unload);
        saveAll(false);
    }

    @Override
    public void reload() {
        saveAll(true);
        getAll().forEach(PlotProfile::reload);
    }

    private void sendPluginMessage(Player player, Document document) {
        player.sendPluginMessage(plugin, tag, new DocumentDecorator(document).serialize());
    }

    /**
     * Adds the object to the cache.
     *
     * @param profile The profile to add.
     * @return The previous object. Null if no previous object was mapped.
     */
    @Nullable
    public PlotProfile addObject(PlotProfile profile) {
        return cache.put(profile.getIdentification(), profile);
    }

    /**
     * Removes the object from the cache.
     *
     * @param profile The profile to remove.
     * @return The object removed. Null if not found.
     */
    @Nullable
    public PlotProfile removeObject(PlotProfile profile) {
        return removeObject(profile.getIdentification());
    }

    /**
     * Removes the object from the cache.
     *
     * @param key The getKey of the object.
     * @return The object removed. Null if not found.
     */
    @Nullable
    public PlotProfile removeObject(String key) {
        return cache.remove(key);
    }

    /**
     * Deletes the object from the database.
     *
     * @param key The getKey of the object.
     */
    public void deleteObject(String key) {
        crudManager.delete(key);
    }

    public Optional<PlotProfile> isCached(String id) {
        return Optional.ofNullable(cache.get(id));
    }

    public void ifIsOnline(String id, Consumer<PlotProfile> consumer) {
        Optional<PlotProfile> optional = isCached(id);
        optional.ifPresent(consumer);
    }

    public void ifIsOnlineThenUpdateElse(String id, Consumer<PlotProfile> consumer,
                                         Runnable runnable) {
        Optional<PlotProfile> optional = isCached(id);
        boolean isPresent = optional.isPresent();
        if (isPresent) {
            PlotProfile serializable = optional.get();
            consumer.accept(serializable);
            crudManager.update(serializable.serializeAllAttributes());
        } else {
            runnable.run();
        }
    }

    public void uploadObject(@NotNull PlotProfile profile,
                             @Nullable Runnable runnable) {
        String index = profile.getPlot().getData().getIndex();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            crudManager.update(profile.serializeAllAttributes());
            cache.remove(profile.getIdentification());
            Bukkit.getScheduler().runTask(getPlugin(), () -> {
                getManagerDirector().getPlotManager().freePlayerPlot(index);
                if (runnable != null)
                    runnable.run();
            });
        });
    }

    public void ifIsOnlineThenUpdate(String id, Consumer<PlotProfile> consumer) {
        ifIsOnlineThenUpdateElse(id, consumer, () -> {
        });
    }

    public boolean exists(String key) {
        return crudManager.exists(key);
    }

    private void saveAll(boolean readd) {
        getAll().forEach(profile -> {
            save(profile, readd);
        });
    }

    private void save(@NotNull PlotProfile profile,
                      boolean readd) {
        Objects.requireNonNull(profile, "'profile' cannot be null");
        Plot plot = profile.getPlot();
        profile.saveEco();
        if (Bukkit.isPrimaryThread()) {
            profile.getCurrentExpansion().restructure(plot.getData());
            profile.disjoinOnlineProprietors(readd);
            crudManager.update(profile.serializeAllAttributes());
        } else {
            Bukkit.getScheduler().runTask(getPlugin(), () -> {
                profile.getCurrentExpansion().restructure(plot.getData());
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    profile.disjoinOnlineProprietors(readd);
                    crudManager.update(profile.serializeAllAttributes());
                });
            });
        }
    }

    public Collection<PlotProfile> getAll() {
        return cache.values();
    }

    public StorageType getStorageType() {
        return crudManager.getStorageType();
    }

    public IdentifierType getIdentifierType() {
        return crudManager.getIdentifierType();
    }
}
