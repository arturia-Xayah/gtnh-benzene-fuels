package dev.benzenefuels;

/** Numerical upgrade definitions. No survival recipe is invented for an undecided design. */
public enum GeneratorRoute {

    TECHNOLOGY("technology", new double[] { 1.01, 1.02, 1.05, 1.07, 1.1 }, new int[] { 2, 4, 8, 16, 32 }),
    MAGIC("magic", new double[] { 1.01, 1.05, 1.1, 1.2, 1.3 }, new int[] { 2, 4, 6, 8, 10 });

    public final String key;
    private final double[] bases;
    private final int[] exponents;

    GeneratorRoute(String key, double[] bases, int[] exponents) {
        this.key = key;
        this.bases = bases;
        this.exponents = exponents;
    }

    public double base(int mk) {
        checkMk(mk);
        return bases[mk - 1];
    }

    public int exponent(int mk) {
        checkMk(mk);
        return exponents[mk - 1];
    }

    public String baseFluidId(int mk) {
        checkMk(mk);
        return "benzenefuels." + key + ".base.mk" + mk;
    }

    private static void checkMk(int mk) {
        if (mk < 1 || mk > 5) throw new IllegalArgumentException("Mk must be 1 through 5");
    }
}
