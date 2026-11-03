package dev.benzenefuels;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import gregtech.api.enums.GTValues;
import gregtech.api.enums.ItemList;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GTRecipe;

public final class StarRecipes {

    public static final Map<VoltageStar, GTRecipe> REGISTERED = new LinkedHashMap<>();
    public static final Map<VoltageStar, GTRecipe.RecipeAssemblyLine> ASSEMBLY_LINES = new LinkedHashMap<>();
    public static final String[] COMPONENT_PREFIXES = { "Electric_Motor_", "Electric_Piston_", "Electric_Pump_",
        "Conveyor_Module_", "Robot_Arm_", "Emitter_", "Sensor_", "Field_Generator_" };

    private StarRecipes() {}

    public static void register() {
        if (!REGISTERED.isEmpty()) throw new IllegalStateException("Star recipes registered twice");
        for (VoltageStar stage : VoltageStar.values()) {
            ItemStack[] inputs = inputs(stage);
            GTRecipe recipe;
            if (stage.assemblyLine()) {
                // The native convenience builder requires a fluid. Native assembly lines themselves do not.
                // Register directly so the agreed item-only recipe gains no arbitrary solder/fluid cost.
                ItemStack research = FuelContent.star(VoltageStar.values()[stage.ordinal() - 1], 1);
                GTRecipe.RecipeAssemblyLine assembly = new GTRecipe.RecipeAssemblyLine(
                    research,
                    1200,
                    stage.voltage(),
                    inputs,
                    new FluidStack[0],
                    FuelContent.star(stage, 1),
                    stage.durationTicks(),
                    stage.voltage());
                GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes.add(assembly);
                ASSEMBLY_LINES.put(stage, assembly);
                GTValues.RA.stdBuilder()
                    .itemInputs(research)
                    .itemOutputs(FuelContent.star(stage, 1))
                    .special(assembly.newDataStickForNEI("Writes Research result", 1))
                    .duration(1200)
                    .eut(stage.voltage())
                    .specialValue(-201)
                    .ignoreCollision()
                    .fake()
                    .addTo(RecipeMaps.scannerFakeRecipes);
                recipe = RecipeMaps.assemblylineVisualRecipes.addFakeRecipe(
                    false,
                    inputs,
                    new ItemStack[] { FuelContent.star(stage, 1) },
                    new ItemStack[] { assembly.newDataStickForNEI("Reads Research result", 0) },
                    new FluidStack[0],
                    null,
                    stage.durationTicks(),
                    stage.voltage(),
                    0,
                    new ItemStack[inputs.length][],
                    null,
                    false);
            } else {
                recipe = GTValues.RA.stdBuilder()
                    .itemInputsUnsafe(inputs)
                    .itemOutputs(FuelContent.star(stage, 1))
                    .duration(stage.durationTicks())
                    .eut(stage.voltage())
                    .addTo(RecipeMaps.assemblerRecipes)
                    .iterator()
                    .next();
            }
            if (recipe == null) throw new IllegalStateException("Missing star recipe: " + stage);
            REGISTERED.put(stage, recipe);
        }
    }

    public static ItemStack[] inputs(VoltageStar stage) {
        ItemStack[] inputs = new ItemStack[stage == VoltageStar.LV ? 8 : 9];
        for (int i = 0; i < COMPONENT_PREFIXES.length; i++) {
            ItemList component = ItemList.valueOf(COMPONENT_PREFIXES[i] + stage.name());
            inputs[i] = component.get(1);
            if (inputs[i] == null || inputs[i].getItem() == null)
                throw new IllegalStateException("Missing component: " + COMPONENT_PREFIXES[i] + stage);
            // ItemList.get(amount) clamps before the recipe builder sees the ingredient.
            inputs[i].stackSize = stage.componentsEach();
        }
        if (stage != VoltageStar.LV)
            inputs[8] = FuelContent.star(VoltageStar.values()[stage.ordinal() - 1], stage.previousStars());
        return inputs;
    }
}
