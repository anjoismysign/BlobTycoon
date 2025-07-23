package io.github.anjoismysign.blobtycoon.entity;

import org.jetbrains.annotations.NotNull;

public interface Sellable {
    @NotNull
    String getSellingCurrency();

    @NotNull
    String getBuyingCurrency();
    
    double getSellingPrice();

    double getBuyingPrice();
}
