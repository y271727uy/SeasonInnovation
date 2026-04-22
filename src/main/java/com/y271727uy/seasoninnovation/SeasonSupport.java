package com.y271727uy.seasoninnovation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import sereneseasons.api.season.Season;
import sereneseasons.api.season.SeasonHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SeasonSupport {
    private static final long TICKS_PER_DAY = 24000L;
    private static final int DEFAULT_SUB_SEASON_DURATION = 8;
    private static final long DEFAULT_SEASON_DAY_DURATION = 24000L;
    private static volatile SeasonCalendarSettings cachedSeasonCalendarSettings;

    private SeasonSupport() {
    }

    static SeasonWindow getSeasonWindow(Level level) {
        var seasonState = SeasonHelper.getSeasonState(level);
        if (seasonState == null) {
            return null;
        }

        Season.SubSeason subSeason = seasonState.getSubSeason();
        return switch (subSeason) {
            case EARLY_SPRING, MID_SPRING, LATE_SPRING -> SeasonWindow.SPRING;
            case EARLY_SUMMER, MID_SUMMER, LATE_SUMMER -> SeasonWindow.SUMMER;
            case EARLY_AUTUMN, MID_AUTUMN, LATE_AUTUMN -> SeasonWindow.AUTUMN;
            case EARLY_WINTER, MID_WINTER, LATE_WINTER -> SeasonWindow.WINTER;
        };
    }

    static boolean isModdedEntity(EntityType<?> entityType) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return key != null && !"minecraft".equals(key.getNamespace());
    }

    static Component getSeasonName(SeasonWindow season) {
        return Component.translatable("season.season_innovation." + season.name().toLowerCase(Locale.ROOT))
                .withStyle(getSeasonColor(season));
    }

    public static List<SeasonWindow> allSeasons() {
        return List.of(SeasonWindow.values());
    }

    static boolean isYearRound(Collection<SeasonWindow> seasons) {
        return seasons.containsAll(EnumSet.allOf(SeasonWindow.class));
    }

    static Component formatSeasonsOrYearRound(Collection<SeasonWindow> seasons) {
        if (seasons.isEmpty() || isYearRound(seasons)) {
            return Component.translatable("season.season_innovation.year_round")
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        return formatSeasonList(seasons);
    }

    static Component formatSeasonsInlineOrYearRound(Collection<SeasonWindow> seasons) {
        if (seasons.isEmpty() || isYearRound(seasons)) {
            return Component.translatable("season.season_innovation.year_round")
                    .withStyle(ChatFormatting.LIGHT_PURPLE);
        }

        return formatSeasonInlineList(seasons);
    }

    static Component formatSeasonList(Iterable<SeasonWindow> seasons) {
        MutableComponent result = Component.empty();
        Iterator<SeasonWindow> iterator = orderedSeasons(seasons).iterator();
        while (iterator.hasNext()) {
            result.append(getSeasonName(iterator.next()));
            if (iterator.hasNext()) {
                result.append(Component.literal("\n"));
            }
        }
        return result;
    }

    static Component formatSeasonInlineList(Iterable<SeasonWindow> seasons) {
        MutableComponent result = Component.empty();
        Iterator<SeasonWindow> iterator = orderedSeasons(seasons).iterator();
        while (iterator.hasNext()) {
            result.append(getSeasonName(iterator.next()));
            if (iterator.hasNext()) {
                result.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
            }
        }
        return result;
    }

    static Component createSeasonInfoLine(String translationKey, Collection<SeasonWindow> seasons) {
        return Component.translatable(translationKey, formatSeasonsOrYearRound(seasons)).withStyle(ChatFormatting.GRAY);
    }

    static List<Component> createSeasonInfoLines(String translationKey, Collection<SeasonWindow> seasons) {
        List<Component> lines = new ArrayList<>();
        if (seasons.isEmpty() || isYearRound(seasons)) {
            lines.add(createSeasonInfoLine(translationKey, seasons));
            return lines;
        }

        lines.add(Component.translatable(translationKey, Component.empty()).withStyle(ChatFormatting.GRAY));
        for (SeasonWindow season : orderedSeasons(seasons)) {
            lines.add(getSeasonName(season));
        }
        return lines;
    }

    private static List<SeasonWindow> orderedSeasons(Iterable<SeasonWindow> seasons) {
        EnumSet<SeasonWindow> orderedSet = EnumSet.noneOf(SeasonWindow.class);
        for (SeasonWindow season : seasons) {
            orderedSet.add(season);
        }
        return List.copyOf(orderedSet);
    }

    private static ChatFormatting getSeasonColor(SeasonWindow season) {
        return switch (season) {
            case SPRING -> ChatFormatting.GREEN;
            case SUMMER -> ChatFormatting.YELLOW;
            case AUTUMN -> ChatFormatting.GOLD;
            case WINTER -> ChatFormatting.AQUA;
        };
    }

    public static String getEntityId(EntityType<?> entityType) {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
        return key == null ? null : key.toString();
    }

    public static String getItemId(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return key == null ? null : key.toString();
    }

    public static Item resolveItem(String itemId) {
        ResourceLocation key = ResourceLocation.tryParse(itemId);
        return key == null ? null : ForgeRegistries.ITEMS.getValue(key);
    }

    static boolean isMissingItem(String itemId) {
        ResourceLocation key = ResourceLocation.tryParse(itemId);
        return key == null || !ForgeRegistries.ITEMS.containsKey(key);
    }

    public static Set<SeasonWindow> normalizeSeasons(SeasonWindow... seasons) {
        if (seasons.length == 0) {
            return EnumSet.allOf(SeasonWindow.class);
        }

        return EnumSet.copyOf(List.of(seasons));
    }

    public static Component createHudDayText(Level level) {
        SeasonCalendarSettings settings = getSeasonCalendarSettings();
        long elapsedDays = Math.max(0L, level.getDayTime()) / settings.dayDuration();
        long daysPerYear = settings.daysPerYear();
        long year = elapsedDays / daysPerYear;
        long dayOfYear = (elapsedDays % daysPerYear) + 1L;
        return Component.translatable("hud.season_innovation.day_counter", year, dayOfYear);
    }

    public static String formatHudTime(Level level) {
        long dayTicks = Math.floorMod(level.getDayTime(), TICKS_PER_DAY);
        int hours = (int) ((dayTicks / 1000L + 6L) % 24L);
        int minutes = (int) (((dayTicks % 1000L) * 60L) / 1000L);
        return String.format(Locale.ROOT, "%02d:%02d", hours, minutes);
    }

    private static SeasonCalendarSettings getSeasonCalendarSettings() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("sereneseasons").resolve("seasons.toml");
        long lastModified = getLastModified(configPath);
        SeasonCalendarSettings cached = cachedSeasonCalendarSettings;
        if (cached != null && cached.lastModified() == lastModified) {
            return cached;
        }

        SeasonCalendarSettings loaded = loadSeasonCalendarSettings(configPath, lastModified);
        cachedSeasonCalendarSettings = loaded;
        return loaded;
    }

    private static SeasonCalendarSettings loadSeasonCalendarSettings(Path configPath, long lastModified) {
        int subSeasonDuration = DEFAULT_SUB_SEASON_DURATION;
        long dayDuration = DEFAULT_SEASON_DAY_DURATION;

        if (Files.exists(configPath)) {
            boolean inTimeSettings = false;
            try {
                for (String rawLine : Files.readAllLines(configPath, StandardCharsets.UTF_8)) {
                    String line = stripTomlLine(rawLine);
                    if (line.isEmpty()) {
                        continue;
                    }

                    if (line.startsWith("[") && line.endsWith("]")) {
                        inTimeSettings = "[time_settings]".equals(line);
                        continue;
                    }

                    if (!inTimeSettings) {
                        continue;
                    }

                    if (line.startsWith("sub_season_duration")) {
                        subSeasonDuration = Math.max(1, parsePositiveInt(line, DEFAULT_SUB_SEASON_DURATION));
                    } else if (line.startsWith("day_duration")) {
                        dayDuration = Math.max(1L, parsePositiveLong(line, DEFAULT_SEASON_DAY_DURATION));
                    }
                }
            } catch (IOException ignored) {
                subSeasonDuration = DEFAULT_SUB_SEASON_DURATION;
                dayDuration = DEFAULT_SEASON_DAY_DURATION;
            }
        }

        return new SeasonCalendarSettings(
                subSeasonDuration,
                dayDuration,
                (long) subSeasonDuration * Season.SubSeason.values().length,
                lastModified
        );
    }

    private static String stripTomlLine(String rawLine) {
        String line = rawLine;
        int commentIndex = line.indexOf('#');
        if (commentIndex >= 0) {
            line = line.substring(0, commentIndex);
        }
        return line.trim();
    }

    private static int parsePositiveInt(String line, int fallback) {
        int separator = line.indexOf('=');
        if (separator < 0) {
            return fallback;
        }

        try {
            return Integer.parseInt(line.substring(separator + 1).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static long parsePositiveLong(String line, long fallback) {
        int separator = line.indexOf('=');
        if (separator < 0) {
            return fallback;
        }

        try {
            return Long.parseLong(line.substring(separator + 1).trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static long getLastModified(Path path) {
        try {
            return Files.exists(path) ? Files.getLastModifiedTime(path).toMillis() : Long.MIN_VALUE;
        } catch (IOException ignored) {
            return Long.MIN_VALUE;
        }
    }

    static void sendPlayerFeedback(Player player, Component message) {
        //noinspection resource
        if (!Config.sendActionBarFeedback || player.level().isClientSide()) {
            return;
        }

        player.displayClientMessage(message.copy().withStyle(ChatFormatting.GOLD), true);
    }

    public enum SeasonWindow {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER
    }

    private record SeasonCalendarSettings(int subSeasonDuration, long dayDuration, long daysPerYear, long lastModified) {
    }
}


