package dev.benzenefuels.packtest;

import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import dev.benzenefuels.BenzeneFuels;
import dev.benzenefuels.FluidDescriptions;
import dev.benzenefuels.FuelContent;
import dev.benzenefuels.FuelDescription;
import dev.benzenefuels.FuelStage;
import dev.benzenefuels.GeneratorDescription;
import dev.benzenefuels.GeneratorRecipes;
import dev.benzenefuels.ResonanceGenerator;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.MultiblockTooltipBuilder;

/** Headless checks of actual registered NEI data and shared tooltip builders. */
final class DisplaySuite {

    interface Check {

        void test(String name, boolean passed, String details);
    }

    static void run(Check check) throws Exception {
        List<GTRecipe> recipes = new ArrayList<>(GeneratorRecipes.RECIPES.getAllRecipes());
        long supported = FluidRegistry.getRegisteredFluids()
            .values()
            .stream()
            .filter(f -> ResonanceGenerator.heat(new FluidStack(f, 1)) > 0)
            .count();
        check.test(
            "nei.allSupportedFuels",
            recipes.size() == supported * 25,
            recipes.size() + " recipes / " + supported + " fuels / 25 base combinations");
        check.test(
            "nei.controllerCatalyst",
            ((ResonanceGenerator) GregTechAPI.METATILEENTITIES[BenzeneFuels.generatorMetaId]).getAvailableRecipeMaps()
                .contains(GeneratorRecipes.RECIPES),
            "Native GT catalyst association");
        check.test(
            "nei.referenceOnly",
            recipes.stream()
                .allMatch(
                    r -> r.mFakeRecipe && r.mEUt == 0
                        && r.mDuration == 100
                        && r.mOutputs.length == 0
                        && r.mFluidOutputs.length == 0),
            "Reference recipes never execute or fabricate a controller output");
        check.test(
            "nei.exactFluidQuantities",
            recipes.stream()
                .allMatch(
                    r -> r.mFluidInputs.length == 3 && r.mFluidInputs[0].amount == 1000
                        && r.mFluidInputs[1].amount == 1
                        && r.mFluidInputs[2].amount == 1),
            "1000 mB fuel + 1 mB technology + 1 mB magic");
        for (FuelStage stage : FuelStage.values()) {
            List<GTRecipe> rows = recipes.stream()
                .filter(r -> r.mFluidInputs[0].isFluidEqual(FuelContent.stack(stage, 1)))
                .collect(Collectors.toList());
            check.test(stage.key + ".nei.allBaseCombinations", rows.size() == 25, "Five grades on each route");
            check.test(
                stage.key + ".nei.fullPrecisionEnergy",
                rows.stream()
                    .allMatch(r -> {
                        BigInteger expected = BigInteger.valueOf(stage.euPerLiter)
                            .multiply(BigInteger.valueOf(1000))
                            .multiply(
                                BigInteger.valueOf(
                                    GeneratorRecipes
                                        .percent(r.mFluidInputs[1], dev.benzenefuels.GeneratorRoute.TECHNOLOGY)))
                            .multiply(
                                BigInteger.valueOf(
                                    GeneratorRecipes.percent(r.mFluidInputs[2], dev.benzenefuels.GeneratorRoute.MAGIC)))
                            .divide(BigInteger.valueOf(10000));
                        return expected.equals(GeneratorRecipes.baselineEnergy(r));
                    }),
                "BigInteger, including UIV/UMV/UXV");
            List<String> lines = new ArrayList<>();
            FluidDescriptions.append(
                FuelContent.stack(stage, 1)
                    .getFluid(),
                lines);
            check.test(
                stage.key + ".fluidTooltip.shared",
                lines.equals(Arrays.asList(FuelDescription.fluidLines(stage))) && lines.size() == 4,
                lines.toString());
            FluidDescriptions.append(
                FuelContent.stack(stage, 1)
                    .getFluid(),
                lines);
            check.test(stage.key + ".fluidTooltip.notDuplicated", lines.size() == 4, "Repeated tooltip hook");
        }
        List<String> water = new ArrayList<>();
        water.add("Water");
        FluidDescriptions.append(FluidRegistry.WATER, water);
        FluidDescriptions.append(null, water);
        check.test("fluidTooltip.otherFluidsUntouched", water.equals(Arrays.asList("Water")), water.toString());
        for (String locale : new String[] { "zh_CN", "en_US" }) {
            Properties properties = new Properties();
            try (InputStreamReader reader = new InputStreamReader(
                DisplaySuite.class.getResourceAsStream("/assets/benzenefuels/lang/" + locale + ".lang"),
                StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
            FuelDescription.Translation text = (key, args) -> {
                if (!properties.containsKey(key)) throw new IllegalArgumentException("Missing " + locale + ": " + key);
                return String.format(Locale.ROOT, properties.getProperty(key), args);
            };
            MultiblockTooltipBuilder tooltip = GeneratorDescription.tooltip(text);
            check.test(
                locale + ".controller.description",
                tooltip.getInformation().length >= 12,
                Arrays.toString(tooltip.getInformation()));
            check.test(
                locale + ".controller.structure",
                tooltip.getStructureInformation().length > 4,
                Arrays.toString(tooltip.getStructureInformation()));
            for (FuelStage stage : FuelStage.values()) {
                String[] lines = FuelDescription.fluidLines(stage, text);
                check.test(
                    locale + "." + stage.key + ".fluidTooltip.noContainerCapacity",
                    lines.length == 4 && Arrays.stream(lines)
                        .noneMatch(l -> l.contains("1000") || l.contains("1000mB")),
                    Arrays.toString(lines));
            }
            check.test(
                locale + ".nei.description",
                recipes.stream()
                    .allMatch(
                        r -> GeneratorRecipes.description(r, text)
                            .size() == 6),
                "Localized energy, power, batch and exponent baseline");
        }
    }
}
