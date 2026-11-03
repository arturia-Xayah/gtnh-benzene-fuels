package dev.benzenefuels.packtest;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.mojang.authlib.GameProfile;

import dev.benzenefuels.BenzeneFuels;
import dev.benzenefuels.FuelContent;
import dev.benzenefuels.FuelStage;
import dev.benzenefuels.GeneratorMath;
import dev.benzenefuels.GeneratorRoute;
import dev.benzenefuels.GeneratorStructure;
import dev.benzenefuels.ResonanceGenerator;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.blocks.BlockCasings5;
import gregtech.common.misc.WirelessNetworkManager;
import io.netty.channel.embedded.EmbeddedChannel;

/** Real blocks/hatches in the disposable dedicated world; no game client. */
public final class GeneratorSuite {

    public interface Reporter {

        void check(String name, boolean passed, String detail);
    }

    private final Reporter reporter;
    private final WorldServer world = MinecraftServer.getServer()
        .worldServerForDimension(0);
    private final UUID owner = UUID.fromString("bf000000-0000-4000-8000-000000000030");
    private FakePlayer player;
    private ResonanceGenerator generator;
    private IGregTechTileEntity base;
    private final List<String> csv = new ArrayList<>();

    public GeneratorSuite(Reporter reporter) {
        this.reporter = reporter;
    }

    private void check(String name, boolean passed, String detail) {
        reporter.check("generator." + name, passed, detail);
    }

    private void require(String name, boolean passed, String detail) {
        check(name, passed, detail);
        if (!passed) throw new AssertionError(name + ": " + detail);
    }

    public void run() throws Exception {
        player = new FakePlayer(world, new GameProfile(owner, "BenzeneGeneratorTest"));
        NetworkManager network = new NetworkManager(false);
        new EmbeddedChannel(network);
        new NetHandlerPlayServer(MinecraftServer.getServer(), network, player) {

            @Override
            public void sendPacket(Packet packet) {}
        };
        player.capabilities.allowEdit = true;
        player.setPosition(256, 66, 256);
        base = place(BenzeneFuels.generator.getItemDamage(), 256, 66, 256);
        base.setFrontFacing(ForgeDirection.NORTH);
        base.setOwnerUuid(owner);
        generator = (ResonanceGenerator) base.getMetaTileEntity();
        generator.construct(new ItemStack(Items.stick), false);
        for (GeneratorStructure.Cell c : GeneratorStructure.CELLS) {
            if (c.role.endsWith("port") || c.role.equals("maintenance")) {
                int id = c.role.equals("output_port") ? ItemList.Hatch_Dynamo_LuV.get(1)
                    .getItemDamage()
                    : c.role.equals("maintenance") ? ItemList.Hatch_Maintenance.get(1)
                        .getItemDamage()
                        : ItemList.Hatch_Input_LuV.get(1)
                            .getItemDamage();
                placeAt(id, c);
            }
        }
        require(
            "full_tower_formed",
            formed(),
            "blocks=" + GeneratorStructure.CELLS
                .size() + " inputs=" + generator.mInputHatches.size() + " dynamos=" + generator.mDynamoHatches.size());
        generator.fixAllIssues();
        placementRules();
        coilRules();
        fuelRules();
        powerAndPersistence();
        continuity();
        additionalChecks();
        exponentAndDisplay();
        base.disableWorking();
        generator.stopMachine(ShutDownReasonRegistry.NONE);
        Files.write(Paths.get("benzene-generator-ticks.csv"), csv, StandardCharsets.UTF_8);
    }

    private boolean formed() {
        return generator.checkStructure(true, base);
    }

    private GeneratorStructure.Cell cell(int x, int y, int z) {
        for (GeneratorStructure.Cell c : GeneratorStructure.CELLS) if (c.x == x && c.y == y && c.z == z) return c;
        throw new IllegalArgumentException("No cell " + x + "," + y + "," + z);
    }

