package com.y271727uy.seasoninnovation;

import com.y271727uy.seasoninnovation.DSL.SeasonFishingDsl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public final class SeasonFishingHandler {
    @SubscribeEvent
    public void onItemFished(ItemFishedEvent event) {
        if (!Config.restrictFishingLoot) {
            return;
        }

        Player player = event.getEntity();
        SeasonSupport.SeasonWindow season = SeasonSupport.getSeasonWindow(player.level());
        if (season == null) {
            return;
        }

        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (!SeasonFishingDsl.rules().hasRule(drop.getItem())) {
                continue;
            }

            var seasonalFish = SeasonFishingDsl.rules().resolveCaughtFish(drop.getItem(), player.getRandom(), season);
            if (seasonalFish == drop.getItem()) {
                continue;
            }

            ItemStack replacement = new ItemStack(seasonalFish, drop.getCount());
            drops.set(i, replacement);
        }
    }
}



