package dev.benzenefuels.packtest;

import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import dev.benzenefuels.FuelContent;
import dev.benzenefuels.FuelDescription;
import dev.benzenefuels.FuelRecipes;
import dev.benzenefuels.FuelStage;
import dev.benzenefuels.FuelTextFormat;
import dev.benzenefuels.GeneratorRoute;
import dev.benzenefuels.StarRecipes;
import dev.benzenefuels.VoltageStar;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.MTEHatchBulkCatalystHousing;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.GTRecipeConstants;
import gregtech.common.tileentities.machines.multi.MTEFusionComputer1;
import gregtech.common.tileentities.machines.multi.MTEFusionComputer2;
import gregtech.common.tileentities.machines.multi.MTEFusionComputer3;
import gregtech.common.tileentities.machines.multi.MTENaquadahFuelRefinery;
import gtPlusPlus.xmod.gregtech.common.tileentities.machines.multi.production.MTEQuantumForceTransformer;

/** Explicit headless-server fixture; excluded from the installable JAR. No player worlds are modified. */
@Mod(
    modid = "benzenefuelstests",
    name = "Benzene Fuels server tests",
    version = "1",
    dependencies = "required-after:benzenefuels")
public final class RecipeSuite {

    private final List<Map<String, Object>> results = new ArrayList<>();
    private final Map<String, String> dependencies = new LinkedHashMap<>();

    @Mod.EventHandler
    public void started(FMLServerStartedEvent event) {
        if (!Boolean.getBoolean("benzenefuels.tests") || !FMLCommonHandler.instance()
            .getSide()
            .isServer()) return;
        try {
            run();
        } catch (Throwable error) {
            check("fixture.exception", false, error.toString());
            error.printStackTrace();
        } finally {
            try {
                Map<String, Object> report = new LinkedHashMap<>();
                report.put("target", "GTNH 2.9.0 / GT5 5.09.54.265");
                report.put("dependencies", dependencies);
                report.put(
                    "scope",
                    "Full-pack registration, native recipe matching/consumption and machine tier validation");
                report.put("clientLaunched", false);
                report.put(
                    "passed",
                    !results.isEmpty() && results.stream()
                        .allMatch(r -> Boolean.TRUE.equals(r.get("passed"))));
                report.put("tests", results);
                Files.write(
                    Paths.get("benzene-fuels-tests.json"),
                    new GsonBuilder().setPrettyPrinting()
                        .create()
                        .toJson(report)
                        .getBytes(StandardCharsets.UTF_8));
            } catch (Exception error) {
                error.printStackTrace();
            }
            MinecraftServer.getServer()
                .initiateShutdown();
        }
    }

