package us.mytheria.blobtycoon.director.manager;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibTranslatableAPI;
import us.mytheria.bloblib.entities.ComplexEventListener;
import us.mytheria.bloblib.entities.ConfigDecorator;
import us.mytheria.bloblib.entities.ListenersSection;
import us.mytheria.bloblib.entities.TinyEventListener;
import us.mytheria.bloblib.itemstack.ItemStackModder;
import us.mytheria.blobtycoon.director.TycoonManager;
import us.mytheria.blobtycoon.director.TycoonManagerDirector;
import us.mytheria.blobtycoon.entity.configuration.BlobTycoonConfiguration;
import us.mytheria.blobtycoon.ui.BlobTycoonUI;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TycoonConfigManager extends TycoonManager {
    private boolean tinyDebug;
    private Material emptyMaterial;
    private final List<String> profileNames;
    private final Set<Material> validFloor;
    private int maxPlacedPerSecond;
    private boolean teleportToPlotOnJoin;
    private int maxProfilesPerPlayer;
    private final ItemStack remove = new ItemStack(Material.BARRIER);
    private final ItemStack objectSlot = new ItemStack(Material.STRUCTURE_VOID);
    private final Set<String> allKeywords;
    private final Set<String> halfKeywords;
    private boolean notifyOwnerJoin;

    private final BlobTycoonConfiguration blobTycoonConfiguration;

    private TinyEventListener internalTransferFunds;
    private TinyEventListener newProfileKit;
    private TinyEventListener objectModelPlaceInteract;
    private TinyEventListener objectModelPlaceHit;
    private TinyEventListener objectModelRemoveInteract;
    private TinyEventListener objectModelRemoveHit;
    private TinyEventListener structureModelRemoveInteract;
    private TinyEventListener structureModelRemoveHit;
    private TinyEventListener translateOnProfileLoad;
    private TinyEventListener structureAssetHeldTutorial;
    private TinyEventListener rackAssetHeldTutorial;
    private TinyEventListener objectAssetHeldTutorial;
    private TinyEventListener plotHelperHit;
    private TinyEventListener plotHelperInteract;
    private TinyEventListener plotHelperPreventDamage;
    private TinyEventListener shopArticleTransferFunds;
    private TinyEventListener blobEconomyTransferFunds;

    private ComplexEventListener tycoonMenu;
    private ComplexEventListener offlineEarning;

    public TycoonConfigManager(TycoonManagerDirector managerDirector) {
        super(managerDirector);
        blobTycoonConfiguration = BlobTycoonConfiguration.getInstance(this);
        profileNames = new ArrayList<>();
        validFloor = new HashSet<>();
        allKeywords = new HashSet<>();
        halfKeywords = new HashSet<>();
        reload();
    }

    @Override
    public void reload() {
        allKeywords.clear();
        halfKeywords.clear();
        ConfigDecorator configDecorator = getPlugin().getConfigDecorator();
        ConfigurationSection settingsSection = configDecorator.reloadAndGetSection("Settings");
        blobTycoonConfiguration.reload(settingsSection);
        BlobTycoonUI.getInstance().reload();
        notifyOwnerJoin = settingsSection.getBoolean("Notify-Owner-Join");
        teleportToPlotOnJoin = settingsSection.getBoolean("Teleport-To-Plot-On-Join");
        maxProfilesPerPlayer = settingsSection.getInt("Max-Profiles-Per-Player");
        tinyDebug = settingsSection.getBoolean("Tiny-Debug");
        maxPlacedPerSecond = settingsSection.getInt("Max-Placed-Per-Second");
        emptyMaterial = Material.matchMaterial(settingsSection.getString(
                "Empty-Material"));
        if (emptyMaterial == null)
            emptyMaterial = Material.AIR;
        File dataFolder = getPlugin().getDataFolder();
        File structuresFolder = new File(dataFolder, "Structures");
        allKeywords.addAll(settingsSection.getStringList("All-Keywords"));
        halfKeywords.addAll(settingsSection.getStringList("Half-Keywords"));

        profileNames.clear();
        profileNames.addAll(settingsSection.getStringList("ProfileNames"));
        if (profileNames.isEmpty()) {
            profileNames.addAll(List.of("Alpha", "Beta", "Gamma", "Delta", "Epsilon",
                    "Zeta", "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi",
                    "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi",
                    "Psi", "Omega"));
        }
        validFloor.clear();
        validFloor.addAll(settingsSection.getStringList("Valid-Floor").stream()
                .map(Material::getMaterial)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));

        ListenersSection listeners = configDecorator.reloadAndGetListeners();
        tycoonMenu = listeners.complexEventListener("Tycoon-Menu");
        offlineEarning = listeners.complexEventListener("Offline-Earning");

        internalTransferFunds = listeners.tinyEventListener("Internal-Transfer-Funds");
        newProfileKit = listeners.tinyEventListener("New-Profile-Kit");
        objectModelPlaceInteract = listeners.tinyEventListener("ObjectModel-Place-Interact");
        objectModelPlaceHit = listeners.tinyEventListener("ObjectModel-Place-Hit");
        objectModelRemoveInteract = listeners.tinyEventListener("ObjectModel-Remove-Interact");
        objectModelRemoveHit = listeners.tinyEventListener("ObjectModel-Remove-Hit");
        structureModelRemoveInteract = listeners.tinyEventListener("StructureModel-Remove-Interact");
        structureModelRemoveHit = listeners.tinyEventListener("StructureModel-Remove-Hit");
        translateOnProfileLoad = listeners.tinyEventListener("Translate-On-Profile-Load-Event");
        structureAssetHeldTutorial = listeners.tinyEventListener("StructureAsset-Held-Tutorial");
        rackAssetHeldTutorial = listeners.tinyEventListener("RackAsset-Held-Tutorial");
        objectAssetHeldTutorial = listeners.tinyEventListener("ObjectAsset-Held-Tutorial");
        plotHelperHit = listeners.tinyEventListener("PlotHelper-Hit");
        plotHelperInteract = listeners.tinyEventListener("PlotHelper-Interact");
        plotHelperPreventDamage = listeners.tinyEventListener("PlotHelper-Prevent-Damage");
        shopArticleTransferFunds = listeners.tinyEventListener("ShopArticle-Transfer-Funds");
        blobEconomyTransferFunds = listeners.tinyEventListener("BlobEconomy-Transfer-Funds");
    }

    public TinyEventListener getInternalTransferFunds() {
        return internalTransferFunds;
    }

    public TinyEventListener getNewProfileKit() {
        return newProfileKit;
    }

    public TinyEventListener getObjectModelPlaceHit() {
        return objectModelPlaceHit;
    }

    public TinyEventListener getObjectModelPlaceInteract() {
        return objectModelPlaceInteract;
    }

    public TinyEventListener getObjectModelRemoveHit() {
        return objectModelRemoveHit;
    }

    public TinyEventListener getObjectModelRemoveInteract() {
        return objectModelRemoveInteract;
    }

    public TinyEventListener getStructureModelRemoveHit() {
        return structureModelRemoveHit;
    }

    public TinyEventListener getStructureModelRemoveInteract() {
        return structureModelRemoveInteract;
    }

    public TinyEventListener getTranslateOnProfileLoad() {
        return translateOnProfileLoad;
    }

    public TinyEventListener getStructureAssetHeldTutorial() {
        return structureAssetHeldTutorial;
    }

    public TinyEventListener getRackAssetHeldTutorial() {
        return rackAssetHeldTutorial;
    }

    public TinyEventListener getObjectAssetHeldTutorial() {
        return objectAssetHeldTutorial;
    }

    public TinyEventListener getPlotHelperHit() {
        return plotHelperHit;
    }

    public TinyEventListener getPlotHelperInteract() {
        return plotHelperInteract;
    }

    public TinyEventListener getPlotHelperPreventDamage() {
        return plotHelperPreventDamage;
    }

    public TinyEventListener getShopArticleTransferFunds() {
        return shopArticleTransferFunds;
    }

    public ComplexEventListener getTycoonMenu() {
        return tycoonMenu;
    }

    public ComplexEventListener getOfflineEarning() {
        return offlineEarning;
    }

    public boolean tinyDebug() {
        return tinyDebug;
    }

    public List<String> getProfileNames() {
        return profileNames;
    }

    public Set<Material> getValidFloor() {
        return validFloor;
    }

    public int getMaxPlacedPerSecond() {
        return maxPlacedPerSecond;
    }

    @NotNull
    public ItemStack getRemoveButton(Player player) {
        ItemStack clone = new ItemStack(remove);
        ItemStackModder.mod(clone).displayName(BlobLibTranslatableAPI.getInstance()
                .getTranslatableSnippet("BlobTycoon-Assets.Remove", player).get());
        return clone;
    }

    @NotNull
    public ItemStack getObjectSlot(Player player) {
        ItemStack clone = new ItemStack(objectSlot);
        ItemStackModder.mod(clone).displayName(BlobLibTranslatableAPI.getInstance()
                .getTranslatableSnippet("BlobTycoon-Assets.Object-Slot", player).get());
        return clone;
    }

    public Material getEmptyMaterial() {
        return emptyMaterial;
    }

    public boolean teleportToPlotOnJoin() {
        return teleportToPlotOnJoin;
    }

    public int getMaxProfilesPerPlayer() {
        return maxProfilesPerPlayer;
    }

    public Set<String> getAllKeywords() {
        return allKeywords;
    }

    public Set<String> getHalfKeywords() {
        return halfKeywords;
    }

    public boolean notifyOwnerJoin() {
        return notifyOwnerJoin;
    }
}