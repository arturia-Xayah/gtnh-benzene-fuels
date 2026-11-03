package dev.benzenefuels;

import static gregtech.api.util.GTRecipeConstants.FUEL_VALUE;
import static gregtech.api.util.GTRecipeConstants.FUSION_THRESHOLD;
import static gregtech.api.util.GTRecipeConstants.NFR_COIL_TIER;
import static gregtech.api.util.GTRecipeConstants.QFT_CATALYST;
import static gregtech.api.util.GTRecipeConstants.RESEARCH_ITEM;
import static gregtech.api.util.GTRecipeConstants.SCANNING;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import goodgenerator.api.recipe.GoodGeneratorRecipeMaps;
import goodgenerator.items.GGMaterial;
import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeConstants;
import gregtech.api.util.recipe.Scanning;
import gtPlusPlus.core.fluids.GTPPFluids;
import gtPlusPlus.core.material.MaterialsElements;

/** Initial survival recipes. All additive quantities are consumed once per 50,000 L batch. */
public final class FuelRecipes {

    public static final Map<String, GTRecipe> REGISTERED = new LinkedHashMap<>();
    private static boolean registered;

    private FuelRecipes() {}

    public static void register() {
        if (registered) throw new IllegalStateException("Benzene recipes registered twice");
        registered = true;
        for (FuelStage stage : FuelStage.values()) {
            if (!stage.supportsNativeGasRecipe()) continue;
            require(
                "fuel." + stage.key,
                GTValues.RA.stdBuilder()
                    .itemInputs(FuelContent.cell(stage, 1))
                    .itemOutputs(ItemList.Cell_Empty.get(1))
                    .metadata(FUEL_VALUE, (int) stage.euPerLiter)
                    .duration(0)
                    .eut(0)
                    .addTo(RecipeMaps.gasTurbineFuels));
        }

        require(
            "luv.activator",
            GTValues.RA.stdBuilder()
                .metadata(RESEARCH_ITEM, dust("Platinum", 1))
                .metadata(SCANNING, new Scanning(1200, 8192))
                .itemInputsUnsafe(
                    dust("Platinum", 1024),
                    dust("Rhodium", 1024),
                    dust("Palladium", 1024),
                    dust("Iridium", 1024),
                    dust("Ruthenium", 1024))
                .fluidInputs(Materials.Helium.getPlasma(16384))
                .itemOutputs(FuelContent.activator(1))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.LUV.voltage())
                .addTo(GTRecipeConstants.AssemblyLine));
        require(
            "upgrade.luv",
            GTValues.RA.stdBuilder()
                .itemInputs(FuelContent.activator(1))
                .fluidInputs(new FluidStack(GTPPFluids.Nitrobenzene, FuelStage.BATCH_LITERS))
                .fluidOutputs(FuelContent.stack(FuelStage.LUV, FuelStage.BATCH_LITERS))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.LUV.voltage())
                .addTo(RecipeMaps.multiblockChemicalReactorRecipes));

        // Exactly two fluid inputs; no solid ingredients or intermediate activator in the ZPM recipe.
        require(
            "upgrade.zpm",
            GTValues.RA.stdBuilder()
                .fluidInputs(
                    FuelContent.stack(FuelStage.LUV, FuelStage.BATCH_LITERS),
                    Materials.Tin.getPlasma(FuelStage.ZPM_TIN_PLASMA_LITERS))
                .fluidOutputs(FuelContent.stack(FuelStage.ZPM, FuelStage.BATCH_LITERS))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.ZPM.voltage())
                .metadata(FUSION_THRESHOLD, 280000000L)
                .addTo(RecipeMaps.fusionRecipes));

        require(
            "uv.activator",
            GTValues.RA.stdBuilder()
                .itemInputsUnsafe(dust("Neutronium", 1024), dust("CosmicNeutronium", 1024))
                .fluidInputs(Materials.Americium.getPlasma(16384))
                .fluidOutputs(new FluidStack(FuelContent.uvActivator, 144))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.UV.voltage())
                .addTo(RecipeMaps.multiblockChemicalReactorRecipes));
        require(
            "upgrade.uv",
            GTValues.RA.stdBuilder()
                .fluidInputs(
                    FuelContent.stack(FuelStage.ZPM, FuelStage.BATCH_LITERS),
                    new FluidStack(FuelContent.uvActivator, 144))
                .fluidOutputs(FuelContent.stack(FuelStage.UV, FuelStage.BATCH_LITERS))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.UV.voltage())
                .metadata(FUSION_THRESHOLD, 500000000L)
                .addTo(RecipeMaps.fusionRecipes));
        require(
            "upgrade.uhv",
            GTValues.RA.stdBuilder()
                .itemInputsUnsafe(dust("InfinityCatalyst", 1024), dust("AtomicSeparationCatalyst", 1024))
                .fluidInputs(
                    FuelContent.stack(FuelStage.UV, FuelStage.BATCH_LITERS),
                    Materials.Infinity.getMolten(17856))
                .fluidOutputs(FuelContent.stack(FuelStage.UHV, FuelStage.BATCH_LITERS))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.UHV.voltage())
                .metadata(NFR_COIL_TIER, 1)
                .addTo(GoodGeneratorRecipeMaps.naquadahFuelRefineFactoryRecipes));

        // One reusable catalyst per parallel; the per-batch additives below remain consumable.
        require(
            "quantum.catalyst",
            GTValues.RA.stdBuilder()
                .itemInputs(
                    dust("Platinum", 8),
                    dust("Palladium", 8),
                    dust("Iridium", 8),
                    ItemList.Field_Generator_UEV.get(1))
                .fluidInputs(Materials.Infinity.getMolten(144))
                .itemOutputs(FuelContent.catalyst(1))
                .duration(FuelStage.DURATION_TICKS)
                .eut(FuelStage.UEV.voltage())
                .addTo(RecipeMaps.assemblerRecipes));
        quantum(
            FuelStage.UEV,
            2,
            new ItemStack[] { dust("TranscendentMetal", 1024),
                largeStack(MaterialsElements.STANDALONE.HYPOGEN.getDust(1), 1024) },
            new FluidStack[] { Materials.Infinity.getMolten(17856) });
        quantum(
            FuelStage.UIV,
            3,
            new ItemStack[] { dust("TranscendentMetal", 2048),
                largeStack(MaterialsElements.STANDALONE.HYPOGEN.getDust(1), 2048) },
            new FluidStack[] { Materials.SpaceTime.getMolten(17856) });
        quantum(
            FuelStage.UMV,
            4,
            new ItemStack[0],
            new FluidStack[] { GGMaterial.shirabon.getMolten(144), Materials.Eternity.getMolten(144),
                Materials.WhiteDwarfMatter.getMolten(16) });
        quantum(
            FuelStage.UXV,
            4,
            new ItemStack[0],
            new FluidStack[] { Materials.Universium.getMolten(124), Materials.MHDCSM.getMolten(124),
                Materials.RawStarMatter.getFluid(1984), Materials.Eternity.getMolten(144) });
    }

    private static void quantum(FuelStage output, int machineTier, ItemStack[] items, FluidStack[] additives) {
        FuelStage previous = FuelStage.values()[output.ordinal() - 1];
        FluidStack[] inputs = new FluidStack[additives.length + 1];
        inputs[0] = FuelContent.stack(previous, FuelStage.BATCH_LITERS);
        System.arraycopy(additives, 0, inputs, 1, additives.length);
        require(
            "upgrade." + output.key,
            GTValues.RA.stdBuilder()
                .itemInputsUnsafe(items)
                .fluidInputs(inputs)
                .fluidOutputs(FuelContent.stack(output, FuelStage.BATCH_LITERS))
                .fluidOutputChances(10000)
                .metadata(QFT_CATALYST, FuelContent.catalyst(1))
                .specialValue(machineTier)
                .duration(FuelStage.DURATION_TICKS)
                .eut(output.voltage())
                .addTo(RecipeMaps.quantumForceTransformerRecipes));
    }

    private static ItemStack largeStack(ItemStack stack, int amount) {
        ItemStack result = stack.copy();
        result.stackSize = amount;
        return result;
    }

    private static ItemStack dust(String material, int count) {
        List<ItemStack> ores = OreDictionary.getOres("dust" + material);
        if (ores.isEmpty()) throw new IllegalStateException("Missing required ingredient: dust" + material);
        ItemStack stack = ores.get(0)
            .copy();
        stack.stackSize = count;
        return stack;
    }

    private static void require(String name, Collection<GTRecipe> recipes) {
        if (recipes.isEmpty()) throw new IllegalStateException("GT rejected recipe: " + name);
        GTRecipe selected = recipes.iterator()
            .next();
        // AssemblyLine returns scanner and visual process entries; retain the process for inspection.
        for (GTRecipe recipe : recipes) {
            if (recipe.mInputs.length > selected.mInputs.length) selected = recipe;
        }
        REGISTERED.put(name, selected);
    }
}