    private void run() throws Exception {
        runtimeDependencies();
        fuelTooltips();
        DisplaySuite.run(this::check);
        for (FuelStage stage : FuelStage.values()) {
            FluidStack fuel = FuelContent.stack(stage, 1000);
            check(stage.key + ".fluid", FluidRegistry.getFluid(stage.fluidId()) == fuel.getFluid(), stage.fluidId());
            fuelTextures(stage, fuel);
            GTRecipe gas = RecipeMaps.gasTurbineFuels.getBackend()
                .findFuel(fuel);
            check(
                stage.key + ".fuelValue",
                stage.supportsNativeGasRecipe() ? gas != null && gas.mSpecialValue == stage.euPerLiter : gas == null,
                gas == null ? "missing" : Integer.toString(gas.mSpecialValue));
            FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(FuelContent.cell(stage, 1));
            check(
                stage.key + ".cell",
                contained != null && contained.amount == 1000 && contained.isFluidEqual(fuel),
                "1,000 L");
            GTRecipe fill = RecipeMaps.cannerRecipes.getAllRecipes()
                .stream()
                .filter(r -> r.mOutputs.length == 1 && r.mOutputs[0].isItemEqual(FuelContent.cell(stage, 1)))
                .findFirst()
                .orElse(null);
            GTRecipe drain = RecipeMaps.cannerRecipes.getAllRecipes()
                .stream()
                .filter(
                    r -> r.mInputs.length == 1 && r.mInputs[0].isItemEqual(FuelContent.cell(stage, 1))
                        && r.mFluidOutputs.length == 1)
                .findFirst()
                .orElse(null);
            check(
                stage.key + ".fillCell",
                fill != null
                    && fill.isRecipeInputEqual(false, new FluidStack[] { fuel.copy() }, ItemList.Cell_Empty.get(1)),
                "native canner fill");
            check(
                stage.key + ".drainCell",
                drain != null && drain.isRecipeInputEqual(false, new FluidStack[0], FuelContent.cell(stage, 1))
                    && drain.mFluidOutputs[0].amount == 1000,
                "native canner drain");
            GTRecipe recipe = FuelRecipes.REGISTERED.get("upgrade." + stage.key);
            check(
                stage.key + ".output",
                recipe.mFluidOutputs.length == 1 && recipe.mFluidOutputs[0].isFluidEqual(fuel)
                    && recipe.mFluidOutputs[0].amount == 50000,
                "50,000 L");
            check(stage.key + ".duration", recipe.mDuration == 200, Integer.toString(recipe.mDuration));
            check(stage.key + ".voltage", recipe.mEUt == stage.voltage(), Integer.toString(recipe.mEUt));

            FluidStack[] fluids = fluids(recipe);
            ItemStack[] items = items(recipe);
            check(stage.key + ".nativeMatch", recipe.isRecipeInputEqual(false, fluids, items), "native GT matcher");
            check(stage.key + ".nativeConsume", recipe.isRecipeInputEqual(true, fluids, items), "consume one batch");
            check(
                stage.key + ".noSecondFreeBatch",
                !recipe.isRecipeInputEqual(false, fluids, items),
                "inputs exhausted");
            fluids = fluids(recipe);
            fluids[0].amount--;
            check(
                stage.key + ".shortInputRejected",
                !recipe.isRecipeInputEqual(false, fluids, items(recipe)),
                "49,999 L");
        }
        GTRecipe zpm = FuelRecipes.REGISTERED.get("upgrade.zpm");
        check(
            "zpm.onlyTwoFluidsNoMetals",
            zpm.mInputs.length == 0 && zpm.mFluidInputs.length == 2
                && zpm.mFluidInputs[0].amount == 50000
                && zpm.mFluidInputs[1].amount == 163840,
            "50,000 L LuV + 163,840 L tin plasma; no solids");
        FluidStack[] fluids = fluids(zpm);
        fluids[1].amount = 163839;
        check(
            "zpm.shortPlasmaRejected",
            !zpm.isRecipeInputEqual(false, fluids, new ItemStack[0]),
            "163,839 L tin plasma");
        check(
            "zpm.failureDoesNotConsume",
            fluids[0].amount == 50000 && fluids[1].amount == 163839,
            "atomic failed match");
        check(
            "zpm.mk1Blocked",
            zpm.getMetadataOrDefault(GTRecipeConstants.FUSION_THRESHOLD, 0L)
                > new MTEFusionComputer1("fixture.mk1").capableStartupCanonical(),
            "280 MEU > Mk-I limit");
        check(
            "zpm.mk2Allowed",
            zpm.getMetadataOrDefault(GTRecipeConstants.FUSION_THRESHOLD, 0L)
                <= new MTEFusionComputer2("fixture.mk2").capableStartupCanonical(),
            "280 MEU <= Mk-II limit");
        GTRecipe uv = FuelRecipes.REGISTERED.get("upgrade.uv");
        long ignition = uv.getMetadataOrDefault(GTRecipeConstants.FUSION_THRESHOLD, 0L);
        check("uv.mk2Blocked", ignition > new MTEFusionComputer2("fixture.mk2b").capableStartupCanonical(), "500 MEU");
        check("uv.mk3Allowed", ignition <= new MTEFusionComputer3("fixture.mk3").capableStartupCanonical(), "500 MEU");
        check(
            "luv.realAssemblyLine",
            GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes.stream()
                .anyMatch(r -> r.mOutput.isItemEqual(FuelContent.activator(1)) && r.mInputs.length == 5),
            "survival assembly-line recipe");

        MTENaquadahFuelRefinery refinery = new MTENaquadahFuelRefinery("fixture.refinery");
        ProcessingLogic refineryLogic = logic(refinery, MTENaquadahFuelRefinery.class);
        GTRecipe uhv = FuelRecipes.REGISTERED.get("upgrade.uhv");
        set(refinery, MTENaquadahFuelRefinery.class, "tier", 0);
        check("uhv.noCoilRejected", !validate(refineryLogic, uhv), "native refinery tier check");
        set(refinery, MTENaquadahFuelRefinery.class, "tier", 1);
        check("uhv.coil1Allowed", validate(refineryLogic, uhv), "native refinery tier check");
        check("uhv.requestedName", FuelStage.UHV.chineseName.equals("UHV苯"), FuelStage.UHV.chineseName);
        quantum();
        GTRecipe activator = FuelRecipes.REGISTERED.get("luv.activator");
        check(
            "luv.activatorProcessExport",
            activator.mInputs.length == 5 && activator.mDuration == 200 && activator.mEUt == 32768,
            "assembly-line process, not scanner research");
        palette();
        scaledCosts();
        progressionItems();
        exportRecipes();
        new GeneratorSuite(this::check).run();
    }