    private IGregTechTileEntity place(int id, int x, int y, int z) {
        world.setBlockToAir(x, y, z);
        ItemStack item = GregTechAPI.METATILEENTITIES[id].getStackForm(1);
        if (!item.tryPlaceItemIntoWorld(player, world, x, y, z, 1, .5F, .5F, .5F))
            throw new AssertionError("place " + id);
        IGregTechTileEntity tile = (IGregTechTileEntity) world.getTileEntity(x, y, z);
        tile.disableWorking();
        return tile;
    }

    private IGregTechTileEntity placeAt(int id, GeneratorStructure.Cell c) {
        int[] p = generator.worldPosition(c);
        return place(id, p[0], p[1], p[2]);
    }

    private void restore(GeneratorStructure.Cell c) {
        int[] p = generator.worldPosition(c);
        world.setBlock(p[0], p[1], p[2], c.dynamo() ? GregTechAPI.sBlockCasings2 : GregTechAPI.sBlockCasings8, 0, 2);
    }

    private void placementRules() {
        GeneratorStructure.Cell extra = cell(-7, 2, -16);
        placeAt(
            ItemList.Hatch_Input_LuV.get(1)
                .getItemDamage(),
            extra);
        check("four_inputs_rejected", !formed(), "fourth input in otherwise legal front zone");
        restore(extra);
        GeneratorStructure.Cell bottom = cell(-1, 1, -16);
        placeAt(
            ItemList.Hatch_Input_LuV.get(1)
                .getItemDamage(),
            bottom);
        check("bottom_row_input_rejected", !formed(), "Y=1");
        restore(bottom);
        GeneratorStructure.Cell rear = cell(-8, 2, 14);
        placeAt(
            ItemList.Hatch_Input_LuV.get(1)
                .getItemDamage(),
            rear);
        check("rear_input_rejected", !formed(), "input in rear dynamo zone");
        restore(rear);
        placeAt(
            ItemList.Hatch_Dynamo_LuV.get(1)
                .getItemDamage(),
            extra);
        check("front_dynamo_rejected", !formed(), "dynamo on front");
        restore(extra);
        List<GeneratorStructure.Cell> extras = new ArrayList<>();
        for (GeneratorStructure.Cell c : GeneratorStructure.CELLS) if (c.dynamo() && !c.role.equals("output_port")) {
            placeAt(
                ItemList.Hatch_Dynamo_LuV.get(1)
                    .getItemDamage(),
                c);
            extras.add(c);
            if (extras.size() == 15) check("sixteen_dynamos_allowed", formed(), "16 physical dynamos");
            if (extras.size() == 16) {
                check("seventeen_dynamos_rejected", !formed(), "17 physical dynamos");
                break;
            }
        }
        for (GeneratorStructure.Cell c : extras) restore(c);
        require("structure_restored", formed(), "3 inputs, 1 maintenance, 1 dynamo");
        GeneratorStructure.Cell light = GeneratorStructure.CELLS.stream()
            .filter(GeneratorStructure.Cell::light)
            .findFirst()
            .get();
        int[] p = generator.worldPosition(light);
        world.setBlockMetadataWithNotify(p[0], p[1], p[2], 1, 2);
        check("active_light_meta_accepted", formed(), "metadata 1 remains a valid casing");
        world.setBlockMetadataWithNotify(p[0], p[1], p[2], 0, 2);
        check(
            "idle_light_dark",
            FuelContent.resonanceLightCasing.getLightValue(world, p[0], p[1], p[2]) == 0,
            "metadata 0");
    }

    private void clear() {
        generator.stopMachine(ShutDownReasonRegistry.NONE);
        for (MTEHatchInput h : generator.mInputHatches) {
            if (h instanceof MTEHatchMultiInput) {
                for (FluidStack f : ((MTEHatchMultiInput) h).getStoredFluid()) if (f != null) f.amount = 0;
            } else h.setFillableStack(null);
            h.updateSlots();
        }
    }

    private void fill(int slot, FluidStack fluid) {
        int filled = generator.mInputHatches.get(slot)
            .fill(ForgeDirection.UNKNOWN, fluid, true);
        if (filled != fluid.amount) throw new AssertionError("hatch capacity: " + filled + " / " + fluid.amount);
    }

