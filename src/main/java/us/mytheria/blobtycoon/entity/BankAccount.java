package us.mytheria.blobtycoon.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.mytheria.bloblib.api.BlobLibEconomyAPI;
import us.mytheria.bloblib.entities.currency.Currency;
import us.mytheria.bloblib.vault.multieconomy.ElasticEconomy;
import us.mytheria.blobtycoon.blobeconomy.BlobEconomyMiddleman;
import us.mytheria.blobtycoon.util.TycoonUnit;

import java.util.*;

public interface BankAccount {
    Map<UUID, Map<String, Double>> getBankBalances();

    /**
     * Gets the user account of the specified uuid
     *
     * @param uuid the uuid of the player
     * @return the account
     */
    default Map<String, Double> getUserAccount(@NotNull UUID uuid) {
        return getBankBalances().computeIfAbsent(uuid, k -> new HashMap<>());
    }

    /**
     * Gets the balance of the specified currency
     *
     * @param currency the currency
     * @param uuid     the uuid of the player
     * @return the balance
     */
    default double getBalance(@NotNull String currency,
                              @NotNull UUID uuid) {
        return getUserAccount(uuid).getOrDefault(currency, 0.0);
    }

    /**
     * Checks if the specified currency has the specified amount
     *
     * @param currency the currency
     * @param amount   the amount
     * @param uuid     the uuid of the player
     * @return whether the currency has the amount
     */
    default boolean hasBankAmount(@NotNull String currency,
                                  double amount,
                                  @NotNull UUID uuid) {
        return getBalance(currency, uuid) >= amount;
    }

    /**
     * Parses the balance of the specified currency
     *
     * @param currency the currency
     * @param uuid     the uuid of the player
     * @return the parsed balance
     */
    default String parseBankBalance(@NotNull String currency,
                                    @NotNull UUID uuid) {
        double balance = getBalance(currency, uuid);
        return TycoonUnit.THOUSANDS_SEPARATOR.format(balance);
    }

    /**
     * Deposits the specified amount into the specified currency
     *
     * @param currency the currency
     * @param amount   the amount
     * @param uuid     the uuid of the player
     * @return the end balance
     */
    default double depositBank(@NotNull String currency,
                               double amount,
                               @NotNull UUID uuid) {
        double endBalance = getBalance(currency, uuid) + amount;
        getUserAccount(uuid).put(currency, endBalance);
        return endBalance;
    }

    /**
     * Withdraws the specified amount from the specified currency
     *
     * @param currency the currency
     * @param amount   the amount
     * @param uuid     the uuid of the player
     * @return whether the transaction was successful
     */
    default boolean withdrawBank(@NotNull String currency,
                                 double amount,
                                 @NotNull UUID uuid) {
        double endBalance = getBalance(currency, uuid) - amount;
        if (endBalance < 0)
            return false;
        getUserAccount(uuid).put(currency, endBalance);
        return true;
    }

    /**
     * Clears the player's bank account and digital wallet
     *
     * @param uuid the UUID of the player involved
     */
    default void reset(@NotNull UUID uuid,
                       boolean rebirth) {
        if (rebirth) {
            Player player = Bukkit.getPlayer(uuid);
            ElasticEconomy elasticEconomy = BlobLibEconomyAPI.getInstance().getElasticEconomy();
            Set<String> used = new HashSet<>();
            elasticEconomy.getAllImplementations().forEach(economy -> {
                String name = economy.getName();
                Currency currency = BlobEconomyMiddleman.getInstance().getCurrency(name);
                used.add(name);
                if (currency == null || !currency.isPersistent()) {
                    if (player != null)
                        economy.withdrawPlayer(player, economy.getBalance(player));
                    withdrawBank(name, getBalance(name, uuid), uuid);
                }
            });
            Map<String, Double> userAccount = getUserAccount(uuid);
            Map<String, Double> dupe = new HashMap<>(userAccount);
            dupe.forEach((currency, amount) -> {
                if (used.contains(currency))
                    return;
                userAccount.put(currency, 0.0);
            });
        } else
            getBankBalances().remove(uuid);
    }
}