    private void runtimeDependencies() {
        for (String[] expected : new String[][] { { "gregtech", "5.09.54.265" },
            { "appliedenergistics2", "rv3-beta-1095-GTNH" }, { "structurelib", "1.4.47" },
            { "modularui2", "2.3.94-1.7.10" }, { "gtnhlib", "0.11.54" }, { "Waila", "1.19.34" } }) {
            ModContainer mod = Loader.instance()
                .getIndexedModList()
                .get(expected[0]);
            String actual = mod == null ? "missing" : mod.getVersion();
            dependencies.put(expected[0], actual);
            check("runtime." + expected[0], expected[1].equals(actual), actual);
        }
    }

    private void fuelTextures(FuelStage stage, FluidStack fuel) throws Exception {
        check(
            stage.key + ".noDoubleTint",
            fuel.getFluid()
                .getColor() == 0xFFFFFF,
            "authored full-color palette");
        for (String property : new String[] { "stillIconResourceLocation", "flowingIconResourceLocation" }) {
            Field field = fuel.getFluid()
                .getClass()
                .getDeclaredField(property);
            field.setAccessible(true);
            check(
                stage.key + "." + property,
                stage.fluidTexture()
                    .equals(String.valueOf(field.get(fuel.getFluid()))),
                stage.fluidTexture());
        }
        for (String path : new String[] { "blocks/fluids/benzene_" + stage.key, "items/cell_" + stage.key }) {
            String resource = "/assets/benzenefuels/textures/" + path + ".png";
            try (java.io.InputStream png = RecipeSuite.class.getResourceAsStream(resource);
                java.io.InputStream metadata = RecipeSuite.class.getResourceAsStream(resource + ".mcmeta")) {
                check(stage.key + ".asset." + path, png != null && metadata != null, resource);
            }
        }
    }

    private void palette() throws Exception {
        JsonObject data;
        try (InputStreamReader reader = new InputStreamReader(
            RecipeSuite.class.getResourceAsStream("/assets/benzenefuels/generator-palette.json"),
            StandardCharsets.UTF_8)) {
            data = new Gson().fromJson(reader, JsonObject.class);
        }
        JsonObject actual = new JsonObject();
        for (Map.Entry<String, JsonElement> entry : data.getAsJsonObject("roles")
            .entrySet()) {
            JsonObject selected = entry.getValue()
                .getAsJsonObject();
            String status = selected.get("status")
                .getAsString();
            if (status.equals("reserved_controller")) continue;
            String registry = selected.get("registry_name")
                .getAsString();
            ItemStack stack;
            if (!selected.get("item_list")
                .isJsonNull()) {
                stack = ItemList.valueOf(
                    selected.get("item_list")
                        .getAsString())
                    .get(1);
            } else {
                String[] split = registry.split(":", 2);
                Block block = GameRegistry.findBlock(split[0], split[1]);
                stack = block == null ? null
                    : new ItemStack(
                        block,
                        1,
                        selected.get("metadata")
                            .getAsInt());
            }
            boolean valid = stack != null && stack.getItem() != null
                && Block.getBlockFromItem(stack.getItem()) != Blocks.air;
            check("palette." + entry.getKey() + ".registered", valid, registry);
            if (!valid) continue;
            String actualRegistry = String
                .valueOf(Block.blockRegistry.getNameForObject(Block.getBlockFromItem(stack.getItem())));
            check("palette." + entry.getKey() + ".registry", registry.equals(actualRegistry), actualRegistry);
            if (!selected.get("metadata")
                .isJsonNull())
                check(
                    "palette." + entry.getKey() + ".metadata",
                    stack.getItemDamage() == selected.get("metadata")
                        .getAsInt(),
                    Integer.toString(stack.getItemDamage()));
            JsonObject row = new JsonObject();
            row.addProperty("registry", actualRegistry);
            row.addProperty("metadata", stack.getItemDamage());
            row.addProperty("displayName", stack.getDisplayName());
            actual.add(entry.getKey(), row);
        }
        check("palette.customLightLevel", FuelContent.resonanceLightCasing.getLightValue() == 15, "15");
        Files.write(
            Paths.get("benzene-fuels-palette.json"),
            new GsonBuilder().setPrettyPrinting()
                .create()
                .toJson(actual)
                .getBytes(StandardCharsets.UTF_8));
    }

