package dev.benzenefuels;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;

import goodgenerator.blocks.tileEntity.AntimatterGenerator;
import goodgenerator.main.GGConfigLoader;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gtPlusPlus.core.fluids.GTPPFluids;

/** Same-volume, unboosted reference energy. Antimatter uses balanced copper, exponent 1. */
public final class FuelDescription {

    public static final class Reference {

        public final String key;
        public final long heat;

        Reference(String key, long heat) {
            this.key = key;
            this.heat = heat;
        }
    }

    private FuelDescription() {}

    public static Reference reference(FuelStage stage) {
        long heat = stage.euPerLiter;
        // These stages deliberately use a nearby reference, even when it is more energy-dense.
        if (stage == FuelStage.UV || stage == FuelStage.UHV) {
            int index = stage == FuelStage.UV ? 0 : 3;
            long energy = (long) GGConfigLoader.NaquadahFuelVoltage[index] * GGConfigLoader.NaquadahFuelTime[index];
            if (energy > 0) return new Reference(stage == FuelStage.UV ? "excited_uranium" : "naquadah1", energy);
        }
        GTRecipe nitro = RecipeMaps.gasTurbineFuels.getBackend()
            .findFuel(new FluidStack(GTPPFluids.Nitrobenzene, 1));
        List<Reference> values = new ArrayList<>();
        if (nitro != null && nitro.mSpecialValue > 0) values.add(new Reference("nitrobenzene", nitro.mSpecialValue));
        for (int i = 3; i < 9; i++) {
            long energy = (long) GGConfigLoader.NaquadahFuelVoltage[i] * GGConfigLoader.NaquadahFuelTime[i];
            if (energy > 0) values.add(new Reference("naquadah" + (i - 2), energy));
        }
        values.add(new Reference("antimatter", AntimatterGenerator.ANTIMATTER_FUEL_VALUE));
        Reference selected = null;
        // Prefer a familiar fuel below this one, giving an immediately readable multiplier >= 1.
        for (Reference value : values)
            if (value.heat <= heat && (selected == null || value.heat > selected.heat)) selected = value;
        if (selected == null)
            for (Reference value : values) if (selected == null || value.heat < selected.heat) selected = value;
        return selected;
    }

    public static String[] lines(FuelStage stage) {
        return lines(stage, (key, args) -> StatCollector.translateToLocalFormatted(key, args));
    }

    public interface Translation {

        String text(String key, Object... args);
    }

    /** The preview and server checks can use the exact same text builder with a selected language. */
    public static String[] lines(FuelStage stage, Translation language) {
        List<String> lines = new ArrayList<>(java.util.Arrays.asList(fluidLines(stage, language)));
        lines.add(1, language.text("tooltip.benzenefuels.fuel_volume"));
        return lines.toArray(new String[0]);
    }

    public static String[] fluidLines(FuelStage stage) {
        return fluidLines(stage, (key, args) -> StatCollector.translateToLocalFormatted(key, args));
    }

    /** A fluid has no fixed container capacity. The GUI supplies its actual amount separately. */
    public static String[] fluidLines(FuelStage stage, Translation language) {
        Reference reference = reference(stage);
        List<String> lines = new ArrayList<>();
        lines.add(language.text("tooltip.benzenefuels.fuel_tier", stage == FuelStage.LUV ? "LuV" : stage.name()));
        lines.add(language.text("tooltip.benzenefuels.fuel_heat", FuelTextFormat.heat(stage.euPerLiter)));
        lines.add(
            language.text(
                "tooltip.benzenefuels.fuel_equivalent",
                FuelTextFormat.ratio(stage.euPerLiter, reference.heat),
                language.text("tooltip.benzenefuels.reference." + reference.key)));
        lines.add(language.text("tooltip.benzenefuels.flavor." + stage.key));
        return lines.toArray(new String[0]);
    }
}