    private void input(int fuel, int technology, int magic, int mk) {
        fill(0, FuelContent.stack(FuelStage.LUV, fuel));
        if (technology > 0) fill(1, FuelContent.baseFluid(GeneratorRoute.TECHNOLOGY, mk, technology));
        if (magic > 0) fill(2, FuelContent.baseFluid(GeneratorRoute.MAGIC, mk, magic));
    }

    private int amount(int slot) {
        FluidStack f = generator.mInputHatches.get(slot)
            .getFillableStack();
        return f == null ? 0 : f.amount;
    }

    private boolean start() {
        return generator.checkProcessing()
            .wasSuccessful();
    }

    private void setCoils(HeatingCoilLevel level) {
        for (GeneratorStructure.Cell c : GeneratorStructure.CELLS) {
            if (!c.role.equals("heating_coil")) continue;
            int[] p = generator.worldPosition(c);
            world.setBlock(p[0], p[1], p[2], GregTechAPI.sBlockCasings5, BlockCasings5.getMetaFromCoilHeat(level), 2);
        }
    }

    private void coilRules() {
        long count = GeneratorStructure.CELLS.stream()
            .filter(c -> c.role.equals("heating_coil"))
            .count();
        require("functional_coil_jacket", count == 1648, "coils=" + count);
        check(
            "new_controller_default_one_parallel",
            generator.getMaximumLitres() == 1,
            "default configured parallel 1");
        check(
            "cupronickel_limit_one",
            generator.getCoilLevel() == HeatingCoilLevel.LV && generator.getCoilParallelLimit() == 1,
            "1801 K -> 1 parallel");
        clear();
        input(9000, 9, 9, 1);
        require("default_one_parallel_starts", start(), "abundant input, default cap 1");
        check(
            "default_one_parallel_consumption",
            amount(0) == 8000 && amount(1) == 8 && amount(2) == 8,
            "burns 1000 mB fuel plus 1 mB per route");
        int progress = generator.mProgresstime;
        BigInteger remaining = generator.getRemainingEnergy();
        check(
            "coil_valid_forced_check_keeps_batch",
            formed() && generator.mProgresstime == progress
                && generator.getRemainingEnergy()
                    .equals(remaining),
            "valid forced recheck preserves paid batch");
        clear();
        generator.setMaximumLitres(Integer.MAX_VALUE);
        input(9000, 9, 9, 1);
        require("coil_caps_large_setting", start(), "configured Integer.MAX_VALUE, coil 1");
        check(
            "coil_cap_consumption",
            amount(0) == 8000 && amount(1) == 8 && amount(2) == 8,
            "only 1 parallel consumed");
        clear();
        GeneratorStructure.Cell c = GeneratorStructure.CELLS.stream()
            .filter(v -> v.role.equals("heating_coil"))
            .findFirst()
            .get();
        int[] p = generator.worldPosition(c);
        world.setBlock(p[0], p[1], p[2], GregTechAPI.sBlockCasings5, 1, 2);
        check("mixed_coil_tiers_rejected", !formed(), "single Kanthal in Cupronickel jacket");
        world.setBlockToAir(p[0], p[1], p[2]);
        check("missing_coil_rejected", !formed(), "one of 1648 coils missing");
        for (HeatingCoilLevel level : HeatingCoilLevel.values()) {
            if (level == HeatingCoilLevel.None || level == HeatingCoilLevel.ULV) continue;
            setCoils(level);
            int expected = (int) Math.floor(Math.pow(4, (level.getHeat() - 901) / 900.0 - 1));
            require(
                "coil_tier_" + level.name(),
                formed() && generator.getCoilLevel() == level && generator.getCoilParallelLimit() == expected,
                level.getHeat() + " K -> " + expected);
        }
        setCoils(HeatingCoilLevel.MV);
        require("kanthal_upgrade_reforms", formed(), "2701 K -> 4 parallel");
        generator.setMaximumLitres(30);
        input(20000, 20, 20, 1);
        require("upgraded_coil_batch", start(), "30 configured, 4 coil cap");
        check(
            "upgraded_coil_consumption",
            amount(0) == 16000 && amount(1) == 16 && amount(2) == 16,
            "4 parallel at upgraded tier");
        clear();
        generator.setMaximumLitres(Integer.MAX_VALUE);
        NBTTagCompound nbt = new NBTTagCompound();
        generator.saveNBTData(nbt);
        generator.setMaximumLitres(1);
        generator.loadNBTData(nbt);
        check(
            "parallel_setting_saved",
            generator.getMaximumLitres() == Integer.MAX_VALUE,
            "saved requested cap; effective cap is recalculated from structure");
        setCoils(HeatingCoilLevel.MV);
        require("coil_jacket_restored", formed(), "Kanthal; enough parallel for subsequent multi-batch tests");
        generator.setMaximumLitres(1);
    }

