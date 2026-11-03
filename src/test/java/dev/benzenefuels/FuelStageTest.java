package dev.benzenefuels;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class FuelStageTest {

    @Test
    public void agreedHeatValuesAndFlowUseLongArithmetic() {
        long[] expected = { 13940L, 136200L, 1491000L, 18290000L, 251500000L, 3876000000L, 66950000000L,
            1296000000000L };
        FuelStage[] stages = FuelStage.values();
        assertEquals(expected.length, stages.length);
        for (int i = 0; i < stages.length; i++) {
            assertEquals(expected[i], stages[i].euPerLiter);
            assertEquals(expected[i] * 1000L, stages[i].theoreticalPower(1000));
            assertEquals(32768L << (2 * i), stages[i].voltage());
        }
        assertEquals(1296000000000000L, FuelStage.UXV.theoreticalPower(1000));
    }

    @Test
    public void stableIdsAndRequestedNames() {
        Set<String> ids = new HashSet<>();
        for (FuelStage stage : FuelStage.values()) assertTrue(ids.add(stage.fluidId()));
        assertEquals("超富集铂族苯", FuelStage.LUV.chineseName);
        assertEquals("中子过饱和苯", FuelStage.UV.chineseName);
        assertEquals("因果闭环苯", FuelStage.UIV.chineseName);
        assertEquals("永恒调律苯", FuelStage.UMV.chineseName);
        assertEquals("苯征宇宙燃料", FuelStage.UXV.chineseName);
        for (FuelStage stage : new FuelStage[] { FuelStage.ZPM, FuelStage.UHV, FuelStage.UEV }) {
            assertEquals(stage.name() + "苯", stage.chineseName);
        }
        assertEquals(163840, FuelStage.ZPM_TIN_PLASMA_LITERS);
        assertEquals(50000, FuelStage.BATCH_LITERS);
        assertEquals(250, FuelStage.BATCH_LITERS / FuelStage.DURATION_TICKS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeFlow() {
        FuelStage.UXV.theoreticalPower(-1);
    }

    @Test(expected = ArithmeticException.class)
    public void rejectsOverflowInsteadOfWrapping() {
        FuelStage.UXV.theoreticalPower(Long.MAX_VALUE);
    }
}
