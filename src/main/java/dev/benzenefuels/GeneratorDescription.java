package dev.benzenefuels;

import gregtech.api.util.MultiblockTooltipBuilder;

/** Shared localized controller description, including its Shift structure page. */
public final class GeneratorDescription {

    private GeneratorDescription() {}

    public static MultiblockTooltipBuilder tooltip(FuelDescription.Translation language) {
        MultiblockTooltipBuilder builder = new MultiblockTooltipBuilder()
            .addMachineType(language.text("gt.blockmachines.benzenefuels.resonance_generator.name"));
        for (String key : new String[] { "batch", "coils", "parallel", "actual", "height", "bases", "multiplier",
            "cores", "first_fuel", "overflow", "nei", "wireless" }) {
            builder.addInfo(language.text("tooltip.benzenefuels.tower." + key));
        }
        return builder.beginStructureBlock(33, 129, 33, false)
            .addController(language.text("tooltip.benzenefuels.tower.controller"))
            .addInputHatch("1-3", language.text("tooltip.benzenefuels.tower.input"), 1)
            .addMaintenanceHatch("1", language.text("tooltip.benzenefuels.tower.maintenance"), 1)
            .addDynamoHatch("0-16", language.text("tooltip.benzenefuels.tower.dynamo"), 2)
            .toolTipFinisher();
    }
}
