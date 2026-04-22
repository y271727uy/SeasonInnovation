package com.y271727uy.seasoninnovation.client;

import com.y271727uy.seasoninnovation.Config;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SeasonConfigScreen extends Screen {
    private final Screen parent;
    private int leftColumnX;
    private int rightColumnX;

    private int rainGrowthLabelY;
    private int hudScaleLabelY;
    private int hudXLabelY;
    private int hudYLabelY;

    private CycleButton<Boolean> restrictFishingLootButton;
    private CycleButton<Boolean> restrictAnimalBreedingButton;
    private CycleButton<Boolean> sendActionBarFeedbackButton;
    private CycleButton<Boolean> weatherAffectsCropGrowthButton;
    private EditBox rainGrowthBonusChanceBox;
    private EditBox hudXBox;
    private EditBox hudYBox;
    private EditBox hudScaleBox;

    public SeasonConfigScreen(Screen parent) {
        super(Component.translatable("screen.season_innovation.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 155;
        int right = this.width / 2 + 5;
        int rowWidth = 150;
        int labelWidth = 118;
        int fieldWidth = 32;
        int y = 32;
        int rowHeight = 24;

        this.leftColumnX = left;
        this.rightColumnX = right;

        restrictFishingLootButton = addRenderableWidget(createBooleanButton(left, y, rowWidth,
                Component.translatable("screen.season_innovation.config.restrict_fishing_loot"), Config.restrictFishingLoot));
        restrictAnimalBreedingButton = addRenderableWidget(createBooleanButton(right, y, rowWidth,
                Component.translatable("screen.season_innovation.config.restrict_animal_breeding"), Config.restrictAnimalBreeding));
        y += rowHeight;

        sendActionBarFeedbackButton = addRenderableWidget(createBooleanButton(left, y, rowWidth,
                Component.translatable("screen.season_innovation.config.send_action_bar_feedback"), Config.sendActionBarFeedback));
        weatherAffectsCropGrowthButton = addRenderableWidget(createBooleanButton(right, y, rowWidth,
                Component.translatable("screen.season_innovation.config.weather_affects_crop_growth"), Config.weatherAffectsCropGrowth));
        y += rowHeight + 6;

        rainGrowthLabelY = y + 6;
        rainGrowthBonusChanceBox = addRenderableWidget(createNumberBox(left + labelWidth, y, fieldWidth,
                Component.translatable("screen.season_innovation.config.rain_growth_bonus_chance"), Double.toString(Config.rainGrowthBonusChance)));

        hudScaleLabelY = y + 6;
        hudScaleBox = addRenderableWidget(createNumberBox(right + labelWidth, y, fieldWidth,
                Component.translatable("screen.season_innovation.config.hud_scale"), Float.toString(Config.hudScale)));
        y += rowHeight;

        hudXLabelY = y + 6;
        hudXBox = addRenderableWidget(createNumberBox(left + labelWidth, y, fieldWidth,
                Component.translatable("screen.season_innovation.config.hud_x"), Integer.toString(Config.hudX)));
        y += rowHeight;

        hudYLabelY = y + 6;
        hudYBox = addRenderableWidget(createNumberBox(left + labelWidth, y, fieldWidth,
                Component.translatable("screen.season_innovation.config.hud_y"), Integer.toString(Config.hudY)));

        int buttonY = this.height - 28;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onSave())
                .bounds(this.width / 2 - 102, buttonY, 100, 20)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> onClose())
                .bounds(this.width / 2 + 2, buttonY, 100, 20)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        renderLabels(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void onSave() {
        Config.saveFromScreen(
                restrictFishingLootButton.getValue(),
                restrictAnimalBreedingButton.getValue(),
                sendActionBarFeedbackButton.getValue(),
                weatherAffectsCropGrowthButton.getValue(),
                parseDouble(rainGrowthBonusChanceBox, Config.rainGrowthBonusChance),
                parseInt(hudXBox, Config.hudX),
                parseInt(hudYBox, Config.hudY),
                (float) parseDouble(hudScaleBox, Config.hudScale)
        );
        onClose();
    }

    private CycleButton<Boolean> createBooleanButton(int x, int y, int width, Component label, boolean initialValue) {
        return CycleButton.onOffBuilder(initialValue)
                .displayOnlyValue()
                .withTooltip(value -> null)
                .create(x, y, width, 20, label);
    }

    private EditBox createNumberBox(int x, int y, int width, Component message, String value) {
        EditBox editBox = new EditBox(this.font, x, y, width, 20, message);
        editBox.setValue(value);
        return editBox;
    }

    private void renderLabels(GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, Component.translatable("screen.season_innovation.config.rain_growth_bonus_chance"), leftColumnX, rainGrowthLabelY, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.season_innovation.config.hud_scale"), rightColumnX, hudScaleLabelY, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.season_innovation.config.hud_x"), leftColumnX, hudXLabelY, 0xFFFFFF, false);
        guiGraphics.drawString(this.font, Component.translatable("screen.season_innovation.config.hud_y"), leftColumnX, hudYLabelY, 0xFFFFFF, false);
    }

    private static int parseInt(EditBox editBox, int fallback) {
        try {
            return Integer.parseInt(editBox.getValue().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static double parseDouble(EditBox editBox, double fallback) {
        try {
            return Double.parseDouble(editBox.getValue().trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}



