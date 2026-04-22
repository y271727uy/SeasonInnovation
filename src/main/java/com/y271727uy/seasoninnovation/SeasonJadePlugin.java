package com.y271727uy.seasoninnovation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Animal;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

import java.util.Objects;

@WailaPlugin
public final class SeasonJadePlugin implements IWailaPlugin {
    private static final AnimalBreedingProvider ANIMAL_BREEDING_PROVIDER = new AnimalBreedingProvider();

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(ANIMAL_BREEDING_PROVIDER, Animal.class);
    }

    private static final class AnimalBreedingProvider implements IEntityComponentProvider {
        private static final ResourceLocation UID = Objects.requireNonNull(ResourceLocation.tryBuild(SIMod.MODID, "animal_breeding_seasons"));

        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (!(accessor.getEntity() instanceof Animal animal)) {
                return;
            }

            for (var line : SeasonSupport.createSeasonInfoLines(
                    "tooltip.season_innovation.breeding_seasons",
                    SeasonBreedingHandler.getAllowedBreedingSeasons(animal)
            )) {
                tooltip.add(line);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }
    }
}


