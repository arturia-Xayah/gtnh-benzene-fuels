package dev.benzenefuels;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** One explicit texture for every registered component metadata value. */
public final class ComponentTextures {

    private static final Map<Integer, String> TEXTURES = create();

    private ComponentTextures() {}

    private static Map<Integer, String> create() {
        Map<Integer, String> textures = new LinkedHashMap<>();
        textures.put(0, "benzenefuels:component_activator");
        textures.put(FuelContent.CATALYST_META, "benzenefuels:catalyst_quantum");
        for (FuelStage stage : FuelStage.values()) textures.put(stage.ordinal() + 1, stage.cellTexture());
        for (VoltageStar stage : VoltageStar.values())
            textures.put(FuelContent.STAR_META + stage.ordinal(), "benzenefuels:star_" + stage.key());
        for (GeneratorRoute route : GeneratorRoute.values()) for (int mk = 1; mk <= 5; mk++) {
            int offset = route.ordinal() * 10 + mk - 1;
            textures.put(FuelContent.EXPONENT_META + offset, "benzenefuels:core_" + route.key + "_mk" + mk);
            textures.put(FuelContent.BASE_CELL_META + offset, "benzenefuels:base_cell_" + route.key + "_mk" + mk);
        }
        return Collections.unmodifiableMap(textures);
    }

    public static Map<Integer, String> all() {
        return TEXTURES;
    }

    public static String baseFluid(GeneratorRoute route, int mk) {
        route.base(mk);
        return "benzenefuels:fluids/" + route.key + "_base_mk" + mk;
    }
}
