package us.mytheria.blobtycoon.entity;

import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.vault.multieconomy.ElasticEconomy;
import us.mytheria.blobtycoon.util.TycoonUnit;

import java.util.Map;

public interface ValuableAccount {
    Map<String, Double> getValuables();

    /**
     * Gets the balance of the specified valuable
     *
     * @param valuable the valuable
     * @return the balance
     */
    default double getValuable(String valuable) {
        return getValuables().getOrDefault(valuable, 0.0);
    }

    /**
     * Checks if the specified valuable has the specified amount
     *
     * @param valuable the valuable
     * @param amount   the amount
     * @return whether the valuable has the amount
     */
    default boolean hasValuableAmount(String valuable, double amount) {
        return getValuable(valuable) >= amount;
    }

    /**
     * Parses the balance of the specified valuable
     *
     * @param valuable the valuable
     * @return the parsed balance
     */
    default String parseValuableBalance(String valuable) {
        double balance = getValuable(valuable);
        ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
        boolean exists = elasticEconomy.existsImplementation(valuable);
        if (!exists)
            return TycoonUnit.THOUSANDS_SEPARATOR.format(balance);
        return elasticEconomy.getImplementation(valuable).format(balance);
    }

    /**
     * Deposits the specified amount into the specified valuable
     *
     * @param valuable the valuable
     * @param amount   the amount
     * @return the end balance
     */
    default double depositValuable(String valuable, double amount) {
        double endBalance = getValuable(valuable) + amount;
        getValuables().put(valuable, endBalance);
        return endBalance;
    }

    /**
     * Withdraws the specified amount from the specified valuable
     *
     * @param valuable the valuable
     * @param amount   the amount
     * @return whether the transaction was successful
     */
    default boolean withdrawValuable(String valuable, double amount) {
        double endBalance = getValuable(valuable) - amount;
        if (endBalance < 0)
            return false;
        getValuables().put(valuable, endBalance);
        return true;
    }
}
