package dev.benzenefuels;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.GTValues;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.recipe.RecipeMapBuilder;
import gregtech.api.util.GTRecipe;

/** NEI reference recipes only: execution remains in ResonanceGenerator.checkProcessing. */
public final class GeneratorRecipes {

    public static final RecipeMap<RecipeMapBackend> RECIPES = RecipeMapBuilder.of("benzenefuels.recipe.resonance")
        .maxIO(0, 0, 3, 0)
        .minInputs(0, 3)
        .dontUseProgressBar()
        .logoPos(151, 12)
        .disableRegisterNEI()
        .neiHandlerInfo(
            builder -> builder.setDisplayStack(BenzeneFuels.generator)
                .setHeight(180)
                .setMaxRecipesPerPage(1))
        .neiRecipeBackgroundSize(170, 174)
        .frontend(GeneratorRecipeFrontend::new)
        .build();

    private GeneratorRecipes() {}

    public static void register() {
        List<Fluid> fuels = new ArrayList<>(
            FluidRegistry.getRegisteredFluids()
                .values());
        fuels.sort(Comparator.comparingLong(fluid -> ResonanceGenerator.heat(new FluidStack(fluid, 1))));
        for (Fluid fluid : fuels) {
            if (ResonanceGenerator.heat(new FluidStack(fluid, 1)) <= 0) continue;
            for (int tech = 1; tech <= 5; tech++) for (int magic = 1; magic <= 5; magic++) {
                GTValues.RA.stdBuilder()
                    .fluidInputs(
                        new FluidStack(fluid, 1000),
                        FuelContent.baseFluid(GeneratorRoute.TECHNOLOGY, tech, 1),
                        FuelContent.baseFluid(GeneratorRoute.MAGIC, magic, 1))
                    .duration(GeneratorMath.TICKS)
                    .eut(0)
                    .fake()
                    .addTo(RECIPES);
            }
        }
    }

    public static int percent(FluidStack fluid, GeneratorRoute route) {
        for (int mk = 1; mk <= 5; mk++) if (route.baseFluidId(mk)
            .equals(
                fluid.getFluid()
                    .getName()))
            return (int) Math.round(route.base(mk) * 100);
        throw new IllegalArgumentException("Unknown route base fluid");
    }

    public static BigInteger baselineEnergy(GTRecipe recipe) {
        return GeneratorMath.energy(
            ResonanceGenerator.heat(recipe.mFluidInputs[0]),
            recipe.mFluidInputs[0].amount,
            percent(recipe.mFluidInputs[1], GeneratorRoute.TECHNOLOGY),
            percent(recipe.mFluidInputs[2], GeneratorRoute.MAGIC));
    }

    public static List<String> description(GTRecipe recipe, FuelDescription.Translation language) {
        BigInteger energy = baselineEnergy(recipe);
        List<String> lines = new ArrayList<>();
        lines.add(language.text("nei.benzenefuels.batch"));
        lines.add(language.text("nei.benzenefuels.bases"));
        lines.add(language.text("nei.benzenefuels.energy", GeneratorFormat.power(energy)));
        lines.add(
            language.text("nei.benzenefuels.power", GeneratorFormat.power(energy.divide(BigInteger.valueOf(100)))));
        lines.add(language.text("nei.benzenefuels.exponents"));
        lines.add(language.text("nei.benzenefuels.upgrades"));
        return lines;
    }
}
