package com.y271727uy.seasoninnovation.farming;

import com.y271727uy.seasoninnovation.Config;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

public final class SeasonFarmingHandler {
    private static final ThreadLocal<Boolean> APPLYING_RAIN_BONUS = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public void onCropGrowPre(BlockEvent.CropGrowEvent.Pre event) {
        if (!Config.weatherAffectsCropGrowth || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (FarmingWeatherSupport.isSnowFallingOn(level, event.getPos())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onCropGrowPost(BlockEvent.CropGrowEvent.Post event) {
        if (!Config.weatherAffectsCropGrowth || APPLYING_RAIN_BONUS.get() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        if (Config.rainGrowthBonusChance <= 0.0D
                || !didCropAdvance(event)
                || FarmingWeatherSupport.isSnowFallingOn(level, event.getPos())
                || !FarmingWeatherSupport.isRainFallingOn(level, event.getPos())) {
            return;
        }

        if (level.random.nextDouble() >= Config.rainGrowthBonusChance) {
            return;
        }

        BlockState currentState = level.getBlockState(event.getPos());
        if (!currentState.isRandomlyTicking()) {
            return;
        }

        APPLYING_RAIN_BONUS.set(true);
        try {
            currentState.randomTick(level, event.getPos(), level.random);
        } finally {
            APPLYING_RAIN_BONUS.set(false);
        }
    }

    private static boolean didCropAdvance(BlockEvent.CropGrowEvent.Post event) {
        return !Objects.equals(event.getOriginalState(), event.getState());
    }
}