    private void scaledCosts() {
        String[] solidRecipes = { "luv.activator", "uv.activator", "upgrade.uhv", "upgrade.uev", "upgrade.uiv" };
        for (String id : solidRecipes) {
            GTRecipe r = FuelRecipes.REGISTERED.get(id);
            int expected = id.equals("upgrade.uiv") ? 2048 : 1024;
            for (ItemStack item : r.mInputs) check(
                id + ".fullSolidCount." + item.getDisplayName(),
                item.stackSize == expected,
                Integer.toString(item.stackSize));
            ItemStack[] shortItems = items(r);
            shortItems[0].stackSize--;
            check(
                id + ".shortSolidRejected",
                !r.isRecipeInputEqual(false, fluids(r), shortItems),
                "One missing item must reject a complete batch");
            // Physical inventories supply multiple ordinary stacks, not one impossible 1,024-item stack.
            List<ItemStack> split = new ArrayList<>();
            for (ItemStack item : items(r)) {
                for (int left = item.stackSize; left > 0; left -= 64) {
                    ItemStack part = item.copy();
                    part.stackSize = Math.min(64, left);
                    split.add(part);
                }
            }
            check(
                id + ".splitStacksConsume",
                r.isRecipeInputEqual(true, fluids(r), split.toArray(new ItemStack[0])),
                "Native matcher consumes across ordinary 64-item stacks");
        }
        check("costs.helium", amount("luv.activator", "plasma.helium") == 16384, "16,384 L");
        check("costs.americium", amount("uv.activator", "plasma.americium") == 16384, "16,384 L");
        check("costs.infinityUHV", amount("upgrade.uhv", "molten.infinity") == 17856, "17,856 L");
        check("costs.infinityUEV", amount("upgrade.uev", "molten.infinity") == 17856, "17,856 L");
        check("costs.spacetime", amount("upgrade.uiv", "molten.spacetime") == 17856, "17,856 L");
        check("costs.universium", amount("upgrade.uxv", "molten.universium") == 124, "124 L");
        check(
            "costs.mhdcsm",
            amount("upgrade.uxv", "molten.magnetohydrodynamicallyconstrainedstarmatter") == 124,
            "124 L");
        check("costs.rawStarMatter", amount("upgrade.uxv", "rawstarmatter") == 1984, "1,984 L");
        check("costs.eternityNotScaled", amount("upgrade.uxv", "molten.eternity") == 144, "144 L");
        check("costs.reusableCatalystNotScaled", amount("quantum.catalyst", "molten.infinity") == 144, "144 L");
    }

    private static int amount(String id, String fluidName) {
        for (FluidStack fluid : FuelRecipes.REGISTERED.get(id).mFluidInputs) if (fluid.getFluid()
            .getName()
            .equals(fluidName)) return fluid.amount;
        return -1;
    }

