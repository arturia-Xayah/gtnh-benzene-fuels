package dev.benzenefuels.nei;

import codechicken.nei.api.API;
import codechicken.nei.api.IConfigureNEI;
import dev.benzenefuels.Tags;

/** Discovered by NEI only on the client. GT registers the map's handler metadata and controller catalyst. */
public final class NEIBenzeneFuelsConfig implements IConfigureNEI {

    @Override
    public void loadConfig() {
        GeneratorRecipeHandler handler = new GeneratorRecipeHandler();
        API.registerRecipeHandler(handler);
        API.registerUsageHandler(handler);
    }

    @Override
    public String getName() {
        return "Benzene Fuels";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
