package dev.benzenefuels;

/** Stable fluid identifiers: display-name changes must not change saved fluid IDs. */
public enum FuelStage {

    LUV("luv", "Hyper-Enriched Platinum-Group Benzene", "超富集铂族苯", 13940L, 6, 0xA6DAFF),
    ZPM("zpm", "ZPM Benzene", "ZPM苯", 136200L, 7, 0x80BFFF),
    UV("uv", "Neutron-Supersaturated Benzene", "中子过饱和苯", 1491000L, 8, 0x9D9BFF),
    UHV("uhv", "UHV Benzene", "UHV苯", 18290000L, 9, 0xE7E7FF),
    UEV("uev", "UEV Benzene", "UEV苯", 251500000L, 10, 0xF3A6F0),
    UIV("uiv", "Causal-Loop Benzene", "因果闭环苯", 3876000000L, 11, 0xB887FF),
    UMV("umv", "Eternally Attuned Benzene", "永恒调律苯", 66950000000L, 12, 0x8FECE7),
    UXV("uxv", "Eigenbenzene Cosmic Fuel", "苯征宇宙燃料", 1296000000000L, 13, 0xFFF2AF);

    public static final int BATCH_LITERS = 50000;
    public static final int DURATION_TICKS = 200;
    public static final int SOLID_COST_MULTIPLIER = 1024;
    public static final int PLASMA_COST_MULTIPLIER = 16384;
    public static final int SELECTED_FLUID_COST_MULTIPLIER = 124;
    public static final int ZPM_TIN_PLASMA_LITERS = 10 * PLASMA_COST_MULTIPLIER;
    public final String key, englishName, chineseName;
    public final long euPerLiter;
    public final int voltageTier, color;

    FuelStage(String key, String englishName, String chineseName, long euPerLiter, int voltageTier, int color) {
        this.key = key;
        this.englishName = englishName;
        this.chineseName = chineseName;
        this.euPerLiter = euPerLiter;
        this.voltageTier = voltageTier;
        this.color = color;
    }

    public String fluidId() {
        return "benzenefuels." + key;
    }

    public String fluidTexture() {
        return "benzenefuels:fluids/benzene_" + key;
    }

    public String cellTexture() {
        return "benzenefuels:cell_" + key;
    }

    public boolean supportsNativeGasRecipe() {
        return euPerLiter <= Integer.MAX_VALUE;
    }

    public long voltage() {
        return 8L << (2 * voltageTier);
    }

    public long theoreticalPower(long litersPerTick) {
        if (litersPerTick < 0) throw new IllegalArgumentException("Negative fuel flow");
        return Math.multiplyExact(litersPerTick, euPerLiter);
    }
}
