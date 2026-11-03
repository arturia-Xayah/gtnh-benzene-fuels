package dev.benzenefuels;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/** Decimal display only; exact generation/accounting never passes through doubles. */
public final class GeneratorFormat {

    private static final BigInteger THOUSAND = BigInteger.valueOf(1000);
    private static final BigInteger TRILLION = BigInteger.TEN.pow(12);

    private GeneratorFormat() {}

    public static String power(BigInteger value) {
        BigInteger n = value.max(BigInteger.ZERO);
        if (n.compareTo(TRILLION) > 0) {
            int exponent = n.toString()
                .length() - 1;
            return new BigDecimal(n).movePointLeft(exponent)
                .setScale(2, RoundingMode.DOWN)
                .toPlainString() + "E" + exponent;
        }
        String[] suffix = { "", "K", "M", "G" };
        int unit = 0;
        BigInteger divisor = BigInteger.ONE;
        while (unit < 3 && n.compareTo(divisor.multiply(THOUSAND)) >= 0) {
            divisor = divisor.multiply(THOUSAND);
            unit++;
        }
        return new BigDecimal(n).divide(new BigDecimal(divisor), 2, RoundingMode.DOWN)
            .toPlainString() + suffix[unit];
    }

    public static String volume(long units, long divisor) {
        return BigDecimal.valueOf(Math.max(0, units))
            .divide(BigDecimal.valueOf(divisor))
            .stripTrailingZeros()
            .toPlainString();
    }

    public static String percentage(BigInteger delivered, BigInteger generated) {
        if (generated.signum() <= 0) return "0.00";
        return new BigDecimal(
            delivered.max(BigInteger.ZERO)
                .min(generated)).multiply(BigDecimal.valueOf(100))
                    .divide(new BigDecimal(generated), 2, RoundingMode.DOWN)
                    .toPlainString();
    }
}
