package dev.benzenefuels;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/** Four significant digits for fuel data; independent of the two-decimal power display. */
public final class FuelTextFormat {

    private static final MathContext FOUR_DIGITS = new MathContext(4, RoundingMode.DOWN);

    private FuelTextFormat() {}

    public static String compact(BigDecimal value) {
        if (value.signum() < 0) throw new IllegalArgumentException("Negative fuel data");
        String[] suffix = { "", "K", "M", "G", "T", "P", "E" };
        int unit = 0;
        BigDecimal scaled = value;
        while (scaled.compareTo(BigDecimal.valueOf(1000)) >= 0 && unit < suffix.length - 1) {
            scaled = scaled.movePointLeft(3);
            unit++;
        }
        return scaled.round(FOUR_DIGITS)
            .stripTrailingZeros()
            .toPlainString() + suffix[unit];
    }

    public static String heat(long value) {
        return compact(BigDecimal.valueOf(value));
    }

    public static String ratio(long value, long reference) {
        if (reference <= 0) throw new IllegalArgumentException("Invalid reference heat");
        return compact(
            BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(reference), FOUR_DIGITS));
    }
}