    private void progressionItems() {
        for (VoltageStar stage : VoltageStar.values()) {
            GTRecipe r = StarRecipes.REGISTERED.get(stage);
            check("star." + stage + ".registered", r != null, stage.name());
            if (r == null) continue;
            check("star." + stage + ".count", r.mInputs.length == (stage == VoltageStar.LV ? 8 : 9), "eight families");
            for (int i = 0; i < 8; i++) check(
                "star." + stage + ".component." + i,
                r.mInputs[i].stackSize == stage.componentsEach(),
                Integer.toString(r.mInputs[i].stackSize));
            if (stage != VoltageStar.LV) check(
                "star." + stage + ".previous",
                r.mInputs[8].stackSize == stage.previousStars()
                    && r.mInputs[8].isItemEqual(FuelContent.star(VoltageStar.values()[stage.ordinal() - 1], 1)),
                Integer.toString(r.mInputs[8].stackSize));
            check(
                "star." + stage + ".process",
                r.mDuration == stage.durationTicks() && r.mEUt == stage.voltage(),
                "duration and voltage");
            check(
                "star." + stage + ".consume",
                r.isRecipeInputEqual(true, new FluidStack[0], items(r)),
                "native consume");
            ItemStack[] shortItems = items(r);
            shortItems[0].stackSize--;
            check(
                "star." + stage + ".shortRejected",
                !r.isRecipeInputEqual(false, new FluidStack[0], shortItems),
                "short component input");
            if (stage.assemblyLine()) {
                GTRecipe.RecipeAssemblyLine nativeRecipe = StarRecipes.ASSEMBLY_LINES.get(stage);
                check(
                    "star." + stage + ".nativeAssembly",
                    nativeRecipe != null && GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes.contains(nativeRecipe)
                        && nativeRecipe.mFluidInputs.length == 0
                        && nativeRecipe.getPersistentHash() != 0,
                    "native registration with no invented fluid cost");
                for (int i = 0; i < 8; i++) check(
                    "star." + stage + ".assemblyAmount." + i,
                    GTRecipe.RecipeAssemblyLine
                        .getMatchedIngredientAmount(nativeRecipe.mInputs[i], nativeRecipe.mInputs[i], null)
                        == stage.componentsEach(),
                    "native AL ingredient quantity");
            } else {
                check(
                    "star." + stage + ".assemblerSlots",
                    r.mInputs.length <= 9 && RecipeMaps.assemblerRecipes.getAllRecipes()
                        .contains(r),
                    "native nine-slot assembler");
            }
        }
        for (GeneratorRoute route : GeneratorRoute.values()) {
            for (int mk = 1; mk <= 5; mk++) {
                FluidStack fluid = FuelContent.baseFluid(route, mk, 1000);
                ItemStack cell = FuelContent.baseCell(route, mk, 1);
                FluidStack contained = FluidContainerRegistry.getFluidForFilledItem(cell);
                check(
                    route.key + mk + ".baseCell",
                    contained != null && contained.amount == 1000 && contained.isFluidEqual(fluid),
                    "1,000 L base fluid");
                check(
                    route.key + mk + ".exponentItem",
                    FuelContent.exponentCore(route, mk, 1)
                        .getItem() != null,
                    "exponent=" + route.exponent(mk));
                boolean fill = RecipeMaps.cannerRecipes.getAllRecipes()
                    .stream()
                    .anyMatch(
                        r -> r.mOutputs.length == 1 && r.mOutputs[0].isItemEqual(cell)
                            && r.isRecipeInputEqual(
                                false,
                                new FluidStack[] { fluid.copy() },
                                ItemList.Cell_Empty.get(1)));
                boolean drain = RecipeMaps.cannerRecipes.getAllRecipes()
                    .stream()
                    .anyMatch(
                        r -> r.mInputs.length == 1 && r.mInputs[0].isItemEqual(cell)
                            && r.mFluidOutputs.length == 1
                            && r.mFluidOutputs[0].isFluidEqual(fluid));
                check(route.key + mk + ".fillAndDrain", fill && drain, "native cell recipes");
            }
        }
    }

