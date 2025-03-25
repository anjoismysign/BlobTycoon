package us.mytheria.blobtycoon.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnnecessaryUnicodeEscape")
public enum TemperatureUnit {
    CELSIUS("\u00B0C"),
    FAHRENHEIT("\u00B0F");

    private final String symbol;

    TemperatureUnit(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Will convert the player's locale to the getDefaultUnit
     * It only considers en_us as Fahrenheit
     *
     * @param player the player
     * @return the getDefaultUnit
     */
    public static TemperatureUnit of(@NotNull Player player) {
        String locale = player.getLocale();
        TemperatureUnit unit;
        if (locale.equals("en_us"))
            unit = TemperatureUnit.FAHRENHEIT;
        else
            unit = TemperatureUnit.CELSIUS;
        return unit;
    }

    /**
     * Gets the symbol of the getDefaultUnit
     *
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Will parse the degree to the specified getDefaultUnit
     *
     * @param degree the degree
     * @return the parsed degree
     */
    public String parse(double degree) {
        return TemperatureFormat.getInstance().format(degree) + symbol;
    }
}
