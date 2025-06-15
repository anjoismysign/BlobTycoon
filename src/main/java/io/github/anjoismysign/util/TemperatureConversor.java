package io.github.anjoismysign.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a temperature conversor
 *
 * @param getDefaultUnit the default temperature unit
 */
public record TemperatureConversor(@NotNull TemperatureUnit getDefaultUnit) {
    /**
     * Creates a new TemperatureConversor
     *
     * @param defaultUnit the default temperature unit
     * @return the new TemperatureConversor
     */
    public static TemperatureConversor of(@NotNull TemperatureUnit defaultUnit) {
        Objects.requireNonNull(defaultUnit);
        return new TemperatureConversor(defaultUnit);
    }

    /**
     * Will parse the degree to the specified getDefaultUnit
     *
     * @param degree the degree
     * @param to     the getDefaultUnit
     * @return the parsed degree
     */
    public String parse(double degree, TemperatureUnit to) {
        if (getDefaultUnit == to)
            return to.parse(degree);
        double temperature;
        if (getDefaultUnit == TemperatureUnit.CELSIUS)
            temperature = toFahrenheit(degree);
        else
            temperature = toCelsius(degree);
        return to.parse(temperature);
    }

    public String parse(double degree, @NotNull String locale) {
        if (locale.equals("en_us"))
            return parse(degree, TemperatureUnit.FAHRENHEIT);
        return parse(degree, TemperatureUnit.CELSIUS);
    }

    /**
     * Will parse the degree to Celsius
     *
     * @param degree the degree
     * @return the parsed degree
     */
    public String parse(double degree) {
        return parse(degree, TemperatureUnit.CELSIUS);
    }

    private double toFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    private double toCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

}
