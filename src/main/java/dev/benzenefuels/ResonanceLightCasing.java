package dev.benzenefuels;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/** The only custom structural casing; all other structural roles reuse existing blocks. */
public final class ResonanceLightCasing extends Block {

    private IIcon idle, active;

    public ResonanceLightCasing() {
        super(Material.iron);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 2);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister register) {
        idle = register.registerIcon("benzenefuels:resonance_light_casing_idle");
        active = register.registerIcon("benzenefuels:resonance_light_casing_active");
        blockIcon = idle;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int metadata) {
        return (metadata & 1) == 1 ? active : idle;
    }

    @Override
    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return (world.getBlockMetadata(x, y, z) & 1) == 1 ? 15 : 0;
    }

    @Override
    public int damageDropped(int metadata) {
        return 0;
    }
}