    private void fuelRules() {
        clear();
        generator.setMaximumLitres(3);
        input(3999, 3, 3, 1);
        require("fuel_cap_and_floor", start(), "3 L from 3999 mB");
        check(
            "exact_1_per_1000_boosters",
            amount(0) == 999 && amount(1) == 0 && amount(2) == 0,
            "fuel=" + amount(0) + " tech=" + amount(1) + " magic=" + amount(2));
        check("multiplier_1_0201", Math.abs(generator.getFuelMultiplier() - 1.0201) < 1E-12, "both Mk1");
        clear();
        input(999, 4, 4, 1);
        check("sub_litre_rejected", !start() && amount(0) == 999 && amount(1) == 4, "999 mB; no partial deductions");
        clear();
        input(3000, 2, 3, 1);
        check(
            "insufficient_booster_atomic",
            !start() && amount(0) == 3000 && amount(1) == 2 && amount(2) == 3,
            "no fuel downscaling to fit booster shortage");
        clear();
        input(1000, 1, 0, 1);
        check("missing_magic_rejected", !start() && amount(0) == 1000 && amount(1) == 1, "one route missing");
        clear();
        fill(0, new FluidStack(FluidRegistry.WATER, 5000));
        fill(1, FuelContent.stack(FuelStage.LUV, 2000));
        check(
            "first_invalid_no_fallback",
            !start() && amount(0) == 5000 && amount(1) == 2000,
            "water before valid LuV fuel");
        clear();
        input(1000, 1, 1, 5);
        generator.setWireless(true);
        require("mk5_start", start(), "both Mk5");
        check("multiplier_1_43", Math.abs(generator.getFuelMultiplier() - 1.43) < 1E-12, "all exponents 1");
        clear();
        generator.setMaximumLitres(1);
        input(1000, 1, 1, 1);
        require("first_small_batch", start(), "1000 mB");
        clear();
        input(1000, 0, 0, 1);
        check("no_booster_credit", !start(), "every whole-litre batch needs new 1 mB of each base");
    }

