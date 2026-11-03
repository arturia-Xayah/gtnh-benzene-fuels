package dev.benzenefuels;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FuelTextFormatTest {

    @Test
    public void fourSignificantDigitsPreserveAllAgreedFuelValues() {
        String[] expected = { "13.94K", "136.2K", "1.491M", "18.29M", "251.5M", "3.876G", "66.95G", "1.296T" };
        FuelStage[] stages = FuelStage.values();
        for (int i = 0; i < stages.length; i++) assertEquals(expected[i], FuelTextFormat.heat(stages[i].euPerLiter));
        assertEquals("0", FuelTextFormat.heat(0));
        assertEquals("999", FuelTextFormat.heat(999));
        assertEquals("1K", FuelTextFormat.heat(1000));
        assertEquals("9.223E", FuelTextFormat.heat(Long.MAX_VALUE));
    }

    @Test
    public void sameVolumeReferencesDoNotRescaleByOneThousand() {
        assertEquals("8.712", FuelTextFormat.ratio(13940, 1600));
        assertEquals("11.43K", FuelTextFormat.ratio(18290000, 1600));
        assertEquals("1.296", FuelTextFormat.ratio(1296000000000L, 1000000000000L));
        assertEquals("0.001296", FuelTextFormat.ratio(1296000000L, 1000000000000L));
    }
}
