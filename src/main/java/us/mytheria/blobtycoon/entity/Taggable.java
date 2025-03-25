package us.mytheria.blobtycoon.entity;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.bloblib.entities.tag.TagSet;

import java.util.Objects;

public interface Taggable {

    @NotNull
    static String read(@NotNull ConfigurationSection section) {
        Objects.requireNonNull(section, "'section' cannot be null");
        if (!section.isString("TagSet"))
            return "BlobTycoon.Generic-TagSet";
        return Objects.requireNonNull(section.getString("TagSet"),
                "'TagSet' cannot be null");
    }

    /**
     * Gets the key for the TagSet.
     *
     * @return The key.
     */
    @NotNull
    String getTagSetKey();

    /**
     * Serializes the TagSet key to the ConfigurationSection.
     *
     * @param section The ConfigurationSection to serialize to.
     */
    default void serializeTag(@NotNull ConfigurationSection section) {
        Objects.requireNonNull(section, "'section' cannot be null");
        section.set("TagSet", getTagSetKey());
    }

    /**
     * Gets the TagSet for this Taggable.
     *
     * @return The TagSet.
     */
    @NotNull
    default TagSet getTagSet() {
        return Objects.requireNonNull(TagSet.by(getTagSetKey()),
                "TagSet not found for key: " + getTagSetKey());
    }

    default boolean isCompatible(@NotNull Taggable other,
                                 @Nullable String key) {
        Objects.requireNonNull(other, "'other' cannot be null");
        if (other.getTagSetKey().equals(getTagSetKey()))
            return true;
        if (key == null)
            return isCompatible(other.getTagSet());
        else
            return isCompatible(key) || isCompatible(other.getTagSet());
    }

    /**
     * Checks if the TagSet of this Taggable is compatible with another TagSet.
     *
     * @param two The TagSet to check compatibility with.
     * @return True if compatible, false otherwise.
     */
    default boolean isCompatible(@NotNull TagSet two) {
        Objects.requireNonNull(two, "'two' cannot be null");
        TagSet one = getTagSet();
        return one.contains(two.identifier()) || two.identifier().equals(one.identifier());
    }

    /**
     * Checks if the TagSet of this Taggable is compatible with a key.
     *
     * @param key The key to check compatibility with.
     * @return True if compatible, false otherwise.
     */
    default boolean isCompatible(@NotNull String key) {
        Objects.requireNonNull(key, "'key' cannot be null");
        TagSet tagSet = getTagSet();
        return tagSet.contains(key) || key.equals(tagSet.identifier());
    }
}
