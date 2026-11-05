package dev.benzenefuels;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;

import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEHatchDynamo;
import gregtech.api.metatileentity.implementations.MTEHatchInput;
import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTRecipe;
import gregtech.api.util.MultiblockTooltipBuilder;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.client.iconContainers.blocks.GTCustomBlockIconContainer;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;
import gregtech.common.misc.WirelessNetworkManager;
import gregtech.common.tileentities.machines.MTEHatchInputME;

/** 100-tick fuel generator. Native recipe lifecycle, custom loss-tolerant power output. */
public class ResonanceGenerator extends MTEEnhancedMultiBlockBase<ResonanceGenerator>
    implements ISurvivalConstructable {

    private static final BigInteger ZERO = BigInteger.ZERO;
    private static final IIconContainer IDLE = GTCustomBlockIconContainer
        .create("benzenefuels", "controller_front_idle");
    private static final IIconContainer ACTIVE = GTCustomBlockIconContainer
        .create("benzenefuels", "controller_front_active");
    private static final IIconContainer GLOW = GTCustomBlockIconContainer
        .create("benzenefuels", "controller_front_active_glow");
    private int maximumLitres = 1;
    private HeatingCoilLevel coilLevel = HeatingCoilLevel.None;
    private boolean wireless, batchWireless;
    private UUID batchOwner;
    private boolean lightsKnown, lightsActive;
    private BigInteger batchEnergy = ZERO, remainingEnergy = ZERO, lastGenerated = ZERO, lastDelivered = ZERO;
    private BigInteger lifetimeBurned = ZERO, lifetimeDelivered = ZERO, lifetimeWasted = ZERO;
    private long lastFuelUnits;
    private int technologyMk, magicMk, technologyExponentMk, magicExponentMk;
    private BigDecimal batchMultiplier = BigDecimal.ONE;
    private String fuelId = "";
    private String fuelName = "-";

    public ResonanceGenerator(int id, String name, String regional) {
        super(id, name, regional);
    }

    public ResonanceGenerator(String name) {
        super(name);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity tile) {
        return new ResonanceGenerator(mName);
    }

    @Override
    public IStructureDefinition<ResonanceGenerator> getStructureDefinition() {
        return GeneratorStructure.definition();
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        return (d, r, f) -> d.offsetY == 0 && r == com.gtnewhorizon.structurelib.alignment.enumerable.Rotation.NORMAL
            && f == com.gtnewhorizon.structurelib.alignment.enumerable.Flip.NONE;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity tile, ItemStack stack) {
        coilLevel = HeatingCoilLevel.None;
        boolean structure = checkPiece("tower", 16, 126, 0);
        return structure && coilLevel != HeatingCoilLevel.None
            && !mInputHatches.isEmpty()
            && mInputHatches.size() <= 3
            && mDynamoHatches.size() <= 16
            && mMaintenanceHatches.size() == 1;
    }

    public boolean addFrontHatch(IGregTechTileEntity tile, int texture) {
        if (tile == null) return false;
        IMetaTileEntity hatch = tile.getMetaTileEntity();
        if (hatch instanceof MTEHatchInput) return addInputHatchToMachineList(tile, texture);
        if (hatch instanceof MTEHatchMaintenance) return addMaintenanceToMachineList(tile, texture);
        return false;
    }

    public boolean addRearDynamo(IGregTechTileEntity tile, int texture) {
        if (tile == null || !(tile.getMetaTileEntity() instanceof MTEHatchDynamo)) return false;
        MTEHatchDynamo hatch = (MTEHatchDynamo) tile.getMetaTileEntity();
        hatch.updateTexture(texture);
        hatch.updateCraftingIcon(getMachineCraftingIcon(), getBaseMetaTileEntity());
        return mDynamoHatches.add(hatch);
    }

    @Override
    public void construct(ItemStack trigger, boolean hints) {
        buildPiece("tower", trigger, hints, 16, 126, 0);
    }

    @Override
    public int survivalConstruct(ItemStack trigger, int budget, ISurvivalBuildEnvironment environment) {
        if (mMachine) return -1;
        return survivialBuildPiece("tower", trigger, 16, 126, 0, budget, environment, false, true);
    }

    @Override
    protected MultiblockTooltipBuilder createTooltip() {
        return GeneratorDescription
            .tooltip((key, args) -> net.minecraft.util.StatCollector.translateToLocalFormatted(key, args));
    }

    @Override
    public gregtech.api.recipe.RecipeMap<?> getRecipeMap() {
        return GeneratorRecipes.RECIPES;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity tile, ForgeDirection side, ForgeDirection facing, int color,
        boolean active, boolean redstone) {
        ITexture casing = Textures.BlockIcons.getCasingTextureForId(176);
        if (side != facing) return new ITexture[] { casing };
        if (active) return new ITexture[] { casing, TextureFactory.builder()
            .addIcon(ACTIVE)
            .extFacing()
            .build(),
            TextureFactory.builder()
                .addIcon(GLOW)
                .extFacing()
                .glow()
                .build() };
        return new ITexture[] { casing, TextureFactory.builder()
            .addIcon(IDLE)
            .extFacing()
            .build() };
    }

    /** Ordered hatch/slot view; ME duplicates retain only their first native stack reference. */
    public List<FluidStack> orderedFluids() {
        List<FluidStack> result = new ArrayList<>();
        Map<Fluid, FluidStack> me = new LinkedHashMap<>();
        Set<FluidStack> seen = Collections.newSetFromMap(new IdentityHashMap<FluidStack, Boolean>());
        for (MTEHatchInput hatch : mInputHatches) {
            if (!hatch.isValid()) continue;
            if (hatch instanceof MTEHatchInputME) {
                for (FluidStack f : ((MTEHatchInputME) hatch).getStoredFluids()) {
                    if (f != null && f.amount > 0 && !me.containsKey(f.getFluid())) {
                        me.put(f.getFluid(), f);
                        result.add(f);
                    }
                }
            } else if (hatch instanceof MTEHatchMultiInput) {
                for (FluidStack f : ((MTEHatchMultiInput) hatch).getStoredFluid())
                    if (f != null && f.amount > 0 && seen.add(f)) result.add(f);
            } else {
                FluidStack f = hatch.getFillableStack();
                if (f != null && f.amount > 0 && seen.add(f)) result.add(f);
            }
        }
        return result;
    }

    private static int routeMk(FluidStack fluid, GeneratorRoute route) {
        String name = fluid.getFluid()
            .getName();
        for (int mk = 1; mk <= 5; mk++) if (name.equals(route.baseFluidId(mk))) return mk;
        return 0;
    }

    public static long heat(FluidStack fluid) {
        for (FuelStage stage : FuelStage.values()) if (stage.fluidId()
            .equals(
                fluid.getFluid()
                    .getName()))
            return stage.euPerLiter;
        GTRecipe nativeFuel = RecipeMaps.gasTurbineFuels.getBackend()
            .findFuel(fluid);
        return nativeFuel == null ? 0 : Math.max(0, nativeFuel.mSpecialValue);
    }

    private static long amount(List<FluidStack> fluids, FluidStack kind) {
        long sum = 0;
        for (FluidStack f : fluids) if (f.isFluidEqual(kind)) sum += Math.max(0, f.amount);
        return sum;
    }

    private static void consume(List<FluidStack> fluids, FluidStack kind, long amount) {
        for (FluidStack f : fluids) if (f.isFluidEqual(kind)) {
            int drain = (int) Math.min(amount, Math.max(0, f.amount));
            f.amount -= drain;
            amount -= drain;
            if (amount == 0) return;
        }
        throw new IllegalStateException("Fluid plan changed during recipe check");
    }

    @Override
    public CheckRecipeResult checkProcessing() {
        if (mMaxProgresstime > 0 && remainingEnergy.signum() > 0) return fail("busy");
        absorbExponentCore();
        List<FluidStack> fluids = orderedFluids();
        FluidStack fuel = null, tech = null, magic = null;
        for (FluidStack f : fluids) {
            if (routeMk(f, GeneratorRoute.TECHNOLOGY) > 0) {
                if (tech == null) tech = f;
            } else if (routeMk(f, GeneratorRoute.MAGIC) > 0) {
                if (magic == null) magic = f;
            } else if (fuel == null) fuel = f;
        }
        if (fuel == null) return fail("no_fuel");
        long heat = heat(fuel);
        if (heat <= 0) return fail("first_fuel_invalid");
        long units = GeneratorMath.fuelUnits(amount(fluids, fuel), getEffectiveParallelLimit());
        if (units == 0) return fail("whole_litre_required");
        long boost = units / 1000;
        if (tech == null || magic == null || amount(fluids, tech) < boost || amount(fluids, magic) < boost)
            return fail("base_fluids_missing");
        UUID owner = getBaseMetaTileEntity().getOwnerUuid();
        if (wireless && (owner == null || owner.equals(new UUID(0, 0)))) return fail("wireless_owner_missing");
        int t = routeMk(tech, GeneratorRoute.TECHNOLOGY), m = routeMk(magic, GeneratorRoute.MAGIC);
        BigInteger energy = GeneratorMath.energy(
            heat,
            units,
            percent(GeneratorRoute.TECHNOLOGY, t),
            getExponent(GeneratorRoute.TECHNOLOGY),
            percent(GeneratorRoute.MAGIC, m),
            getExponent(GeneratorRoute.MAGIC));
        // Validate the whole plan first. Native ME transaction start/end wraps this method.
        consume(fluids, fuel, units);
        consume(fluids, tech, boost);
        consume(fluids, magic, boost);
        technologyMk = t;
        magicMk = m;
        lastFuelUnits = units;
        fuelId = fuel.getFluid()
            .getName();
        batchMultiplier = GeneratorMath.multiplier(
            percent(GeneratorRoute.TECHNOLOGY, t),
            getExponent(GeneratorRoute.TECHNOLOGY),
            percent(GeneratorRoute.MAGIC, m),
            getExponent(GeneratorRoute.MAGIC));
        // Keep the last measured tick through a seamless batch transition.
        fuelName = fuel.getLocalizedName();
        batchEnergy = energy;
        remainingEnergy = energy;
        lifetimeBurned = lifetimeBurned.add(energy);
        batchWireless = wireless;
        batchOwner = owner;
        if (batchWireless) WirelessNetworkManager.strongCheckOrAddUser(owner);
        mEUt = 0;
        mEfficiency = 10000;
        mEfficiencyIncrease = 0;
        mProgresstime = 0;
        mMaxProgresstime = 100;
        updateSlots();
        markDirty();
        return CheckRecipeResultRegistry.GENERATING;
    }

    private static int percent(GeneratorRoute route, int mk) {
        return (int) Math.round(route.base(mk) * 100);
    }

    private CheckRecipeResult fail(String reason) {
        return SimpleCheckRecipeResult.ofFailure("benzenefuels." + reason);
    }

    @Override
    public boolean onRunningTick(ItemStack stack) {
        if (mProgresstime < 0 || mProgresstime >= 100 || remainingEnergy.signum() <= 0) return false;
        BigInteger energy = GeneratorMath.tickEnergy(batchEnergy, mProgresstime)
            .min(remainingEnergy);
        BigInteger delivered;
        if (batchWireless)
            delivered = batchOwner != null && WirelessNetworkManager.addEUToGlobalEnergyMap(batchOwner, energy) ? energy
                : ZERO;
        else delivered = deliverWired(energy);
        remainingEnergy = remainingEnergy.subtract(energy);
        lastGenerated = energy;
        lastDelivered = delivered;
        lifetimeDelivered = lifetimeDelivered.add(delivered);
        lifetimeWasted = lifetimeWasted.add(energy.subtract(delivered));
        return true;
    }

    /** Clamp each hatch independently. Never invoke vanilla's overflow explosion path. */
    public BigInteger deliverWired(BigInteger budget) {
        BigInteger left = budget;
        for (MTEHatchDynamo hatch : mDynamoHatches) {
            if (!hatch.isValid()) continue;
            IGregTechTileEntity tile = hatch.getBaseMetaTileEntity();
            long room = Math.max(0, tile.getEUCapacity() - tile.getStoredEU());
            BigInteger rate = BigInteger.valueOf(Math.max(0, hatch.maxEUOutput()))
                .multiply(BigInteger.valueOf(Math.max(0, hatch.maxAmperesOut())));
            long put = left.min(rate)
                .min(BigInteger.valueOf(room))
                .longValue();
            if (put > 0 && tile.increaseStoredEnergyUnits(put, false)) left = left.subtract(BigInteger.valueOf(put));
            if (left.signum() == 0) break;
        }
        return budget.subtract(left);
    }

    @Override
    public void stopMachine(ShutDownReason reason) {
        lifetimeWasted = lifetimeWasted.add(remainingEnergy);
        remainingEnergy = ZERO;
        lastGenerated = ZERO;
        lastDelivered = ZERO;
        super.stopMachine(reason);
        syncLights(false);
    }

    @Override
    public void onPostTick(IGregTechTileEntity tile, long tick) {
        if (tile.isServerSide()) {
            absorbExponentCore();
            // These values describe this server tick; a new recipe must not erase them.
            lastGenerated = ZERO;
            lastDelivered = ZERO;
        }
        super.onPostTick(tile, tick);
        if (tile.isServerSide()) {
            if (!mMachine && mStartUpCheck < 0 && mMaxProgresstime > 0)
                stopMachine(gregtech.api.util.shutdown.ShutDownReasonRegistry.STRUCTURE_INCOMPLETE);
            syncLights(mMachine && mMaxProgresstime > 0);
        }
    }

    /** Model coordinates converted using the same StructureLib facing transform. */
    public int[] worldPosition(GeneratorStructure.Cell cell) {
        int[] p = new int[3];
        getExtendedFacing().getWorldOffset(new int[] { cell.x, 2 - cell.y, cell.z + 16 }, p);
        IGregTechTileEntity base = getBaseMetaTileEntity();
        p[0] += base.getXCoord();
        p[1] += base.getYCoord();
        p[2] += base.getZCoord();
        return p;
    }

    private void syncLights(boolean active) {
        IGregTechTileEntity tile = getBaseMetaTileEntity();
        if (tile == null || tile.getWorld() == null
            || tile.getWorld().isRemote
            || lightsKnown && lightsActive == active) return;
        World world = tile.getWorld();
        boolean allLoaded = true;
        GeneratorStructure.definition();
        for (GeneratorStructure.Cell cell : GeneratorStructure.CELLS) if (cell.light()) {
            int[] p = worldPosition(cell);
            if (!world.blockExists(p[0], p[1], p[2])) {
                allLoaded = false;
                continue;
            }
            if (world.getBlock(p[0], p[1], p[2]) == FuelContent.resonanceLightCasing
                && world.getBlockMetadata(p[0], p[1], p[2]) != (active ? 1 : 0)) {
                world.setBlockMetadataWithNotify(p[0], p[1], p[2], active ? 1 : 0, 2);
                world.updateLightByType(EnumSkyBlock.Block, p[0], p[1], p[2]);
            }
        }
        lightsKnown = allLoaded;
        lightsActive = active;
    }

    @Override
    public void onRemoval() {
        syncLights(false);
        super.onRemoval();
    }

    public void setCoilLevel(HeatingCoilLevel level) {
        coilLevel = level;
    }

    public HeatingCoilLevel getCoilLevel() {
        return coilLevel;
    }

    public int getCoilParallelLimit() {
        return GeneratorMath.coilParallelLimit(coilLevel.getHeat());
    }

    public int getEffectiveParallelLimit() {
        return Math.min(maximumLitres, getCoilParallelLimit());
    }

    public int getMaximumLitres() {
        return maximumLitres;
    }

    public void setMaximumLitres(int value) {
        maximumLitres = Math.max(1, value);
        markDirty();
    }

    public boolean getWireless() {
        return wireless;
    }

    public void setWireless(boolean value) {
        wireless = value;
        markDirty();
    }

    public int getExponentMk(GeneratorRoute route) {
        return route == GeneratorRoute.TECHNOLOGY ? technologyExponentMk : magicExponentMk;
    }

    public int getExponent(GeneratorRoute route) {
        int mk = getExponentMk(route);
        return mk == 0 ? 1 : route.exponent(mk);
    }

    /** Only the next tier is consumed, once, on the server. Invalid, duplicate and skipped tiers remain removable. */
    public boolean absorbExponentCore() {
        IGregTechTileEntity tile = getBaseMetaTileEntity();
        if (tile == null || !tile.isServerSide()) return false;
        ItemStack stack = getControllerSlot();
        if (stack == null || stack.stackSize <= 0 || stack.getItem() != FuelContent.components) return false;
        int offset = stack.getItemDamage() - FuelContent.EXPONENT_META;
        if (offset < 0 || offset >= 20 || offset % 10 >= 5) return false;
        GeneratorRoute route = GeneratorRoute.values()[offset / 10];
        int mk = offset % 10 + 1;
        if (mk != getExponentMk(route) + 1) return false;
        if (route == GeneratorRoute.TECHNOLOGY) technologyExponentMk = mk;
        else magicExponentMk = mk;
        stack.stackSize--;
        if (stack.stackSize == 0) setInventorySlotContents(getControllerSlotIndex(), null);
        markDirty();
        return true;
    }

    public double getFuelMultiplier() {
        if (mMaxProgresstime > 0) return batchMultiplier.doubleValue();
        return technologyMk == 0 || magicMk == 0 ? 1
            : GeneratorMath
                .multiplier(
                    percent(GeneratorRoute.TECHNOLOGY, technologyMk),
                    getExponent(GeneratorRoute.TECHNOLOGY),
                    percent(GeneratorRoute.MAGIC, magicMk),
                    getExponent(GeneratorRoute.MAGIC))
                .doubleValue();
    }

    public GeneratorDisplay.State getDisplayState() {
        GeneratorDisplay.State state = new GeneratorDisplay.State();
        boolean active = mMaxProgresstime > 0;
        state.fuelId = fuelId;
        state.fuelUnits = active ? lastFuelUnits : 0;
        state.coilHeat = coilLevel.getHeat();
        state.parallelLimit = getCoilParallelLimit();
        state.configuredParallel = maximumLitres;
        state.activeParallel = active ? lastFuelUnits / GeneratorMath.UNITS_PER_LITRE : 0;
        state.generation = (active ? batchEnergy.divide(BigInteger.valueOf(100)) : ZERO).toString();
        state.delivered = (active ? lastDelivered : ZERO).toString();
        state.generated = (active ? lastGenerated : ZERO).toString();
        state.multiplier = active ? batchMultiplier.toPlainString()
            : BigDecimal.valueOf(getFuelMultiplier())
                .toPlainString();
        state.technologyMk = technologyMk;
        state.magicMk = magicMk;
        state.technologyExponentMk = technologyExponentMk;
        state.magicExponentMk = magicExponentMk;
        state.wireless = active ? batchWireless : wireless;
        BigInteger stored = ZERO, capacity = ZERO, rate = ZERO;
        for (MTEHatchDynamo hatch : mDynamoHatches) {
            if (!hatch.isValid()) continue;
            state.dynamoCount++;
            IGregTechTileEntity tile = hatch.getBaseMetaTileEntity();
            stored = stored.add(BigInteger.valueOf(Math.max(0, tile.getStoredEU())));
            capacity = capacity.add(BigInteger.valueOf(Math.max(0, tile.getEUCapacity())));
            rate = rate.add(
                BigInteger.valueOf(Math.max(0, hatch.maxEUOutput()))
                    .multiply(BigInteger.valueOf(Math.max(0, hatch.maxAmperesOut()))));
        }
        state.dynamoStored = stored.toString();
        state.dynamoCapacity = capacity.toString();
        state.dynamoRate = rate.toString();
        return state;
    }

    public String getDisplaySnapshot() {
        return GeneratorDisplay.encode(getDisplayState());
    }

    public BigInteger getBatchEnergy() {
        return batchEnergy;
    }

    public BigInteger getRemainingEnergy() {
        return remainingEnergy;
    }

    public BigInteger getLifetimeBurned() {
        return lifetimeBurned;
    }

    public BigInteger getLifetimeDelivered() {
        return lifetimeDelivered;
    }

    public BigInteger getLifetimeWasted() {
        return lifetimeWasted;
    }

    @Override
    public String[] getInfoData() {
        return GeneratorDisplay.lines(getDisplayState());
    }

    @Override
    public void getWailaNBTData(net.minecraft.entity.player.EntityPlayerMP player,
        net.minecraft.tileentity.TileEntity tile, NBTTagCompound tag, World world, int x, int y, int z) {
        super.getWailaNBTData(player, tile, tag, world, x, y, z);
        tag.removeTag("energyUsage");
        tag.setString(
            "bf.generation",
            (mMaxProgresstime > 0 ? batchEnergy.divide(BigInteger.valueOf(100)) : ZERO).toString());
        tag.setString("bf.output", (mMaxProgresstime > 0 ? lastDelivered : ZERO).toString());
        tag.setDouble("bf.multiplier", getFuelMultiplier());
    }

    @Override
    public void getWailaBody(ItemStack stack, List<String> lines, mcp.mobius.waila.api.IWailaDataAccessor accessor,
        mcp.mobius.waila.api.IWailaConfigHandler config) {
        super.getWailaBody(stack, lines, accessor, config);
        NBTTagCompound tag = accessor.getNBTData();
        lines.add(
            net.minecraft.util.StatCollector.translateToLocal("gui.benzenefuels.generation") + ": "
                + GeneratorFormat.power(big(tag, "bf.generation"))
                + " EU/t");
        lines.add(
            net.minecraft.util.StatCollector.translateToLocal("gui.benzenefuels.delivered") + ": "
                + GeneratorFormat.power(big(tag, "bf.output"))
                + " EU/t");
        lines.add(
            net.minecraft.util.StatCollector.translateToLocal("gui.benzenefuels.multiplier") + ": "
                + String.format(java.util.Locale.ROOT, "%.4fx", tag.getDouble("bf.multiplier")));
    }

    @Override
    protected MTEMultiBlockBaseGui<?> getGui() {
        return new ResonanceGeneratorGui(this);
    }

    @Override
    public boolean supportsLogo() {
        return false;
    }

    @Override
    public boolean hasRunningText() {
        return false;
    }

    @Override
    public boolean supportsPowerPanel() {
        return false;
    }

    @Override
    public boolean showRecipeTextInGUI() {
        return false;
    }

    @Override
    public void setItemNBT(NBTTagCompound nbt) {
        super.setItemNBT(nbt);
        nbt.setInteger("bf.techExponentMk", technologyExponentMk);
        nbt.setInteger("bf.magicExponentMk", magicExponentMk);
        nbt.setInteger("bf.maximumLitres", maximumLitres);
        nbt.setBoolean("bf.wireless", wireless);
    }

    @Override
    public void saveNBTData(NBTTagCompound nbt) {
        super.saveNBTData(nbt);
        nbt.setInteger("bf.maximumLitres", maximumLitres);
        nbt.setBoolean("bf.wireless", wireless);
        nbt.setBoolean("bf.batchWireless", batchWireless);
        if (batchOwner != null) nbt.setString("bf.batchOwner", batchOwner.toString());
        nbt.setString("bf.batchEnergy", batchEnergy.toString());
        nbt.setString("bf.remainingEnergy", remainingEnergy.toString());
        nbt.setString("bf.burned", lifetimeBurned.toString());
        nbt.setString("bf.delivered", lifetimeDelivered.toString());
        nbt.setString("bf.wasted", lifetimeWasted.toString());
        nbt.setLong("bf.fuelUnits", lastFuelUnits);
        nbt.setInteger("bf.techMk", technologyMk);
        nbt.setInteger("bf.magicMk", magicMk);
        nbt.setString("bf.fuel", fuelName);
        nbt.setString("bf.fuelId", fuelId);
        nbt.setInteger("bf.techExponentMk", technologyExponentMk);
        nbt.setInteger("bf.magicExponentMk", magicExponentMk);
        nbt.setString("bf.batchMultiplier", batchMultiplier.toPlainString());
    }

    @Override
    public void loadNBTData(NBTTagCompound nbt) {
        super.loadNBTData(nbt);
        maximumLitres = nbt.hasKey("bf.maximumLitres") ? Math.max(1, nbt.getInteger("bf.maximumLitres")) : 1;
        wireless = nbt.getBoolean("bf.wireless");
        batchWireless = nbt.getBoolean("bf.batchWireless");
        try {
            batchOwner = UUID.fromString(nbt.getString("bf.batchOwner"));
        } catch (IllegalArgumentException e) {
            batchOwner = null;
        }
        batchEnergy = big(nbt, "bf.batchEnergy");
        remainingEnergy = big(nbt, "bf.remainingEnergy").min(batchEnergy);
        lifetimeBurned = big(nbt, "bf.burned");
        lifetimeDelivered = big(nbt, "bf.delivered");
        lifetimeWasted = big(nbt, "bf.wasted");
        lastFuelUnits = Math.max(0, nbt.getLong("bf.fuelUnits"));
        technologyMk = Math.min(5, Math.max(0, nbt.getInteger("bf.techMk")));
        magicMk = Math.min(5, Math.max(0, nbt.getInteger("bf.magicMk")));
        fuelName = nbt.getString("bf.fuel");
        fuelId = nbt.getString("bf.fuelId");
        technologyExponentMk = Math.min(5, Math.max(0, nbt.getInteger("bf.techExponentMk")));
        magicExponentMk = Math.min(5, Math.max(0, nbt.getInteger("bf.magicExponentMk")));
        try {
            batchMultiplier = nbt.hasKey("bf.batchMultiplier") ? new BigDecimal(nbt.getString("bf.batchMultiplier"))
                : BigDecimal.valueOf(
                    technologyMk == 0 || magicMk == 0 ? 1
                        : GeneratorRoute.TECHNOLOGY.base(technologyMk) * GeneratorRoute.MAGIC.base(magicMk));
        } catch (NumberFormatException invalid) {
            batchMultiplier = BigDecimal.ONE;
        }
        lastGenerated = ZERO;
        lastDelivered = ZERO;
        lightsKnown = false;
    }

    private static BigInteger big(NBTTagCompound nbt, String key) {
        try {
            return new BigInteger(nbt.getString(key)).max(ZERO);
        } catch (NumberFormatException e) {
            return ZERO;
        }
    }
}
