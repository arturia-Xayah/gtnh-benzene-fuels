package dev.benzenefuels.mixin;

import java.util.List;

import net.minecraftforge.fluids.Fluid;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.benzenefuels.FluidDescriptions;
import gregtech.common.items.ItemFluidDisplay;

@Mixin(value = ItemFluidDisplay.class, remap = false)
public abstract class FluidTooltipMixin {

    @Inject(method = "addTooltipForFluid", at = @At("RETURN"), require = 1)
    private static void benzeneFuels$describe(Fluid fluid, List<String> lines, CallbackInfo ci) {
        FluidDescriptions.append(fluid, lines);
    }
}
