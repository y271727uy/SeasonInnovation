package com.y271727uy.seasoninnovation.DSL;

import com.y271727uy.seasoninnovation.SeasonSupport;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SeasonFishingDsl {
    private static final SeasonFishingDsl RULES = createDefaults();

    private final Map<String, FishRule> rules = new LinkedHashMap<>();

    public static SeasonFishingDsl rules() {
        return RULES;
    }

    public FishRuleBuilder fish(String itemId) {
        return new FishRuleBuilder(this, itemId);
    }

    public boolean hasRule(Item item) {
        String itemId = SeasonSupport.getItemId(item);
        return itemId != null && rules.containsKey(itemId);
    }

    public Collection<SeasonSupport.SeasonWindow> getCatchSeasons(Item item) {
        FishRule rule = getRule(item);
        return rule == null ? SeasonSupport.allSeasons() : rule.catchSeasons();
    }

    public Item resolveCaughtFish(Item caughtItem, RandomSource random, SeasonSupport.SeasonWindow currentSeason) {
        if (getRule(caughtItem) == null) {
            return caughtItem;
        }

        ResolvedFishRule rerolledFish = rollSeasonalFish(random, currentSeason);
        return rerolledFish == null ? caughtItem : rerolledFish.item();
    }

    private ResolvedFishRule rollSeasonalFish(RandomSource random, SeasonSupport.SeasonWindow currentSeason) {
        List<ResolvedFishRule> resolvedRules = getResolvedRules();
        double totalWeight = 0.0D;
        for (ResolvedFishRule resolvedRule : resolvedRules) {
            totalWeight += resolvedRule.rule().getWeight(currentSeason);
        }

        if (totalWeight <= 0.0D) {
            return null;
        }

        double roll = random.nextDouble() * totalWeight;
        for (ResolvedFishRule resolvedRule : resolvedRules) {
            roll -= resolvedRule.rule().getWeight(currentSeason);
            if (roll < 0.0D) {
                return resolvedRule;
            }
        }

        return resolvedRules.isEmpty() ? null : resolvedRules.get(0);
    }

    private FishRule getRule(Item item) {
        String itemId = SeasonSupport.getItemId(item);
        return itemId == null ? null : rules.get(itemId);
    }

    private List<ResolvedFishRule> getResolvedRules() {
        List<ResolvedFishRule> resolvedRules = new ArrayList<>();
        for (FishRule rule : rules.values()) {
            Item item = SeasonSupport.resolveItem(rule.itemId());
            if (item == null) {
                continue;
            }

            if (rule.baseWeight() <= 0) {
                continue;
            }

            resolvedRules.add(new ResolvedFishRule(item, rule));
        }
        return resolvedRules;
    }

    private SeasonFishingDsl register(FishRule rule) {
        rules.put(rule.itemId(), rule);
        return this;
    }

    private static SeasonFishingDsl createDefaults() {
        SeasonFishingDsl dsl = new SeasonFishingDsl();
        dsl.fish("minecraft:cod")
                .baseWeight(60)
                .catchSeasons(SeasonSupport.SeasonWindow.AUTUMN, SeasonSupport.SeasonWindow.WINTER)
                .inSeasonWeightMultiplier(1.35F)
                .reducedOutOfSeason(0.45F)
                .register();
        dsl.fish("minecraft:salmon")
                .baseWeight(25)
                .catchSeasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.AUTUMN)
                .inSeasonWeightMultiplier(1.45F)
                .reducedOutOfSeason(0.40F)
                .register();
        dsl.fish("minecraft:pufferfish")
                .baseWeight(13)
                .catchSeasons(SeasonSupport.SeasonWindow.SUMMER, SeasonSupport.SeasonWindow.AUTUMN)
                .inSeasonWeightMultiplier(1.60F)
                .reducedOutOfSeason(0.20F)
                .register();
        dsl.fish("minecraft:tropical_fish")
                .baseWeight(2)
                .catchSeasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .inSeasonWeightMultiplier(1.75F)
                .reducedOutOfSeason(0.15F)
                .register();
        return dsl;
    }

    public static final class FishRuleBuilder {
        private final SeasonFishingDsl owner;
        private final String itemId;
        private int baseWeight = 1;
        private Set<SeasonSupport.SeasonWindow> catchSeasons = EnumSet.allOf(SeasonSupport.SeasonWindow.class);
        private float inSeasonWeightMultiplier = 1.0F;
        private float reducedWeightMultiplier = 1.0F;

        private FishRuleBuilder(SeasonFishingDsl owner, String itemId) {
            this.owner = owner;
            this.itemId = itemId;
        }

        public FishRuleBuilder baseWeight(int baseWeight) {
            this.baseWeight = Math.max(1, baseWeight);
            return this;
        }

        public FishRuleBuilder catchSeasons(SeasonSupport.SeasonWindow... seasons) {
            this.catchSeasons = SeasonSupport.normalizeSeasons(seasons);
            return this;
        }

        public FishRuleBuilder inSeasonWeightMultiplier(float multiplier) {
            this.inSeasonWeightMultiplier = Math.max(0.0F, multiplier);
            return this;
        }

        public FishRuleBuilder allYear() {
            this.catchSeasons = EnumSet.allOf(SeasonSupport.SeasonWindow.class);
            this.inSeasonWeightMultiplier = 1.0F;
            this.reducedWeightMultiplier = 1.0F;
            return this;
        }

        public FishRuleBuilder blockedOutOfSeason() {
            this.reducedWeightMultiplier = 0.0F;
            return this;
        }

        public FishRuleBuilder reducedOutOfSeason(float multiplier) {
            this.reducedWeightMultiplier = Math.max(0.0F, multiplier);
            return this;
        }

        public SeasonFishingDsl register() {
            return owner.register(new FishRule(itemId, baseWeight, catchSeasons, inSeasonWeightMultiplier, reducedWeightMultiplier));
        }
    }

    private record FishRule(
            String itemId,
            int baseWeight,
            Set<SeasonSupport.SeasonWindow> catchSeasons,
            float inSeasonWeightMultiplier,
            float reducedWeightMultiplier
    ) {
        private double getWeight(SeasonSupport.SeasonWindow currentSeason) {
            if (catchSeasons.contains(currentSeason)) {
                return baseWeight * inSeasonWeightMultiplier;
            }

            return baseWeight * reducedWeightMultiplier;
        }
    }

    private record ResolvedFishRule(Item item, FishRule rule) {
    }
}