    private void powerAndPersistence() {
        clear();
        generator.setWireless(true);
        input(1000, 1, 1, 1);
        require("wireless_start", start(), "100 tick batch");
        BigInteger expected = generator.getBatchEnergy(), before = WirelessNetworkManager.getUserEU(owner);
        for (int i = 0; i < 37; i++) {
            generator.mProgresstime = i;
            generator.onRunningTick(null);
        }
        NBTTagCompound nbt = new NBTTagCompound();
        generator.mProgresstime = 37;
        generator.saveNBTData(nbt);
        generator.loadNBTData(nbt);
        check("nbt_progress_preserved", generator.mProgresstime == 37, "resume the next ungenerated tick");
        for (int i = 37; i < 100; i++) {
            generator.mProgresstime = i;
            generator.onRunningTick(null);
        }
        check(
            "nbt_resume_no_double_energy",
            WirelessNetworkManager.getUserEU(owner)
                .subtract(before)
                .equals(expected),
            "resume tick37 without redraining fuel");
        check(
            "bigint_accounting",
            generator.getLifetimeBurned()
                .equals(
                    generator.getLifetimeDelivered()
                        .add(generator.getLifetimeWasted())
                        .add(generator.getRemainingEnergy())),
            "burned = delivered + lost + remaining");
        check(
            "integer_config_persisted",
            generator.getMaximumLitres() == 1 && generator.getWireless(),
            "GUI cap + wireless persisted");
        clear();
        generator.setWireless(false);
        input(2000, 2, 2, 1);
        generator.setMaximumLitres(2);
        IGregTechTileEntity dynamo = generator.mDynamoHatches.get(0)
            .getBaseMetaTileEntity();
        dynamo.increaseStoredEnergyUnits(dynamo.getEUCapacity() - dynamo.getStoredEU(), false);
        require("full_dynamo_start", start(), "output full at start");
        BigInteger lost = generator.getLifetimeWasted(), batch = generator.getBatchEnergy();
        for (int i = 0; i < 100; i++) {
            generator.mProgresstime = i;
            check("full_output_tick_" + i, generator.onRunningTick(null), "continues");
        }
        check(
            "full_output_entire_batch_lost",
            generator.getLifetimeWasted()
                .subtract(lost)
                .equals(batch),
            "no overfill and no overflow explosion");
        check("dynamo_not_overfilled", dynamo.getStoredEU() <= dynamo.getEUCapacity(), "capacity honored");
        clear();
        generator.setWireless(true);
        input(1000, 1, 1, 1);
        require("force_check_start", start(), "active batch");
        int progress = generator.mProgresstime;
        BigInteger remaining = generator.getRemainingEnergy();
        check(
            "valid_force_check_preserves_run",
            formed() && generator.mProgresstime == progress && remaining.equals(generator.getRemainingEnergy()),
            "valid structure recheck does not stop generation");
        clear();
    }

    private void continuity() {
        generator.setWireless(true);
        generator.setMaximumLitres(1);
        input(4000, 4, 4, 1);
        generator.fixAllIssues();
        base.enableWorking();
        csv.add("tick,progress,duration,input_fuel_mB,network_EU,remaining_EU");
        BigInteger before = WirelessNetworkManager.getUserEU(owner);
        BigInteger one = GeneratorMath.energy(FuelStage.LUV.euPerLiter, 1000, 101, 101);
        for (int tick = 0; tick < 650; tick++) {
            generator.onPostTick(base, tick);
            csv.add(
                tick + ","
                    + generator.mProgresstime
                    + ","
                    + generator.mMaxProgresstime
                    + ","
                    + amount(0)
                    + ","
                    + WirelessNetworkManager.getUserEU(owner)
                    + ","
                    + generator.getRemainingEnergy());
            if (tick == 150) {
                long lit = 0;
                for (GeneratorStructure.Cell c : GeneratorStructure.CELLS) if (c.light()) {
                    int[] p = generator.worldPosition(c);
                    if (world.getBlockMetadata(p[0], p[1], p[2]) == 1) lit++;
                }
                check("running_casings_lit", lit == 621, "active casings=" + lit);
            }
        }
        check(
            "native_continuous_four_batches",
            WirelessNetworkManager.getUserEU(owner)
                .subtract(before)
                .equals(one.multiply(BigInteger.valueOf(4))),
            "650 native controller ticks; 4 complete 100-tick batches");
        check(
            "all_three_inputs_exhausted",
            amount(0) == 0 && amount(1) == 0 && amount(2) == 0,
            "4000 mB fuel + 4 mB each base");
        check("starved_machine_idle", generator.mMaxProgresstime == 0 && !base.isActive(), "native starvation result");
        long lit = 0;
        for (GeneratorStructure.Cell c : GeneratorStructure.CELLS) if (c.light()) {
            int[] p = generator.worldPosition(c);
            if (world.getBlockMetadata(p[0], p[1], p[2]) == 1) lit++;
        }
        check("idle_casings_reset", lit == 0, "active casings=" + lit);
    }

