package dev.benzenefuels;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.google.gson.Gson;

/** Synchronize values, then localize on the viewing client rather than the dedicated server. */
public final class GeneratorDisplay {

    public static final class State {

        public String fuelId = "", generation = "0", delivered = "0", generated = "0", multiplier = "1";
        public long fuelUnits, coilHeat, activeParallel;
        public int parallelLimit, configuredParallel;
        public int technologyMk, magicMk, technologyExponentMk, magicExponentMk;
        public boolean wireless;
        public int dynamoCount;
        public String dynamoStored = "0", dynamoCapacity = "0", dynamoRate = "0";
    }

    private static final Gson GSON = new Gson();
    private static final String[] ROMAN = { "", "I", "II", "III", "IV", "V" };

    private GeneratorDisplay() {}

    public static String encode(State state) {
        return GSON.toJson(state);
    }

    public static String text(String encoded) {
        State state = GSON.fromJson(encoded, State.class);
        String[] lines = lines(state);
        String[] colors = { "b", "6", "a", "7", "b", "e", "6", "a", "b", "b", "d", "d", "f" };
        if (new BigInteger(state.delivered).compareTo(new BigInteger(state.generated)) < 0) colors[7] = "6";
        for (int i = 0; i < lines.length; i++) lines[i] = "\u00a7" + colors[i] + lines[i] + "\u00a7r";
        return String.join("\n", lines);
    }

    public static String details(String encoded) {
        State s = GSON.fromJson(encoded, State.class);
        return tr("parallel", String.format(java.util.Locale.ROOT, "%,d", s.activeParallel)) + "\n"
            + tr("coil", s.coilHeat, String.format(java.util.Locale.ROOT, "%,d", s.parallelLimit))
            + "\n"
            + tr(
                "parallel_limits",
                String.format(java.util.Locale.ROOT, "%,d", s.configuredParallel),
                String.format(java.util.Locale.ROOT, "%,d", Math.min(s.configuredParallel, s.parallelLimit)))
            + "\n"
            + tr("input", GeneratorFormat.volume(s.fuelUnits, 1000), GeneratorFormat.volume(s.fuelUnits, 5000))
            + "\n"
            + tr(
                "dynamo_buffer",
                s.dynamoCount,
                GeneratorFormat.power(new BigInteger(s.dynamoStored)),
                GeneratorFormat.power(new BigInteger(s.dynamoCapacity)))
            + "\n"
            + tr("dynamo_rate", GeneratorFormat.power(new BigInteger(s.dynamoRate)))
            + "\n"
            + tr("dynamo_help");
    }

    private static String tr(String key, Object... args) {
        return StatCollector.translateToLocalFormatted("gui.benzenefuels." + key, args);
    }

    private static String fluidName(String id) {
        Fluid fluid = FluidRegistry.getFluid(id);
        return fluid == null ? "-" : fluid.getLocalizedName(new FluidStack(fluid, 1));
    }

    private static String base(GeneratorRoute route, int mk) {
        return mk < 1 ? "-" : fluidName(route.baseFluidId(mk));
    }

    private static String exponent(GeneratorRoute route, int mk) {
        return (mk == 0 ? 1 : route.exponent(mk)) + " ("
            + (mk == 0 ? tr("not_upgraded") : tr(route.key + "_core") + " MK" + ROMAN[mk])
            + ")";
    }

    public static String[] lines(State state) {
        return new String[] { tr("fuel", fluidName(state.fuelId)),
            tr("coil", state.coilHeat, GeneratorFormat.compact(state.parallelLimit, 1)),
            tr("parallel", GeneratorFormat.compact(state.activeParallel, 1)),
            tr(
                "parallel_limits",
                GeneratorFormat.compact(state.configuredParallel, 1),
                GeneratorFormat.compact(Math.min(state.configuredParallel, state.parallelLimit), 1)),
            tr("input", GeneratorFormat.compact(state.fuelUnits, 1000), GeneratorFormat.compact(state.fuelUnits, 5000)),
            tr(
                "efficiency",
                new BigDecimal(state.multiplier).setScale(4, RoundingMode.DOWN)
                    .toPlainString()),
            tr("generated", GeneratorFormat.power(new BigInteger(state.generation))),
            tr(
                "sent",
                GeneratorFormat.power(new BigInteger(state.delivered)),
                GeneratorFormat.percentage(new BigInteger(state.delivered), new BigInteger(state.generated))),
            tr("technology_base", base(GeneratorRoute.TECHNOLOGY, state.technologyMk)),
            tr("technology_exponent", exponent(GeneratorRoute.TECHNOLOGY, state.technologyExponentMk)),
            tr("magic_base", base(GeneratorRoute.MAGIC, state.magicMk)),
            tr("magic_exponent", exponent(GeneratorRoute.MAGIC, state.magicExponentMk)),
            tr("wireless_state", tr(state.wireless ? "on" : "off")) };
    }
}
