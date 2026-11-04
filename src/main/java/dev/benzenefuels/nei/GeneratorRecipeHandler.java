package dev.benzenefuels.nei;

import net.minecraft.item.ItemStack;

import codechicken.nei.recipe.TemplateRecipeHandler;
import dev.benzenefuels.BenzeneFuels;
import dev.benzenefuels.GeneratorRecipes;
import gregtech.nei.GTNEIDefaultHandler;

/** R or U on the controller opens its fuel reference; U on a fluid/cell uses GT's native fluid matching. */
public final class GeneratorRecipeHandler extends GTNEIDefaultHandler {

    public GeneratorRecipeHandler() {
        super(GeneratorRecipes.RECIPES.getDefaultRecipeCategory());
    }

    @Override
    public TemplateRecipeHandler newInstance() {
        return new GeneratorRecipeHandler();
    }

    @Override
    protected int getDescriptionYOffset() {
        return 42;
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        if (result != null && result.isItemEqual(BenzeneFuels.generator)) {
            loadCraftingRecipes(GeneratorRecipes.RECIPES.unlocalizedName);
        } else {
            super.loadCraftingRecipes(result);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack input) {
        if (input != null && input.isItemEqual(BenzeneFuels.generator)) {
            loadCraftingRecipes(GeneratorRecipes.RECIPES.unlocalizedName);
        } else {
            super.loadUsageRecipes(input);
        }
    }
}
