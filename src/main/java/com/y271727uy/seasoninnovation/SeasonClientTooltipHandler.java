package com.y271727uy.seasoninnovation;

import com.y271727uy.seasoninnovation.DSL.SeasonFishingDsl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SeasonClientTooltipHandler {
    private SeasonClientTooltipHandler() {
    }

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!SeasonFishingDsl.rules().hasRule(event.getItemStack().getItem())) {
            return;
        }

        event.getToolTip().addAll(SeasonSupport.createSeasonInfoLines(
                "tooltip.season_innovation.fishing_seasons",
                SeasonFishingDsl.rules().getCatchSeasons(event.getItemStack().getItem())
        ));
    }
}

