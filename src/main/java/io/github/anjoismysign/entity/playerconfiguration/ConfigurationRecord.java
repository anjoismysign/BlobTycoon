package io.github.anjoismysign.entity.playerconfiguration;

import org.jetbrains.annotations.NotNull;

public abstract class ConfigurationRecord<T> {

    /**
     * Gets the key of this ConfigurationRecord
     *
     * @return the key of this ConfigurationRecord
     */
    @NotNull
    public abstract String getKey();

    /**
     * Gets the value of this ConfigurationRecord.
     * If the configuration does not have a value, this method will return the default value and set the configuration to the default value.
     *
     * @param configuration the configuration to get the value from
     * @param defaultValue  the default value to set the configuration to if the configuration does not have a value
     * @return the value of this ConfigurationRecord
     */
    @NotNull
    public abstract T getOrDefault(
            @NotNull PlayerConfiguration configuration,
            @NotNull T defaultValue);

    /**
     * Sets the value of this ConfigurationRecord
     *
     * @param configuration the configuration to set the value to
     * @param value         the value to set the configuration to
     */
    public abstract void setConfiguration(@NotNull PlayerConfiguration configuration,
                                          @NotNull T value);
}
