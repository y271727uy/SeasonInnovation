package com.y271727uy.seasoninnovation.farming;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public final class FarmingWeatherSupport {
    private FarmingWeatherSupport() {
    }

    public static boolean isRainFallingOn(ServerLevel level, BlockPos cropPos) {
        return level.isRainingAt(cropPos.above());
    }

    public static boolean isSnowFallingOn(ServerLevel level, BlockPos cropPos) {
        BlockPos precipitationPos = cropPos.above();
        return level.isRaining()
                && level.canSeeSky(precipitationPos)
                && level.getBiome(precipitationPos).value().coldEnoughToSnow(precipitationPos);
    }
}

