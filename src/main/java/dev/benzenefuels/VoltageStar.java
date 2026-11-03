package dev.benzenefuels;

/** One output per recipe. Quantities count each of the eight GT component families. */
public enum VoltageStar {

    LV,
    MV,
    HV,
    EV,
    IV,
    LuV,
    ZPM,
    UV,
    UHV,
    UEV,
    UIV,
    UMV,
    UXV;

    public int componentsEach() {
        return Math.min(1 << ordinal(), 1024);
    }

    public int previousStars() {
        return ordinal() == 0 ? 0 : ordinal() <= LuV.ordinal() ? 16 : 4;
    }

    public int durationTicks() {
        return 160 << ordinal();
    }

    public int voltage() {
        return 8 << (2 * (ordinal() + 1));
    }

    public boolean assemblyLine() {
        return ordinal() >= LuV.ordinal();
    }

    public String key() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Fully expanded components, per family, from LV through this star. */
    public long[] expandedComponents() {
        long[] amounts = new long[ordinal() + 1];
        long multiplier = 1;
        for (int i = ordinal(); i >= 0; i--) {
            amounts[i] = Math.multiplyExact(multiplier, values()[i].componentsEach());
            if (i > 0) multiplier = Math.multiplyExact(multiplier, values()[i].previousStars());
        }
        return amounts;
    }
}
