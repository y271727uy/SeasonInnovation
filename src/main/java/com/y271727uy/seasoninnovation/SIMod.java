package com.y271727uy.seasoninnovation;

import com.mojang.logging.LogUtils;
import com.y271727uy.seasoninnovation.farming.SeasonFarmingHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

import java.lang.reflect.Method;

@Mod(SIMod.MODID)
public final class SIMod {
    public static final String MODID = "season_innovation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SIMod() {
        registerCommonConfig();
        MinecraftForge.EVENT_BUS.register(new SeasonFishingHandler());
        MinecraftForge.EVENT_BUS.register(new SeasonBreedingHandler());
        MinecraftForge.EVENT_BUS.register(new SeasonFarmingHandler());
        LOGGER.info("Season Innovation is loading with Serene Seasons integration enabled.");
    }

    private static void registerCommonConfig() {
        try {
            Object loadingContext = ModLoadingContext.class.getMethod("get").invoke(null);
            for (Method method : ModLoadingContext.class.getMethods()) {
                if (!"registerConfig".equals(method.getName()) || method.getParameterCount() != 2) {
                    continue;
                }

                method.invoke(loadingContext, ModConfig.Type.COMMON, Config.SPEC);
                return;
            }
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to register Season Innovation common config.", exception);
        }

        throw new IllegalStateException("Could not find a compatible ModLoadingContext#registerConfig overload.");
    }
}
