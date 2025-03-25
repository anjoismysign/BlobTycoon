package us.mytheria.blobtycoon.entity.valuable;

import org.bukkit.configuration.file.YamlConfiguration;
import us.mytheria.bloblib.entities.BlobObject;

import java.io.File;

public record Valuable(String getKey, String getCurrency, String getDriver)
        implements BlobObject {

    @Override
    public File saveToFile(File directory) {
        File file = instanceFile(directory);
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        yamlConfiguration.set("Currency", getCurrency);
        yamlConfiguration.set("Driver", getDriver);
        try {
            yamlConfiguration.save(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


}
