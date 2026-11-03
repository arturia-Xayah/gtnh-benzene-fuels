package dev.benzenefuels;

import java.math.BigDecimal;
import java.math.BigInteger;

/** Integer mB accounting. One configured whole litre is 1000 Forge fluid units. */
public final class GeneratorMath {

    public static final int TICKS = 100;
    public static final int UNITS_PER_LITRE = 1000;

    private GeneratorMath() {}

    /** User formula, real division and floor to a whole parallel. No coil is not a valid structure. */
    public static int coilParallelLimit(long heat) {
        if (heat < 901) return 0;
        double count = StrictMath.pow(4.0, (heat - 901) / 900.0 - 1);
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1, StrictMath.floor(count)));
    }

    public static long fuelUnits(long available, int maxLitres) {
        if (available < 1000 || maxLitres < 1) return 0;
        return Math.min(available / 1000, (long) maxLitres) * 1000;
    }

    public static BigInteger energy(long heatPerUnit, long fuelUnits, int technologyPercent, int magicPercent) {
        return energy(heatPerUnit, fuelUnits, technologyPercent, 1, magicPercent, 1);
    }

    public static BigDecimal multiplier(int technologyPercent, int technologyExponent, int magicPercent,
        int magicExponent) {
        if (technologyPercent < 100 || magicPercent < 100
            || technologyExponent < 1
            || technologyExponent > 32
            || magicExponent < 1
            || magicExponent > 10) throw new IllegalArgumentException("Invalid route upgrade");
        return new BigDecimal(
            BigInteger.valueOf(technologyPercent)
                .pow(technologyExponent)
                .multiply(
                    BigInteger.valueOf(magicPercent)
                        .pow(magicExponent)),
            2 * (technologyExponent + magicExponent));
    }

    public static BigInteger energy(long heatPerUnit, long fuelUnits, int technologyPercent, int technologyExponent,
        int magicPercent, int magicExponent) {
        if (heatPerUnit <= 0 || fuelUnits <= 0 || fuelUnits % 1000 != 0)
            throw new IllegalArgumentException("Whole-litre fuel required");
        return new BigDecimal(
            BigInteger.valueOf(heatPerUnit)
                .multiply(BigInteger.valueOf(fuelUnits)))
                    .multiply(multiplier(technologyPercent, technologyExponent, magicPercent, magicExponent))
                    .toBigInteger();
    }

    /** Balanced distribution, exact batch total, adjacent ticks differ by at most 1 EU. */
    public static BigInteger tickEnergy(BigInteger total, int tick) {
        if (tick < 0 || tick >= TICKS || total.signum() < 0) throw new IllegalArgumentException("Invalid tick");
        BigInteger divisor = BigInteger.valueOf(TICKS);
        return total.multiply(BigInteger.valueOf(tick + 1))
            .divide(divisor)
            .subtract(
                total.multiply(BigInteger.valueOf(tick))
                    .divide(divisor));
    }
}
