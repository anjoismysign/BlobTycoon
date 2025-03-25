package us.mytheria.blobtycoon.util;

import java.text.DecimalFormat;

public class TemperatureFormat {
    private static TemperatureFormat instance;
    private final DecimalFormat decimalFormat;

    public static TemperatureFormat getInstance() {
        if (instance == null)
            instance = new TemperatureFormat();
        return instance;
    }

    private TemperatureFormat() {
        this.decimalFormat = new DecimalFormat("#,##0.0");
    }

    public String format(double temperature) {
        return decimalFormat.format(temperature);
    }
}
