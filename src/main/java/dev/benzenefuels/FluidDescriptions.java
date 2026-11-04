package dev.benzenefuels;

import java.util.List;

import net.minecraftforge.fluids.Fluid;

/** Shared by GT's fluid display item and ModularUI fluid tanks; translated at hover time. */
public final class FluidDescriptions {

    private FluidDescriptions() {}

    public static void append(Fluid fluid, List<String> lines) {
        if (fluid == null) return;
        for (FuelStage stage : FuelStage.values()) {
            if (!stage.fluidId()
                .equals(fluid.getName())) continue;
            for (String line : FuelDescription.fluidLines(stage)) {
                if (!lines.contains(line)) lines.add(line);
            }
            return;
        }
    }
}
