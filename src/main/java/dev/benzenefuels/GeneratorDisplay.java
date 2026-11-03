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
    }

    private static final Gson GSON = new Gson();
    private static final String[] ROMAN = { "", "I", "II", "III", "IV", "V" };

    private GeneratorDisplay() {}

    public static String encode(State state) {
        return GSON.toJson(state);
    }

    public static String text(String encoded) {
        return String.join("\n", lines(GSON.fromJson(encoded, State.class)));
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
        return new String[] { tr("fuel", fluidName(state.fuelId)), tr("coil", state.coilHeat, state.parallelLimit),
            tr("parallel", state.activeParallel, state.configuredParallel),
            tr("input", GeneratorFormat.volume(state.fuelUnits, 1000), GeneratorFormat.volume(state.fuelUnits, 5000)),
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
