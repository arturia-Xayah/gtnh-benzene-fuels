package dev.benzenefuels;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;

import com.gtnewhorizons.modularui.api.math.Pos2d;

import gregtech.api.recipe.BasicUIPropertiesBuilder;
import gregtech.api.recipe.NEIRecipePropertiesBuilder;
import gregtech.api.recipe.RecipeMapFrontend;
import gregtech.nei.RecipeDisplayInfo;

/** A generator produces energy, so the normal recipe's input EU/t and voltage must not be drawn. */
public final class GeneratorRecipeFrontend extends RecipeMapFrontend {

    public GeneratorRecipeFrontend(BasicUIPropertiesBuilder ui, NEIRecipePropertiesBuilder nei) {
        super(ui, nei);
    }

    @Override
    public List<Pos2d> getFluidInputPositions(int count) {
        return Arrays.asList(new Pos2d(43, 12), new Pos2d(79, 12), new Pos2d(115, 12));
    }

    @Override
    public void drawDescription(RecipeDisplayInfo info) {
        for (String line : GeneratorRecipes.description(info.recipe, StatCollector::translateToLocalFormatted)) {
            for (String wrapped : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(line, 160)) {
                info.drawText(wrapped);
            }
        }
    }
}
