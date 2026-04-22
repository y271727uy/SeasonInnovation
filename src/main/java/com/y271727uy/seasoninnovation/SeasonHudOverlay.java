package com.y271727uy.seasoninnovation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public final class SeasonHudOverlay {
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final float CLOCK_SCALE = 0.75F;
    private static final int CLOCK_TEXT_SPACING = 14;

    public static final IGuiOverlay OVERLAY = SeasonHudOverlay::render;

    private SeasonHudOverlay() {
    }

    private static void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.level == null || minecraft.player == null) {
            return;
        }

        if (minecraft.options.renderDebug) {
            return;
        }

        renderScaledDay(guiGraphics, minecraft);
        renderScaledTime(guiGraphics, minecraft);
    }

    private static void renderScaledDay(GuiGraphics guiGraphics, Minecraft minecraft) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(Config.hudDayX, Config.hudDayY, 0.0F);
        guiGraphics.pose().scale(Config.hudScale, Config.hudScale, 1.0F);
        guiGraphics.drawString(minecraft.font, SeasonSupport.createHudDayText(minecraft.level), 0, 0, TEXT_COLOR, true);
        guiGraphics.pose().popPose();
    }

    private static void renderScaledTime(GuiGraphics guiGraphics, Minecraft minecraft) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(Config.hudTimeX, Config.hudTimeY, 0.0F);
        guiGraphics.pose().scale(Config.hudScale, Config.hudScale, 1.0F);
        renderClock(guiGraphics, 0, -2);
        guiGraphics.drawString(minecraft.font, SeasonSupport.formatHudTime(minecraft.level), CLOCK_TEXT_SPACING, 0, TEXT_COLOR, true);
        guiGraphics.pose().popPose();
    }

    private static void renderClock(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, 0.0F);
        guiGraphics.pose().scale(CLOCK_SCALE, CLOCK_SCALE, 1.0F);
        guiGraphics.renderItem(new ItemStack(Items.CLOCK), 0, 0);
        guiGraphics.pose().popPose();
    }
}


