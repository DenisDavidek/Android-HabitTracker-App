package net.samclarke.android.habittracker.util;

public class FormatUtils {
    static final double POUNDS_TO_KILOGRAMS = 0.45359237;

    public double poundsToKilograms(double pounds) {
        return pounds * POUNDS_TO_KILOGRAMS;
    }

    public double convertKilogramsToPounds(double kg) {
        return kg / POUNDS_TO_KILOGRAMS;
    }
}
