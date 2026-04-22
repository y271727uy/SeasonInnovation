package com.y271727uy.seasoninnovation.client;

import com.y271727uy.seasoninnovation.SIMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = SIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SeasonClientConfigEvents {
    private SeasonClientConfigEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Object loadingContext = ModLoadingContext.class.getMethod("get").invoke(null);
                for (Method method : ModLoadingContext.class.getMethods()) {
                    if (!"registerExtensionPoint".equals(method.getName()) || method.getParameterCount() != 2) {
                        continue;
                    }

                    method.invoke(
                            loadingContext,
                            ConfigScreenHandler.ConfigScreenFactory.class,
                            (java.util.function.Supplier<ConfigScreenHandler.ConfigScreenFactory>) () ->
                                    new ConfigScreenHandler.ConfigScreenFactory((minecraft, parent) -> new SeasonConfigScreen(parent))
                    );
                    return;
                }
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Failed to register Season Innovation config screen.", exception);
            }

            throw new IllegalStateException("Could not find a compatible ModLoadingContext#registerExtensionPoint overload.");
        });
    }
}


