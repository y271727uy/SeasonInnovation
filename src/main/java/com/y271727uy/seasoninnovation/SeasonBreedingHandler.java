package com.y271727uy.seasoninnovation;

import com.y271727uy.seasoninnovation.DSL.SeasonBreedingDsl;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

public final class SeasonBreedingHandler {
    @SubscribeEvent
    public void onAnimalInteract(PlayerInteractEvent.EntityInteract event) {
        if (!Config.restrictAnimalBreeding || !(event.getTarget() instanceof Animal animal)) {
            return;
        }

        ItemStack heldItem = event.getItemStack();
        if (!animal.isFood(heldItem) || !animal.canFallInLove()) {
            return;
        }

        if (isAnimalBreedingAllowed(animal)) {
            return;
        }

        event.setCancellationResult(InteractionResult.FAIL);
        event.setCanceled(true);
        SeasonSupport.sendPlayerFeedback(event.getEntity(), createBreedingBlockedMessage(animal));
    }

    @SubscribeEvent
    public void onBabyEntitySpawn(BabyEntitySpawnEvent event) {
        if (!Config.restrictAnimalBreeding || !(event.getParentA() instanceof Animal animal)) {
            return;
        }

        if (isAnimalBreedingAllowed(animal)) {
            return;
        }

        event.setCanceled(true);
        Player causedByPlayer = event.getCausedByPlayer();
        if (causedByPlayer != null) {
            SeasonSupport.sendPlayerFeedback(causedByPlayer, createBreedingBlockedMessage(animal));
        }
    }

    private static boolean isAnimalBreedingAllowed(Animal animal) {
        SeasonSupport.SeasonWindow season = SeasonSupport.getSeasonWindow(animal.level());
        return season == null || getAllowedBreedingSeasons(animal).contains(season);
    }

    static Component createBreedingBlockedMessage(Animal animal) {
        return Component.translatable(
                "message.season_innovation.breeding_blocked",
                SeasonSupport.formatSeasonsInlineOrYearRound(getAllowedBreedingSeasons(animal))
        );
    }

    static Collection<SeasonSupport.SeasonWindow> getAllowedBreedingSeasons(Animal animal) {
        return SeasonBreedingDsl.rules().getAllowedSeasons(animal.getType());
    }
}