    private void additionalChecks() throws Exception {
        clear();
        generator.setWireless(false);
        generator.setMaximumLitres(1);
        input(1000, 1, 1, 1);
        IGregTechTileEntity dynamo = generator.mDynamoHatches.get(0)
            .getBaseMetaTileEntity();
        dynamo.decreaseStoredEnergyUnits(dynamo.getStoredEU(), true);
        BigInteger before = generator.getLifetimeDelivered();
        require("wired_empty_start", start(), "empty native dynamo");
        BigInteger tick = GeneratorMath.tickEnergy(generator.getBatchEnergy(), 0);
        generator.mProgresstime = 0;
        generator.onRunningTick(null);
        BigInteger rated = BigInteger.valueOf(
            generator.mDynamoHatches.get(0)
                .maxEUOutput())
            .multiply(
                BigInteger.valueOf(
                    generator.mDynamoHatches.get(0)
                        .maxAmperesOut()));
        check(
            "wired_rate_clamped",
            generator.getLifetimeDelivered()
                .subtract(before)
                .equals(tick.min(rated)),
            "native voltage x amperes per tick");
        clear();
        generator.setWireless(true);
        input(1000, 1, 1, 5);
        generator.mInputHatches.get(0)
            .setFillableStack(null);
        fill(0, FuelContent.stack(FuelStage.UXV, 1000));
        BigInteger network = WirelessNetworkManager.getUserEU(owner);
        require("uxv_long_heat_started", start(), "heat=1296000000000 per native fluid unit");
        BigInteger batch = generator.getBatchEnergy();
        for (int i = 0; i < 100; i++) {
            generator.mProgresstime = i;
            generator.onRunningTick(null);
        }
        check(
            "uxv_full_heat_wireless",
            WirelessNetworkManager.getUserEU(owner)
                .subtract(network)
                .equals(batch),
            "no int heat truncation");
        clear();
        input(1000, 1, 1, 1);
        require("break_test_started", start(), "prepaid fuel");
        GeneratorStructure.Cell core = cell(0, 60, 0);
        int[] p = generator.worldPosition(core);
        net.minecraft.block.Block old = world.getBlock(p[0], p[1], p[2]);
        int meta = world.getBlockMetadata(p[0], p[1], p[2]);
        world.setBlockToAir(p[0], p[1], p[2]);
        check("broken_core_rejected", !formed(), "removed internal core block");
        generator.onPostTick(base, 1000);
        check(
            "broken_structure_stops",
            generator.mMaxProgresstime == 0 && generator.getRemainingEnergy()
                .signum() == 0,
            "remaining prepaid energy lost, not refunded");
        world.setBlock(p[0], p[1], p[2], old, meta, 2);
        require("core_repair_reforms", formed(), "replaced original core block");
        check(
            "final_energy_conservation",
            generator.getLifetimeBurned()
                .equals(
                    generator.getLifetimeDelivered()
                        .add(generator.getLifetimeWasted())
                        .add(generator.getRemainingEnergy())),
            "all successful fuel budgets accounted");
        com.cleanroommc.modularui.value.sync.PanelSyncManager sync = new com.cleanroommc.modularui.value.sync.PanelSyncManager(
            new com.cleanroommc.modularui.value.sync.ModularSyncManager(false),
            false);
        dev.benzenefuels.ResonanceGeneratorGui gui = new dev.benzenefuels.ResonanceGeneratorGui(generator);
        java.lang.reflect.Method register = gui.getClass()
            .getDeclaredMethod("registerSyncValues", sync.getClass());
        register.setAccessible(true);
        register.invoke(gui, sync);
        java.lang.reflect.Method widgets = gui.getClass()
            .getDeclaredMethod(
                "createTerminalTextWidget",
                sync.getClass(),
                com.cleanroommc.modularui.screen.ModularPanel.class);
        widgets.setAccessible(true);
        check("native_gui_widgets_construct", widgets.invoke(gui, sync, null) != null, "compact synchronized terminal");
        check(
            "gui_cap_sync_registered",
            sync.findSyncHandler("bf.maximum", com.cleanroommc.modularui.value.sync.IntSyncValue.class)
                .getIntValue() == generator.getMaximumLitres(),
            "server-authoritative integer cap");
    }

