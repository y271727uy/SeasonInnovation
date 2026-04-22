package com.y271727uy.seasoninnovation;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = SIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(SIMod.MODID + "-common.toml");

    private static final ForgeConfigSpec.BooleanValue RESTRICT_FISHING_LOOT = BUILDER
            .comment("Whether vanilla fishing catches should be seasonally blocked or reduced.")
            .define("restrictFishingLoot", true);

    private static final ForgeConfigSpec.BooleanValue RESTRICT_ANIMAL_BREEDING = BUILDER
            .comment("Whether vanilla animal breeding should be blocked outside the breeding season.")
            .define("restrictAnimalBreeding", true);

    private static final ForgeConfigSpec.BooleanValue SEND_ACTION_BAR_FEEDBACK = BUILDER
            .comment("Whether players should receive an action bar hint when a seasonal rule blocks them.")
            .define("sendActionBarFeedback", true);

    private static final ForgeConfigSpec.BooleanValue WEATHER_AFFECTS_CROP_GROWTH = BUILDER
            .comment("Whether rain and snow should affect crop growth.")
            .define("weatherAffectsCropGrowth", true);

    private static final ForgeConfigSpec.DoubleValue RAIN_GROWTH_BONUS_CHANCE = BUILDER
            .comment("Chance for rain-exposed crops to receive one extra random growth tick after growing.")
            .defineInRange("rainGrowthBonusChance", 0.35D, 0.0D, 1.0D);

    private static final ForgeConfigSpec.IntValue HUD_DAY_X = BUILDER
            .comment("Top-left anchored X coordinate for the day counter HUD.")
            .defineInRange("hudDayX", 6, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue HUD_DAY_Y = BUILDER
            .comment("Top-left anchored Y coordinate for the day counter HUD.")
            .defineInRange("hudDayY", 14, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue HUD_TIME_X = BUILDER
            .comment("Top-left anchored X coordinate for the time HUD, including the clock icon.")
            .defineInRange("hudTimeX", 6, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.IntValue HUD_TIME_Y = BUILDER
            .comment("Top-left anchored Y coordinate for the time HUD, including the clock icon.")
            .defineInRange("hudTimeY", 28, 0, Integer.MAX_VALUE);

    private static final ForgeConfigSpec.DoubleValue HUD_SCALE = BUILDER
            .comment("Overall scale for the top-left HUD blocks.")
            .defineInRange("hudScale", 1.0D, 0.25D, 4.0D);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean restrictFishingLoot;
    public static boolean restrictAnimalBreeding;
    public static boolean sendActionBarFeedback;
    public static boolean weatherAffectsCropGrowth;
    public static double rainGrowthBonusChance;
    public static int hudDayX;
    public static int hudDayY;
    public static int hudTimeX;
    public static int hudTimeY;
    public static float hudScale;

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }

        restrictFishingLoot = RESTRICT_FISHING_LOOT.get();
        restrictAnimalBreeding = RESTRICT_ANIMAL_BREEDING.get();
        sendActionBarFeedback = SEND_ACTION_BAR_FEEDBACK.get();
        weatherAffectsCropGrowth = WEATHER_AFFECTS_CROP_GROWTH.get();
        rainGrowthBonusChance = RAIN_GROWTH_BONUS_CHANCE.get();
        hudDayX = HUD_DAY_X.get();
        hudDayY = HUD_DAY_Y.get();
        hudTimeX = HUD_TIME_X.get();
        hudTimeY = HUD_TIME_Y.get();
        hudScale = HUD_SCALE.get().floatValue();
    }

    public static void saveFromScreen(
            boolean restrictFishingLootValue,
            boolean restrictAnimalBreedingValue,
            boolean sendActionBarFeedbackValue,
            boolean weatherAffectsCropGrowthValue,
            double rainGrowthBonusChanceValue,
            int hudDayXValue,
            int hudDayYValue,
            int hudTimeXValue,
            int hudTimeYValue,
            float hudScaleValue
    ) {
        restrictFishingLoot = restrictFishingLootValue;
        restrictAnimalBreeding = restrictAnimalBreedingValue;
        sendActionBarFeedback = sendActionBarFeedbackValue;
        weatherAffectsCropGrowth = weatherAffectsCropGrowthValue;
        rainGrowthBonusChance = clampDouble(rainGrowthBonusChanceValue, 0.0D, 1.0D);
        hudDayX = clampInt(hudDayXValue, 0, Integer.MAX_VALUE);
        hudDayY = clampInt(hudDayYValue, 0, Integer.MAX_VALUE);
        hudTimeX = clampInt(hudTimeXValue, 0, Integer.MAX_VALUE);
        hudTimeY = clampInt(hudTimeYValue, 0, Integer.MAX_VALUE);
        hudScale = (float) clampDouble(hudScaleValue, 0.25D, 4.0D);

        saveToDisk();
    }

    private static void saveToDisk() {
        String content = """
                #Whether vanilla fishing catches should be seasonally blocked or reduced.
                restrictFishingLoot = %s
                #Whether vanilla animal breeding should be blocked outside the breeding season.
                restrictAnimalBreeding = %s
                #Whether players should receive an action bar hint when a seasonal rule blocks them.
                sendActionBarFeedback = %s
                #Whether rain and snow should affect crop growth.
                weatherAffectsCropGrowth = %s
                #Chance for rain-exposed crops to receive one extra random growth tick after growing.
                #Range: 0.0 ~ 1.0
                rainGrowthBonusChance = %s
                #Top-left anchored X coordinate for the day counter HUD.
                #Range: > 0
                hudDayX = %d
                #Top-left anchored Y coordinate for the day counter HUD.
                #Range: > 0
                hudDayY = %d
                #Top-left anchored X coordinate for the time HUD, including the clock icon.
                #Range: > 0
                hudTimeX = %d
                #Top-left anchored Y coordinate for the time HUD, including the clock icon.
                #Range: > 0
                hudTimeY = %d
                #Overall scale for the top-left HUD blocks.
                #Range: 0.25 ~ 4.0
                hudScale = %s

                """.formatted(
                restrictFishingLoot,
                restrictAnimalBreeding,
                sendActionBarFeedback,
                weatherAffectsCropGrowth,
                formatDouble(rainGrowthBonusChance),
                hudDayX,
                hudDayY,
                hudTimeX,
                hudTimeY,
                formatDouble(hudScale)
        );

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, content, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save Season Innovation config.", exception);
        }
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatDouble(double value) {
        return String.format(Locale.ROOT, "%.3f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
