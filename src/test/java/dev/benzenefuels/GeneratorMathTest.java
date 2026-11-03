package dev.benzenefuels;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class GeneratorMathTest {

    @Test
    public void wholeLitresAndCap() {
        assertEquals(0, GeneratorMath.fuelUnits(999, 5));
        assertEquals(1000, GeneratorMath.fuelUnits(1999, 5));
        assertEquals(5000, GeneratorMath.fuelUnits(9876, 5));
        assertEquals(2_147_483_000L, GeneratorMath.fuelUnits(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    public void coilHeatSubtracts901AndUsesIntegralParallels() {
        assertEquals(0, GeneratorMath.coilParallelLimit(0));
        assertEquals(1, GeneratorMath.coilParallelLimit(901));
        assertEquals(1, GeneratorMath.coilParallelLimit(1801));
        assertEquals(4, GeneratorMath.coilParallelLimit(2701));
        assertEquals(16, GeneratorMath.coilParallelLimit(3601));
        assertEquals(
            (int) StrictMath.floor(StrictMath.pow(4, (13501 - 901) / 900.0 - 1)),
            GeneratorMath.coilParallelLimit(13501));
        assertEquals(67108864, GeneratorMath.coilParallelLimit(13501));
        assertEquals(Integer.MAX_VALUE, GeneratorMath.coilParallelLimit(Long.MAX_VALUE));
        assertEquals(1000, GeneratorMath.fuelUnits(100000, GeneratorMath.coilParallelLimit(1801)));
        assertEquals(1000, GeneratorMath.fuelUnits(100000, Math.min(1, GeneratorMath.coilParallelLimit(1801))));
    }

    @Test
    public void exactMultiplierAndLargeNumbers() {
        assertEquals(new BigInteger("14220194"), GeneratorMath.energy(13940, 1000, 101, 101));
        BigInteger energy = GeneratorMath.energy(1296000000000L, 2147483000L, 110, 130);
        assertTrue(energy.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
        assertEquals(new BigInteger("3979887294240000000000"), energy);
    }

    @Test
    public void evenlyDistributesAllEnergy() {
        for (String value : new String[] { "1", "99", "100", "101", "99999999999999999999999999" }) {
            BigInteger e = new BigInteger(value), sum = BigInteger.ZERO, min = null, max = BigInteger.ZERO;
            for (int t = 0; t < 100; t++) {
                BigInteger v = GeneratorMath.tickEnergy(e, t);
                sum = sum.add(v);
                min = min == null ? v : min.min(v);
                max = max.max(v);
            }
            assertEquals(e, sum);
            assertTrue(
                max.subtract(min)
                    .compareTo(BigInteger.ONE) <= 0);
        }
    }
}
