package us.mytheria.blobtycoon.entity.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import us.mytheria.blobtycoon.BlobTycoonInternalAPI;
import us.mytheria.blobtycoon.entity.TycoonPlayer;
import us.mytheria.blobtycoon.util.TemperatureConversor;

import java.util.Map;
import java.util.function.Consumer;

public interface MechanicsOperator {
    /**
     * Gets the temperature conversor
     *
     * @return the temperature conversor
     */
    TemperatureConversor getTemperatureConversor();

    /**
     * Should not be called, use MechanicsOperator methods instead
     *
     * @return The map of mechanics
     */
    Map<String, Double> getMechanicsData();

    /**
     * Gets the loaded mechanics
     *
     * @return the loaded mechanics
     */
    @Nullable
    Map<String, Mechanics> getMechanics();

    BukkitTask mechanicsTimer();

    /**
     * Will reload the mechanics of this MechanicsOperator
     */
    void reloadMechanicsOperator();

    /**
     * Will consume an online TycoonPlayer that belongs to this MechanicsOperator
     *
     * @param consumer The consumer
     */
    void forEachOnlineProprietor(Consumer<TycoonPlayer> consumer);

    /**
     * Gets the production of a MechanicsData
     *
     * @param mechanicsData The MechanicsData
     * @return The production
     */
    default double getProduction(@NotNull MechanicsData mechanicsData) {
        return getMechanicsData().getOrDefault(mechanicsData.getProductionKey(), 0.0);
    }

    /**
     * Adds production to a MechanicsData
     *
     * @param mechanicsData The MechanicsData
     * @param production    The production to add
     */
    default void addProduction(@NotNull MechanicsData mechanicsData, double production) {
        getMechanicsData().put(mechanicsData.getProductionKey(), getProduction(mechanicsData) + production);
    }

    /**
     * Subtracts production from a MechanicsData
     *
     * @param mechanicsData The MechanicsData
     * @param production    The production to subtract
     */
    default void subtractProduction(@NotNull MechanicsData mechanicsData, double production) {
        getMechanicsData().put(mechanicsData.getProductionKey(), getProduction(mechanicsData) - production);
    }

    /**
     * Gets the consumption of a MechanicsData
     *
     * @param mechanicsData The MechanicsData
     * @return The consumption
     */
    default double getConsumption(@NotNull MechanicsData mechanicsData) {
        SideChannelMechanics sideChannelMechanics = mechanicsData.getSideChannelMechanics();
        if (sideChannelMechanics != null) {
            MechanicsData source = BlobTycoonInternalAPI.getInstance()
                    .getMechanicsData(sideChannelMechanics.getSource());
            if (source == null) {
                Bukkit.getPluginManager().getPlugin("BlobTycoon")
                        .getLogger().warning("SideChannelMechanics source not found '" + sideChannelMechanics.getSource()
                                + "' inside '" + mechanicsData.getKey() + "'. Is plugin reloading? Consumption will be temporarily set to MAX_VALUE");
                return Double.MAX_VALUE;
            }
            double sourceConsumption = getConsumption(source);
            double consumption = sourceConsumption + sideChannelMechanics.getThreshold();
            consumption = sourceConsumption / consumption;
            consumption = sourceConsumption * consumption;
            return consumption;
        }
        return getMechanicsData().getOrDefault(mechanicsData.getConsumptionKey(), 0.0);
    }

    /**
     * Adds consumption to a MechanicsData
     *
     * @param mechanicsData The MechanicsData
     * @param consumption   The consumption to add
     */
    default void addConsumption(@NotNull MechanicsData mechanicsData, double consumption) {
        if (mechanicsData.getSideChannelMechanics() != null)
            return;
        getMechanicsData().put(mechanicsData.getConsumptionKey(), getConsumption(mechanicsData) + consumption);
    }

    /**
     * Subtracts consumption from a MechanicsData
     *
     * @param mechanicsData The MechanicsData
     * @param consumption   The consumption to subtract
     */
    default void subtractConsumption(@NotNull MechanicsData mechanicsData, double consumption) {
        if (mechanicsData.getSideChannelMechanics() != null)
            return;
        getMechanicsData().put(mechanicsData.getConsumptionKey(), getConsumption(mechanicsData) - consumption);
    }
}
