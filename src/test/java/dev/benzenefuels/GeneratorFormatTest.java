package dev.benzenefuels;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class GeneratorFormatTest {

    @Test
    public void compactPowerTruncatesAndSwitchesOnlyBeyond1000G() {
        assertEquals("399.21M", GeneratorFormat.power(new BigInteger("399215250")));
        assertEquals("999.99G", GeneratorFormat.power(new BigInteger("999999999999")));
        assertEquals("1000.00G", GeneratorFormat.power(new BigInteger("1000000000000")));
        assertEquals("1.00E12", GeneratorFormat.power(new BigInteger("1000000000001")));
        assertEquals("9.99E30", GeneratorFormat.power(new BigInteger("9999999999999999999999999999999")));
        assertEquals("0.00", GeneratorFormat.power(BigInteger.ZERO));
    }

    @Test
    public void fractionalInputRateAndSafeDeliveryPercentage() {
        assertEquals("25", GeneratorFormat.volume(25000, 1000));
        assertEquals("5", GeneratorFormat.volume(25000, 5000));
        assertEquals("0.2", GeneratorFormat.volume(1000, 5000));
        assertEquals("1.234", GeneratorFormat.volume(1234, 1000));
        assertEquals("100.00", GeneratorFormat.percentage(BigInteger.TEN, BigInteger.TEN));
        assertEquals("33.33", GeneratorFormat.percentage(BigInteger.ONE, BigInteger.valueOf(3)));
        assertEquals("0.00", GeneratorFormat.percentage(BigInteger.ZERO, BigInteger.ZERO));
    }

    @Test
    public void upgradedEnergyUsesExactPowersAndRoundsOnlyOnce() {
        assertEquals(new BigInteger("49042831400"), GeneratorMath.energy(1491000, 25000, 102, 4, 105, 4));
        BigInteger max = GeneratorMath.energy(1296000000000L, 2147483000L, 110, 32, 130, 10);
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < 100; i++) sum = sum.add(GeneratorMath.tickEnergy(max, i));
        assertEquals(max, sum);
        assertTrue(max.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0);
    }
}
