package dev.benzenefuels;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkCheckHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.GregTechAPI;

@Mod(
    modid = BenzeneFuels.ID,
    name = "Benzene Fuels",
    version = Tags.VERSION,
    dependencies = "required-after:gregtech;required-after:GoodGenerator;required-after:miscutils",
    acceptedMinecraftVersions = "[1.7.10]")
public final class BenzeneFuels {

    public static final String ID = "benzenefuels";
    public static Logger LOG;
    public static int generatorMetaId;
    public static ItemStack generator;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOG = event.getModLog();
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        generatorMetaId = config.getInt(
            "generatorMetaId",
            "machines",
            32240,
            1,
            32766,
            "Stable GT meta-tile ID; change only before placing machines.");
        config.save();
        FuelContent.register();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        FuelContent.registerContainers();
        if (GregTechAPI.METATILEENTITIES[generatorMetaId] != null) throw new IllegalStateException(
            "Generator ID already occupied: " + generatorMetaId
                + "; change machines.generatorMetaId in config/benzenefuels.cfg");
        generator = new ResonanceGenerator(
            generatorMetaId,
            "benzenefuels.resonance_generator",
            "Resonance Fuel Generator").getStackForm(1);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        FuelRecipes.register();
        StarRecipes.register();
        LOG.info("Registered eight benzene fuels, ten base fluids, ten exponent cores and thirteen voltage stars");
    }

    @NetworkCheckHandler
    public boolean compatible(Map<String, String> remoteVersions, Side remoteSide) {
        return Tags.VERSION.equals(remoteVersions.get(ID));
    }
}
