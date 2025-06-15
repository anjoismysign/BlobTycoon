package io.github.anjoismysign.entity.mechanics;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.BlobObject;
import io.github.anjoismysign.bloblib.utilities.Formatter;
import io.github.anjoismysign.util.TemperatureConversor;
import io.github.anjoismysign.util.TemperatureUnit;
import io.github.anjoismysign.util.ThousandsSeparator;

import java.io.File;
import java.util.Objects;

public record MechanicsData(@NotNull String getKey,
                            boolean isEnabled,
                            @NotNull String getShortening,
                            @NotNull String getDisplay,
                            @Nullable SideChannelMechanics getSideChannelMechanics,
                            double getDefaultAmount)
        implements BlobObject {

    private static TemperatureConversor CONVERSOR = TemperatureConversor.of(TemperatureUnit.CELSIUS);

    public static MechanicsData fromFile(File file) {
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        String key = file.getName().replace(".yml", "");
        boolean enabled = yamlConfiguration.getBoolean("Enabled");
        String shortening = yamlConfiguration.getString("Shortening");
        String display = yamlConfiguration.getString("Display");
        @Nullable SideChannelMechanics sideChannelMechanics = SideChannelMechanics.READ(yamlConfiguration);
        double defaultAmount = yamlConfiguration.getDouble("Default-Amount", 0);
        return new MechanicsData(key, enabled, shortening, display, sideChannelMechanics, defaultAmount);
    }

    /**
     * @return the consumption key
     */
    @NotNull
    public String getConsumptionKey() {
        return getKey + "consumption";
    }

    /**
     * @return the production key
     */
    @NotNull
    public String getProductionKey() {
        return getKey + "production";
    }

    /**
     * Displays the amount in the mechanics' display format.
     *
     * @param amount       the amount to display
     * @param locale       the locale to use
     * @param noConversion the string to use if no conversion is needed. If null, the locale will be used.
     * @return the formatted amount
     */
    @NotNull
    public String display(double amount,
                          @Nullable String locale,
                          @Nullable String noConversion) {
        String blobTycoon = getDisplay().replace("%thousandsSeparator%", ThousandsSeparator.getInstance().format(amount))
                .replace("%temperature%", CONVERSOR.parse(amount,
                        noConversion == null ? locale : noConversion));
        return Formatter.getInstance().formatAll(blobTycoon, amount);
    }

    /**
     * Displays the amount in the mechanics' display format.
     *
     * @param amount the amount to display
     * @param player the player to get the locale from
     * @return the formatted amount
     */
    @NotNull
    public String display(double amount, @NotNull Player player) {
        Objects.requireNonNull(player, "'player' cannot be null");
        return display(amount, player.getLocale(), null);
    }

    /**
     * Displays the amount in the mechanics' display format.
     * Uses en_us as the locale.
     *
     * @param amount the amount to display
     * @return the formatted amount
     */
    @NotNull
    public String display(double amount) {
        return display(amount, "en_us", null);
    }

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("Enabled", isEnabled());
        yamlConfiguration.set("Shortening", getShortening());
        yamlConfiguration.set("Display", getDisplay());
        if (getSideChannelMechanics != null)
            getSideChannelMechanics.write(yamlConfiguration);
        yamlConfiguration.set("Default-Amount", getDefaultAmount());
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    @NotNull
    public Mechanics instantiate(@NotNull MechanicsOperator operator) {
        if (!isEnabled)
            throw new IllegalStateException("Cannot instantiate disabled mechanics.");
        Objects.requireNonNull(operator, "'operator' cannot be null");
        return new Mechanics() {

            @Override
            public @NotNull MechanicsOperator getOperator() {
                return operator;
            }

            @Override
            public boolean isFallingShort() {
                return operator.getProduction(MechanicsData.this)
                        < operator.getConsumption(MechanicsData.this);
            }

            @Override
            public @NotNull String getShortening() {
                return getShortening;
            }

            @Override
            public @NotNull String getKey() {
                return getKey;
            }
        };
    }
}
