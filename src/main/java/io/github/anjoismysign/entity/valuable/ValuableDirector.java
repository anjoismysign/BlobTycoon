package io.github.anjoismysign.entity.valuable;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.github.anjoismysign.bloblib.entities.ObjectDirector;
import io.github.anjoismysign.bloblib.entities.ObjectDirectorData;
import io.github.anjoismysign.director.TycoonManagerDirector;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ValuableDirector extends ObjectDirector<Valuable> {
    private static Map<String, String> valuables = new HashMap<>();

    public ValuableDirector(TycoonManagerDirector director) {
        super(director, ObjectDirectorData
                        .simple(director.getRealFileManager(), "Valuable"),
                file -> {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                    String key = file.getName().replace(".yml", "");
                    String currency = config.getString("Currency");
                    if (valuables.containsKey(currency)) {
                        director.getPlugin().getAnjoLogger().singleError("Duplicate currency '" + currency +
                                "' inside: " + file.getName());
                        return null;
                    }
                    String driver = config.getString("Driver");
                    valuables.put(currency, driver);
                    return new Valuable(key, currency, driver);
                }, false);
    }

    @Override
    public void reload() {
        valuables.clear();
        super.reload();
    }

    @Nullable
    public String getDriver(@NotNull String currency) {
        Objects.requireNonNull(currency);
        return valuables.get(currency);
    }

    @NotNull
    public Map<String, String> getValuables() {
        return Map.copyOf(valuables);
    }
}