    private void fuelTooltips() throws Exception {
        Properties chinese = new Properties(), english = new Properties();
        try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("/assets/benzenefuels/lang/zh_CN.lang"),
            StandardCharsets.UTF_8)) {
            chinese.load(reader);
        }
        try (InputStreamReader reader = new InputStreamReader(
            getClass().getResourceAsStream("/assets/benzenefuels/lang/en_US.lang"),
            StandardCharsets.UTF_8)) {
            english.load(reader);
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        for (FuelStage stage : FuelStage.values()) {
            String[] zh = FuelDescription
                .lines(stage, (key, args) -> String.format(Locale.ROOT, chinese.getProperty(key, key), args));
            String[] en = FuelDescription
                .lines(stage, (key, args) -> String.format(Locale.ROOT, english.getProperty(key, key), args));
            List<String> actual = new ArrayList<>();
            // Dedicated servers strip Item.addInformation. Invoke the addon's retained override directly.
            for (Method method : FuelContent.components.getClass()
                .getDeclaredMethods()) {
                Class<?>[] parameters = method.getParameterTypes();
                if (parameters.length == 4 && parameters[0] == ItemStack.class
                    && parameters[2] == List.class
                    && parameters[3] == boolean.class) {
                    method.setAccessible(true);
                    method.invoke(FuelContent.components, FuelContent.cell(stage, 1), null, actual, false);
                    break;
                }
            }
            check(stage.key + ".tooltip.realItem", actual.equals(Arrays.asList(en)), actual.toString());
            check(
                stage.key + ".tooltip.localizedNames",
                stage.chineseName.equals(chinese.getProperty("fluid." + stage.fluidId()))
                    && stage.englishName.equals(english.getProperty("fluid." + stage.fluidId())),
                stage.chineseName);
            check(
                stage.key + ".tooltip.heatAndUnit",
                zh[2].contains(FuelTextFormat.heat(stage.euPerLiter)) && zh[2].contains("EU/mB"),
                zh[2]);
            check(
                stage.key + ".tooltip.noMissingTranslations",
                Arrays.stream(zh)
                    .noneMatch(s -> s.contains("tooltip.benzenefuels"))
                    && Arrays.stream(en)
                        .noneMatch(s -> s.contains("tooltip.benzenefuels")),
                "both languages");
            FuelDescription.Reference reference = FuelDescription.reference(stage);
            check(
                stage.key + ".tooltip.reference",
                reference.heat > 0
                    && (stage != FuelStage.UV && stage != FuelStage.UHV || !"nitrobenzene".equals(reference.key)),
                reference.key + "=" + reference.heat);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("stage", stage.name());
            row.put("nameZh", stage.chineseName + "单元");
            row.put("nameEn", stage.englishName + " Cell");
            row.put("heatPerMb", stage.euPerLiter);
            row.put("reference", reference.key);
            row.put("referenceHeatPerMb", reference.heat);
            row.put("zh", zh);
            row.put("en", en);
            rows.add(row);
        }
        FuelDescription.Reference uxv = FuelDescription.reference(FuelStage.UXV);
        check(
            "tooltip.antimatterCopperBaseline",
            "antimatter".equals(uxv.key) && uxv.heat == 1000000000000L,
            "balanced copper, exponent 1");
        Files.write(
            Paths.get("benzene-fuels-tooltips.json"),
            new GsonBuilder().setPrettyPrinting()
                .create()
                .toJson(rows)
                .getBytes(StandardCharsets.UTF_8));
    }

    private void exportRecipes() throws Exception {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, GTRecipe> allRecipes = new LinkedHashMap<>(FuelRecipes.REGISTERED);
        for (Map.Entry<VoltageStar, GTRecipe> star : StarRecipes.REGISTERED.entrySet()) allRecipes.put(
            "star." + star.getKey()
                .key(),
            star.getValue());
        for (Map.Entry<String, GTRecipe> entry : allRecipes.entrySet()) {
            GTRecipe recipe = entry.getValue();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", entry.getKey());
            row.put("durationTicks", recipe.mDuration);
            row.put("euPerTick", recipe.mEUt);
            row.put("specialValue", recipe.mSpecialValue);
            row.put("fluidInputs", describe(recipe.mFluidInputs));
            row.put("fluidOutputs", describe(recipe.mFluidOutputs));
            List<String> items = new ArrayList<>();
            for (ItemStack item : recipe.mInputs) items.add(item.getDisplayName() + " x " + item.stackSize);
            row.put("itemInputs", items);
            rows.add(row);
        }
        Files.write(
            Paths.get("benzene-fuels-recipes.json"),
            new GsonBuilder().setPrettyPrinting()
                .create()
                .toJson(rows)
                .getBytes(StandardCharsets.UTF_8));
    }

    private static Map<String, Integer> describe(FluidStack[] fluids) {
        Map<String, Integer> values = new LinkedHashMap<>();
        for (FluidStack fluid : fluids) values.put(
            fluid.getFluid()
                .getName(),
            fluid.amount);
        return values;
    }

    @SuppressWarnings("unchecked")
    private void quantum() throws Exception {
        MTEQuantumForceTransformer machine = new MTEQuantumForceTransformer("fixture.quantum");
        MTEHatchBulkCatalystHousing housing = new MTEHatchBulkCatalystHousing(
            "fixture.catalysts",
            new String[0],
            new ITexture[0][][],
            10,
            64);
        housing.setItemStack(FuelContent.catalyst(1));
        housing.setItemCount(4);
        Field housings = MTEQuantumForceTransformer.class.getDeclaredField("catalystHousings");
        housings.setAccessible(true);
        ((List<MTEHatchBulkCatalystHousing>) housings.get(machine)).add(housing);
        check(
            "quantum.housingAcceptsCatalyst",
            housing.isValidItem(FuelContent.catalyst(1)),
            "survival-valid catalyst");
        ProcessingLogic logic = logic(machine, MTEQuantumForceTransformer.class);
        Method chances = MTEQuantumForceTransformer.class
            .getDeclaredMethod("getOutputChances", GTRecipe.class, int.class);
        chances.setAccessible(true);
        for (FuelStage stage : new FuelStage[] { FuelStage.UEV, FuelStage.UIV, FuelStage.UMV, FuelStage.UXV }) {
            GTRecipe recipe = FuelRecipes.REGISTERED.get("upgrade." + stage.key);
            logic.setInputItems(items(recipe));
            logic.setInputFluids(fluids(recipe));
            set(machine, MTEQuantumForceTransformer.class, "mFocusingTier", 4);
            set(machine, MTEQuantumForceTransformer.class, "mCraftingTier", recipe.mSpecialValue - 1);
            check(stage.key + ".lowStructureRejected", !validate(logic, recipe), "native QFT tier gate");
            set(machine, MTEQuantumForceTransformer.class, "mCraftingTier", recipe.mSpecialValue);
            check(stage.key + ".correctStructureAllowed", validate(logic, recipe), "native QFT validation");
            int[] outputChances = (int[]) chances.invoke(machine, recipe, -1);
            check(
                stage.key + ".deterministicOutput",
                outputChances.length == 1 && outputChances[0] == 10000,
                "100% output, one product");
            check(stage.key + ".catalystNotConsumed", housing.getItemCount() == 4, "four parallels retained");
            housing.setItemCount(0);
            housing.setItemStack(null);
            check(stage.key + ".missingCatalystRejected", !validate(logic, recipe), "native QFT catalyst gate");
            housing.setItemStack(FuelContent.catalyst(1));
            housing.setItemCount(4);
        }
    }

    private static ProcessingLogic logic(Object machine, Class<?> type) throws Exception {
        Method method = type.getDeclaredMethod("createProcessingLogic");
        method.setAccessible(true);
        return (ProcessingLogic) method.invoke(machine);
    }

    private static boolean validate(ProcessingLogic logic, GTRecipe recipe) throws Exception {
        Method method = logic.getClass()
            .getDeclaredMethod("validateRecipe", GTRecipe.class);
        method.setAccessible(true);
        return ((CheckRecipeResult) method.invoke(logic, recipe)).wasSuccessful();
    }

    private static void set(Object object, Class<?> type, String name, int value) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(object, value);
    }

    private static FluidStack[] fluids(GTRecipe recipe) {
        FluidStack[] copy = new FluidStack[recipe.mFluidInputs.length];
        for (int i = 0; i < copy.length; i++) copy[i] = recipe.mFluidInputs[i].copy();
        return copy;
    }

    private static ItemStack[] items(GTRecipe recipe) {
        ItemStack[] copy = new ItemStack[recipe.mInputs.length];
        for (int i = 0; i < copy.length; i++) copy[i] = recipe.mInputs[i].copy();
        return copy;
    }

    private void check(String name, boolean passed, String detail) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("name", name);
        row.put("passed", passed);
        row.put("detail", detail);
        results.add(row);
        if (!passed) System.err.println("BENZENE TEST FAILED: " + name + " " + detail);
    }
}
