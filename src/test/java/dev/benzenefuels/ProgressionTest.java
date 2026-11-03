package dev.benzenefuels;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProgressionTest {

    @Test
    public void uxvBillOfMaterialsMatchesApprovedRecurrenceWithoutIntegerOverflow() {
        long[] expected = { 17179869184L, 2147483648L, 268435456L, 33554432L, 4194304L, 524288L, 262144L, 131072L,
            65536L, 32768L, 16384L, 4096L, 1024L };
        assertArrayEquals(expected, VoltageStar.UXV.expandedComponents());
        long total = 0;
        for (long amount : expected) total += amount * 8;
        assertEquals(157076594688L, total);
        assertArrayEquals(new long[] { 16, 2 }, VoltageStar.MV.expandedComponents());
    }

    @Test
    public void routeProgressionMatchesBothApprovedDirections() {
        assertEquals(32, GeneratorRoute.TECHNOLOGY.exponent(5));
        assertEquals(10, GeneratorRoute.MAGIC.exponent(5));
        assertEquals(1.1, GeneratorRoute.TECHNOLOGY.base(5), 0);
        assertEquals(1.3, GeneratorRoute.MAGIC.base(5), 0);
        assertTrue(
            Math.pow(GeneratorRoute.TECHNOLOGY.base(5), GeneratorRoute.TECHNOLOGY.exponent(5))
                > Math.pow(GeneratorRoute.MAGIC.base(5), GeneratorRoute.MAGIC.exponent(5)));
    }

    @Test
    public void longHeatValuesNeverLeakIntoNativeIntegerFuelEntries() {
        assertTrue(FuelStage.UEV.supportsNativeGasRecipe());
        assertFalse(FuelStage.UIV.supportsNativeGasRecipe());
        assertFalse(FuelStage.UXV.supportsNativeGasRecipe());
        assertEquals(1296000000000000L, FuelStage.UXV.theoreticalPower(1000));
        assertEquals(655360, VoltageStar.UXV.durationTicks());
        assertEquals(536870912, VoltageStar.UXV.voltage());
    }
}
