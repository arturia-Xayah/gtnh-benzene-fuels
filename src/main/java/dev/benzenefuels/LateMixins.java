package dev.benzenefuels;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.relauncher.FMLLaunchHandler;

@LateMixin
public final class LateMixins implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.benzenefuels.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> mods) {
        return FMLLaunchHandler.side()
            .isClient() ? Collections.singletonList("FluidTooltipMixin") : Collections.emptyList();
    }
}
