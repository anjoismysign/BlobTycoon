package us.mytheria.blobtycoon.entity;

import me.anjoismysign.blobpets.entity.petexpansion.PetExpansion;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.exception.ConfigurationFieldException;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.tycoonattribute.TycoonAttributeModifier;
import us.mytheria.blobtycoon.entity.tycoonattribute.TycoonAttributeOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record TycoonPet(@NotNull Map<String, List<TycoonAttributeModifier>> getCurrencyAttributeModifiers,
                        @NotNull String getBlobPetKey,
                        @NotNull String getKey) implements PetExpansion {

    public static TycoonPet fromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        if (!config.isString("BlobPet"))
            throw new ConfigurationFieldException("'BlobPet' is not valid or set");
        String blobPetKey = config.getString("BlobPet");
        if (!config.isConfigurationSection("Currencies"))
            throw new ConfigurationFieldException("'Currencies' is not valid or set");
        Map<String, List<TycoonAttributeModifier>> attributeModifiers = new HashMap<>();
        ConfigurationSection currenciesSection = config.getConfigurationSection("Currencies");
        currenciesSection.getKeys(false).forEach(currency -> {
            if (!currenciesSection.isConfigurationSection(currency))
                throw new ConfigurationFieldException("Attribute '" + currency + "' is not valid");
            ConfigurationSection currencySection = currenciesSection.getConfigurationSection(currency);
            if (!currencySection.isDouble("Amount"))
                throw new ConfigurationFieldException("Attribute '" + currency + "' has an invalid amount (DECIMAL NUMBER)");
            double amount = currencySection.getDouble("Amount");
            if (!currencySection.isString("Operation"))
                throw new ConfigurationFieldException("Attribute '" + currency + "' is missing 'Operation' field");
            TycoonAttributeOperation operation = TycoonAttributeOperation.fromName(currencySection.getString("Operation"));
            if (operation == null)
                throw new ConfigurationFieldException("Attribute '" + currency + "' has an invalid operation");
            attributeModifiers.computeIfAbsent(currency, k -> new ArrayList<>())
                    .add(TycoonAttributeModifier.of(amount, operation));
        });
        return new TycoonPet(attributeModifiers, blobPetKey, key);
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("BlobPet", getBlobPetKey);
        try {
            yamlConfiguration.save(file);
        } catch ( Exception exception ) {
            exception.printStackTrace();
        }
        return file;
    }

    public void apply(@NotNull Player player, int holdIndex) {
        Objects.requireNonNull(player, "'player' cannot be null");
        getCurrencyAttributeModifiers.forEach((currency, list) -> {
            list.forEach(attributeModifier -> {
                TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
                if (tycoonPlayer == null)
                    return;
                PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
                if (attributeModifier.getOperation() == TycoonAttributeOperation.ADD)
                    plotProfile.addTransientEarner(currency, attributeModifier.getAmount());
                else
                    plotProfile.addTransientScalarEarner(currency, attributeModifier.getAmount());
            });
        });
    }

    public void unapply(@NotNull Player player, int holdIndex) {
        Objects.requireNonNull(player, "'player' cannot be null");
        TycoonPlayer tycoonPlayer = BlobTycoonInternalAPI.getInstance().getTycoonPlayer(player);
        if (tycoonPlayer == null)
            return;
        PlotProfile plotProfile = tycoonPlayer.getProfile().getPlotProfile();
        getCurrencyAttributeModifiers.forEach((currency, list) -> {
            list.forEach(attributeModifier -> {
                if (attributeModifier.getOperation() == TycoonAttributeOperation.ADD)
                    plotProfile.subtractTransientEarner(currency, attributeModifier.getAmount());
                else
                    plotProfile.subtractTransientScalarEarner(currency, attributeModifier.getAmount());
            });
        });
    }
}
