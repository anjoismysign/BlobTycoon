package io.github.anjoismysign.util;

import java.text.NumberFormat;

public class ThousandsSeparator {
    private static ThousandsSeparator instance;
    private final NumberFormat format;

    public String numberFormat(Double number) {
        return format.format(number).replace("\u00A0", ",");
    }


    public static ThousandsSeparator getInstance() {
        if (instance == null)
            instance = new ThousandsSeparator();
        return instance;
    }

    private ThousandsSeparator() {
        format = NumberFormat.getInstance();
        format.setGroupingUsed(true);
        format.setMaximumFractionDigits(6);
    }

    public String format(double value) {
        if (Math.abs(value) < 0.0000000000000001)
            return "0";
        return format.format(value);
    }
}
