package com.y271727uy.seasoninnovation.DSL;

import com.y271727uy.seasoninnovation.SeasonSupport;
import net.minecraft.world.entity.EntityType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class SeasonBreedingDsl {
    private static final SeasonBreedingDsl RULES = createDefaults();

    private final Map<String, Set<SeasonSupport.SeasonWindow>> entityRules = new LinkedHashMap<>();
    private final Map<String, Set<SeasonSupport.SeasonWindow>> namespaceDefaults = new LinkedHashMap<>();
    private Set<SeasonSupport.SeasonWindow> fallbackSeasons = EnumSet.allOf(SeasonSupport.SeasonWindow.class);

    public static SeasonBreedingDsl rules() {
        return RULES;
    }

    public EntityRuleBuilder animal(String entityId) {
        return new EntityRuleBuilder(this, entityId, false);
    }

    public EntityRuleBuilder namespace(String namespace) {
        return new EntityRuleBuilder(this, namespace, true);
    }

    public SeasonBreedingDsl fallbackTo(SeasonSupport.SeasonWindow... seasons) {
        this.fallbackSeasons = SeasonSupport.normalizeSeasons(seasons);
        return this;
    }

    public Collection<SeasonSupport.SeasonWindow> getAllowedSeasons(EntityType<?> entityType) {
        String entityId = SeasonSupport.getEntityId(entityType);
        if (entityId == null) {
            return fallbackSeasons;
        }

        Set<SeasonSupport.SeasonWindow> explicit = entityRules.get(entityId);
        if (explicit != null) {
            return explicit;
        }

        int separator = entityId.indexOf(':');
        if (separator > 0) {
            String namespace = entityId.substring(0, separator);
            Set<SeasonSupport.SeasonWindow> namespaced = namespaceDefaults.get(namespace);
            if (namespaced != null) {
                return namespaced;
            }
        }

        return fallbackSeasons;
    }

    private SeasonBreedingDsl registerEntityRule(String entityId, Set<SeasonSupport.SeasonWindow> seasons) {
        entityRules.put(entityId, seasons);
        return this;
    }

    private SeasonBreedingDsl registerNamespaceRule(String namespace, Set<SeasonSupport.SeasonWindow> seasons) {
        namespaceDefaults.put(namespace, seasons);
        return this;
    }

    private static SeasonBreedingDsl createDefaults() {
        SeasonBreedingDsl dsl = new SeasonBreedingDsl();

        // Real-world-inspired defaults for vanilla animals.
        dsl.animal("minecraft:cow").allYear().register();
        dsl.animal("minecraft:mooshroom").allYear().register();
        dsl.animal("minecraft:pig").allYear().register();
        dsl.animal("minecraft:sheep")
                .seasons(SeasonSupport.SeasonWindow.AUTUMN, SeasonSupport.SeasonWindow.WINTER)
                .register();
        dsl.animal("minecraft:goat").seasons(SeasonSupport.SeasonWindow.AUTUMN).register();
        dsl.animal("minecraft:chicken")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:rabbit")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:horse")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:donkey")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:llama")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:camel")
                .seasons(SeasonSupport.SeasonWindow.WINTER, SeasonSupport.SeasonWindow.SPRING)
                .register();
        dsl.animal("minecraft:wolf")
                .seasons(SeasonSupport.SeasonWindow.WINTER, SeasonSupport.SeasonWindow.SPRING)
                .register();
        dsl.animal("minecraft:fox").seasons(SeasonSupport.SeasonWindow.WINTER).register();
        dsl.animal("minecraft:cat")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER, SeasonSupport.SeasonWindow.AUTUMN)
                .register();
        dsl.animal("minecraft:ocelot")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:panda").seasons(SeasonSupport.SeasonWindow.SPRING).register();
        dsl.animal("minecraft:bee")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:turtle")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:frog")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:axolotl")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:sniffer")
                .seasons(SeasonSupport.SeasonWindow.SPRING, SeasonSupport.SeasonWindow.SUMMER)
                .register();
        dsl.animal("minecraft:hoglin").allYear().register();
        dsl.animal("minecraft:strider").allYear().register();

        return dsl;
    }

    public static final class EntityRuleBuilder {
        private final SeasonBreedingDsl owner;
        private final String id;
        private final boolean namespaceRule;
        private Set<SeasonSupport.SeasonWindow> seasons = EnumSet.allOf(SeasonSupport.SeasonWindow.class);

        private EntityRuleBuilder(SeasonBreedingDsl owner, String id, boolean namespaceRule) {
            this.owner = owner;
            this.id = id;
            this.namespaceRule = namespaceRule;
        }

        public EntityRuleBuilder seasons(SeasonSupport.SeasonWindow... seasons) {
            this.seasons = SeasonSupport.normalizeSeasons(seasons);
            return this;
        }

        public EntityRuleBuilder allYear() {
            this.seasons = EnumSet.allOf(SeasonSupport.SeasonWindow.class);
            return this;
        }

        public SeasonBreedingDsl register() {
            return namespaceRule
                    ? owner.registerNamespaceRule(id, seasons)
                    : owner.registerEntityRule(id, seasons);
        }
    }
}

