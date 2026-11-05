package dev.benzenefuels;

import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.BooleanSyncValue;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ListWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import gregtech.api.modularui2.GTGuiTextures;
import gregtech.common.gui.modularui.multiblock.base.MTEMultiBlockBaseGui;

/** Compact native terminal, separate wireless button, and a synchronized side settings panel. */
public final class ResonanceGeneratorGui extends MTEMultiBlockBaseGui<ResonanceGenerator> {

    public ResonanceGeneratorGui(ResonanceGenerator machine) {
        super(machine);
    }

    @Override
    protected int getTerminalRowHeight() {
        return 170;
    }

    @Override
    protected int getBasePanelHeight() {
        return super.getBasePanelHeight() + 76;
    }

    @Override
    protected void registerSyncValues(PanelSyncManager sync) {
        super.registerSyncValues(sync);
        sync.syncValue(
            "bf.maximum",
            new IntSyncValue(multiblock::getMaximumLitres, multiblock::setMaximumLitres).allowC2S());
        sync.syncValue(
            "bf.wireless",
            new BooleanSyncValue(multiblock::getWireless, multiblock::setWireless).allowC2S());
        sync.syncValue("bf.info", new StringSyncValue(multiblock::getDisplaySnapshot));
    }

    @Override
    protected ListWidget<IWidget, ?> createTerminalTextWidget(PanelSyncManager sync, ModularPanel parent) {
        StringSyncValue info = sync.findSyncHandler("bf.info", StringSyncValue.class);
        return super.createTerminalTextWidget(sync, parent).child(
            new TextWidget<>(IKey.dynamic(() -> GeneratorDisplay.text(info.getValue()))).color(0xFFFFFFFF)
                .fullWidth()
                .tooltipBuilder(t -> t.addLine(IKey.dynamic(() -> GeneratorDisplay.details(info.getValue())))));
    }

    @Override
    protected Flow createLeftPanelGapRow(ModularPanel parent, PanelSyncManager sync) {
        BooleanSyncValue wireless = sync.findSyncHandler("bf.wireless", BooleanSyncValue.class);
        return Flow.row()
            .coverChildrenWidth()
            .fullHeight()
            .child(
                new ToggleButton().value(wireless)
                    .size(18, 18)
                    .overlay(true, GTGuiTextures.OVERLAY_BUTTON_WIRELESS_ON)
                    .overlay(false, GTGuiTextures.OVERLAY_BUTTON_WIRELESS_OFF)
                    .tooltipBuilder(t -> t.addLine(IKey.lang("gui.benzenefuels.wireless"))));
    }

    @Override
    protected Flow createRightPanelGapRow(ModularPanel parent, PanelSyncManager sync) {
        IPanelHandler settings = sync
            .syncedPanel("bf.settings", true, (panelSync, handler) -> createFuelSettings(panelSync, parent));
        return Flow.row()
            .coverChildrenWidth()
            .fullHeight()
            .rightRel(0)
            .child(
                new ButtonWidget<>().size(18, 18)
                    .overlay(GTGuiTextures.TT_OVERLAY_BUTTON_HEAT_ON)
                    .onMousePressed(button -> {
                        if (settings.isPanelOpen()) settings.closePanel();
                        else settings.openPanel();
                        return true;
                    })
                    .tooltipBuilder(t -> t.addLine(IKey.lang("gui.benzenefuels.fuel_settings"))));
    }

    private ModularPanel createFuelSettings(PanelSyncManager sync, ModularPanel parent) {
        // The child panel owns its sync handler so opening/closing never reuses a handler from another panel.
        IntSyncValue maximum = new IntSyncValue(multiblock::getMaximumLitres, multiblock::setMaximumLitres).allowC2S();
        sync.syncValue("bf.settings.maximum", maximum);
        return new ModularPanel("bf.fuel.settings").relative(parent)
            .leftRel(1)
            .topRel(0)
            .size(142, 88)
            .background(GTGuiTextures.BACKGROUND_STANDARD)
            .child(
                Flow.column()
                    .full()
                    .padding(6)
                    .child(
                        IKey.lang("gui.benzenefuels.fuel_settings")
                            .asWidget()
                            .marginBottom(6))
                    .child(
                        IKey.lang("gui.benzenefuels.maximum")
                            .asWidget()
                            .fullWidth()
                            .marginBottom(3))
                    .child(
                        new TextFieldWidget().value(maximum)
                            .numbersInt(1, Integer.MAX_VALUE)
                            .size(126, 18))
                    .child(
                        IKey.lang("gui.benzenefuels.settings_hint")
                            .asWidget()
                            .fullWidth()
                            .marginTop(4)));
    }

    @Override
    protected boolean shouldDisplayVoidExcess() {
        return false;
    }

    @Override
    protected boolean shouldDisplayInputSeparation() {
        return false;
    }

    @Override
    protected boolean shouldDisplayBatchMode() {
        return false;
    }

    @Override
    protected boolean shouldDisplayRecipeLock() {
        return false;
    }
}