    private void exponentAndDisplay() throws Exception {
        clear();
        base.disableWorking();
        generator.setInventorySlotContents(1, FuelContent.exponentCore(GeneratorRoute.TECHNOLOGY, 2, 1));
        check(
            "core_skip_rejected",
            !generator.absorbExponentCore() && generator.getControllerSlot().stackSize == 1
                && generator.getExponent(GeneratorRoute.TECHNOLOGY) == 1,
            "Mk2 cannot skip Mk1");
        generator.setInventorySlotContents(1, new ItemStack(Items.diamond));
        check("foreign_core_rejected", !generator.absorbExponentCore(), "foreign items remain removable");
        generator.setInventorySlotContents(1, FuelContent.exponentCore(GeneratorRoute.TECHNOLOGY, 1, 2));
        generator.onPostTick(base, 2001);
        check(
            "core_tick_absorbs_one",
            generator.getExponent(GeneratorRoute.TECHNOLOGY) == 2 && generator.getControllerSlot().stackSize == 1,
            "server controller slot, one core consumed");
        check(
            "duplicate_core_preserved",
            !generator.absorbExponentCore() && generator.getControllerSlot().stackSize == 1,
            "remaining duplicate not consumed");
        generator.setInventorySlotContents(1, null);
        generator.setWireless(true);
        generator.setMaximumLitres(1);
        input(1000, 1, 1, 2);
        require("upgraded_batch_started", start(), "tech e2 and magic e1, both Mk2 bases");
        BigInteger frozen = generator.getBatchEnergy();
        check(
            "first_core_changes_energy",
            frozen.equals(GeneratorMath.energy(13940, 1000, 102, 2, 105, 1)),
            "exact exponent energy");
        generator.setInventorySlotContents(1, FuelContent.exponentCore(GeneratorRoute.TECHNOLOGY, 2, 1));
        check(
            "core_next_tier_absorbed",
            generator.absorbExponentCore() && generator.getControllerSlot() == null,
            "tech Mk2 consumed");
        check(
            "upgrade_does_not_reprice_active_batch",
            generator.getBatchEnergy()
                .equals(frozen) && Math.abs(generator.getFuelMultiplier() - 1.09242) < 1E-12,
            "new exponent applies to next batch");
        for (int mk = 1; mk <= 2; mk++) {
            generator.setInventorySlotContents(1, FuelContent.exponentCore(GeneratorRoute.MAGIC, mk, 1));
            require("magic_core_mk" + mk, generator.absorbExponentCore(), "independent magic progression");
        }
        NBTTagCompound saved = new NBTTagCompound();
        generator.saveNBTData(saved);
        generator.loadNBTData(saved);
        check(
            "core_levels_and_batch_survive_reload",
            generator.getExponent(GeneratorRoute.TECHNOLOGY) == 4 && generator.getExponent(GeneratorRoute.MAGIC) == 4
                && generator.getBatchEnergy()
                    .equals(frozen)
                && Math.abs(generator.getFuelMultiplier() - 1.09242) < 1E-12,
            "stored level and frozen active multiplier");
        clear();
        input(1000, 1, 1, 2);
        require("next_batch_uses_both_exponents", start(), "both e4");
        check(
            "both_routes_exact_energy",
            generator.getBatchEnergy()
                .equals(GeneratorMath.energy(13940, 1000, 102, 4, 105, 4)),
            "two upgraded routes");
        generator.mProgresstime = 0;
        generator.onRunningTick(null);
        dev.benzenefuels.GeneratorDisplay.State state = generator.getDisplayState();
        check(
            "display_delivery_is_100_percent",
            dev.benzenefuels.GeneratorFormat
                .percentage(new BigInteger(state.delivered), new BigInteger(state.generated))
                .equals("100.00"),
            "wireless delivered/generated");
        String text = dev.benzenefuels.GeneratorDisplay.text(generator.getDisplaySnapshot());
        check(
            "compact_display_no_totals",
            !text.contains("Total delivered") && !text.contains("Total lost")
                && text.contains("0.2B/s")
                && text.contains("MKII"),
            "fractional rate, core name/tier, compact stats");
        for (int mk = 3; mk <= 5; mk++) {
            for (GeneratorRoute route : GeneratorRoute.values()) {
                generator.setInventorySlotContents(1, FuelContent.exponentCore(route, mk, 1));
                require("core_upgrade_" + route + "_mk" + mk, generator.absorbExponentCore(), "strict next tier");
            }
        }
        check(
            "max_core_values",
            generator.getExponent(GeneratorRoute.TECHNOLOGY) == 32 && generator.getExponent(GeneratorRoute.MAGIC) == 10,
            "route-specific maxima");
        generator.setInventorySlotContents(1, FuelContent.exponentCore(GeneratorRoute.TECHNOLOGY, 1, 1));
        check(
            "downgrade_preserved",
            !generator.absorbExponentCore() && generator.getControllerSlot().stackSize == 1,
            "old tier does not replace or consume");
        generator.setInventorySlotContents(1, null);
        NBTTagCompound item = new NBTTagCompound();
        generator.setItemNBT(item);
        ResonanceGenerator restored = (ResonanceGenerator) place(BenzeneFuels.generator.getItemDamage(), 296, 66, 256)
            .getMetaTileEntity();
        restored.loadNBTData(item);
        check(
            "upgrades_survive_machine_item",
            restored.getExponent(GeneratorRoute.TECHNOLOGY) == 32 && restored.getExponent(GeneratorRoute.MAGIC) == 10
                && restored.getRemainingEnergy()
                    .signum() == 0,
            "pickup stores upgrades/settings but no prepaid energy");
        clear();
        NBTTagCompound legacy = new NBTTagCompound();
        generator.saveNBTData(legacy);
        legacy.removeTag("bf.techExponentMk");
        legacy.removeTag("bf.magicExponentMk");
        generator.loadNBTData(legacy);
        check(
            "old_save_defaults_exponents_one",
            generator.getExponent(GeneratorRoute.TECHNOLOGY) == 1 && generator.getExponent(GeneratorRoute.MAGIC) == 1,
            "legacy saves default to no upgrades");
        com.cleanroommc.modularui.value.sync.PanelSyncManager sync = new com.cleanroommc.modularui.value.sync.PanelSyncManager(
            new com.cleanroommc.modularui.value.sync.ModularSyncManager(false),
            false);
        dev.benzenefuels.ResonanceGeneratorGui gui = new dev.benzenefuels.ResonanceGeneratorGui(generator);
        java.lang.reflect.Method register = gui.getClass()
            .getDeclaredMethod("registerSyncValues", sync.getClass());
        register.setAccessible(true);
        register.invoke(gui, sync);
        com.cleanroommc.modularui.screen.ModularPanel parent = new com.cleanroommc.modularui.screen.ModularPanel(
            "test.main");
        for (String name : new String[] { "createLeftPanelGapRow", "createRightPanelGapRow" }) {
            java.lang.reflect.Method method = gui.getClass()
                .getDeclaredMethod(name, parent.getClass(), sync.getClass());
            method.setAccessible(true);
            check(
                "gui_" + name,
                method.invoke(gui, parent, sync) != null,
                "independent controls construct without a client");
        }
        com.cleanroommc.modularui.value.sync.PanelSyncManager childSync = new com.cleanroommc.modularui.value.sync.PanelSyncManager(
            new com.cleanroommc.modularui.value.sync.ModularSyncManager(false),
            false);
        java.lang.reflect.Method settings = gui.getClass()
            .getDeclaredMethod("createFuelSettings", childSync.getClass(), parent.getClass());
        settings.setAccessible(true);
        check(
            "gui_side_panel_constructs",
            settings.invoke(gui, childSync, parent) != null,
            "separate synchronized numeric fuel cap");
    }

}
