package us.mytheria.blobtycoon.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.exception.ConfigurationFieldException;

public record StructureData(@NotNull Vector getArea,
                            @NotNull Vector getRemovePivot,
                            @Nullable Vector getRemoveRelativeOffset,
                            @Nullable Vector getTranslation,
                            @Nullable Vector getSelectorOffset) {

    /**
     * Will read StructureData from any ConfigurationSection by providing a path
     * to the ConfigurationSection containing the StructureData.
     * Example: StructureData.READ(plugin.getConfig(), "StructureData")
     *
     * @param section The ConfigurationSection to read from
     * @param path    The path to the ConfigurationSection
     * @return The StructureData
     */
    @NotNull
    public static StructureData READ(ConfigurationSection section, String path) {
        ConfigurationSection dataSection = section.getConfigurationSection(path);
        if (dataSection == null)
            throw new ConfigurationFieldException("'" + path + "' does not exist");
        ConfigurationSection areaSection = dataSection.getConfigurationSection("Area");
        if (areaSection == null)
            throw new ConfigurationFieldException("'" + path + ".Area' does not exist");
        Vector area = vectorOrFail(areaSection, path + ".Area");
        ConfigurationSection removePivotSection = dataSection.getConfigurationSection("Remove-Pivot");
        if (removePivotSection == null)
            throw new ConfigurationFieldException("'" + path + ".Remove-Pivot' does not exist");
        Vector removePivot = vectorOrFail(removePivotSection, path + ".Remove-Pivot");
        Vector removeRelativeOffset = null;
        ConfigurationSection removeRelativeOffsetSection = dataSection.getConfigurationSection("Remove-Relative-Offset");
        if (removeRelativeOffsetSection != null)
            removeRelativeOffset = vectorOrFail(removeRelativeOffsetSection, path + ".Remove-Relative-Offset");
        Vector translation = null;
        ConfigurationSection translationSection = dataSection.getConfigurationSection("Translation");
        if (translationSection != null) {
            translation = vectorOrFail(translationSection, path + ".Translation");
        }
        Vector selectorOffset = null;
        ConfigurationSection selectorOffsetSection = dataSection.getConfigurationSection("Selector-Offset");
        if (selectorOffsetSection != null) {
            selectorOffset = vectorOrFail(selectorOffsetSection, path + ".Selector-Offset");
        }
        return new StructureData(area, removePivot, removeRelativeOffset, translation, selectorOffset);
    }

    /**
     * Will read StructureData from any YamlConfiguration.
     * Expected path: Structure-Data
     *
     * @param fileConfiguration The YamlConfiguration to read from
     * @return The StructureData
     */
    @NotNull
    public static StructureData READ(YamlConfiguration fileConfiguration) {
        return READ(fileConfiguration, "Structure-Data");
    }

    public static void WRITE(StructureData structureData, ConfigurationSection section) {
        ConfigurationSection areaSection = section.createSection("Area");
        vectorWrite(areaSection, structureData.getArea());
        ConfigurationSection removePivotSection = section.createSection("Remove-Pivot");
        vectorWrite(removePivotSection, structureData.getRemovePivot());
        Vector removeRelativeOffset = structureData.getRemoveRelativeOffset();
        if (removeRelativeOffset != null && isSignificant(removeRelativeOffset)) {
            ConfigurationSection removeRelativeOffsetSection = section.createSection("Remove-Relative-Offset");
            vectorWrite(removeRelativeOffsetSection, removeRelativeOffset);
        }
        Vector translation = structureData.getTranslation();
        if (translation != null && isSignificant(translation)) {
            ConfigurationSection translationSection = section.createSection("Translation");
            vectorWrite(translationSection, translation);
        }
        Vector selectorOffset = structureData.getSelectorOffset();
        if (selectorOffset != null && isSignificant(selectorOffset)) {
            ConfigurationSection selectorOffsetSection = section.createSection("Selector-Offset");
            vectorWrite(selectorOffsetSection, selectorOffset);
        }
    }

    private static Vector vectorOrFail(ConfigurationSection section, String path) {
        if (!section.isInt("X"))
            throw new ConfigurationFieldException("'" + path + ".X' is not an integer");
        if (!section.isInt("Y"))
            throw new ConfigurationFieldException("'" + path + ".Y' is not an integer");
        if (!section.isInt("Z"))
            throw new ConfigurationFieldException("'" + path + ".Z' is not an integer");
        return new Vector(section.getInt("X"),
                section.getInt("Y"),
                section.getInt("Z"));
    }

    private static void vectorWrite(ConfigurationSection section, Vector vector) {
        section.set("X", vector.getBlockX());
        section.set("Y", vector.getBlockY());
        section.set("Z", vector.getBlockZ());
    }

    private static boolean isSignificant(Vector vector) {
        return vector.getBlockX() != 0 || vector.getBlockY() != 0 || vector.getBlockZ() != 0;
    }
}