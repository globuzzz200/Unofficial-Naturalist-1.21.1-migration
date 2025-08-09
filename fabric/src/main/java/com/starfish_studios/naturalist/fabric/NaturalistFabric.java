package com.starfish_studios.naturalist.fabric;

import com.google.common.base.Preconditions;
import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.NaturalistConfig;
import com.starfish_studios.naturalist.common.entity.*;
import com.starfish_studios.naturalist.core.registry.fabric.NaturalistCreativeModeFabric;
import com.starfish_studios.naturalist.core.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.core.registry.NaturalistTags;
import com.starfish_studios.naturalist.core.registry.NaturalistIngredientRefresh;
import eu.midnightdust.lib.config.MidnightConfig;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NaturalistFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Naturalist.init();

        MidnightConfig.init("naturalist", NaturalistConfig.class);

        addSpawns();
        registerEntityAttributes();
        Naturalist.registerBrewingRecipes();
        Naturalist.registerCompostables();
        Naturalist.registerSpawnPlacements();
        Naturalist.registerDispenserBehaviors();

        NaturalistCreativeModeFabric.init();
        
        // Register ingredient refresh listener for tag reloads
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "ingredient_refresh");
            }
            
            @Override
            public void onResourceManagerReload(ResourceManager resourceManager) {
                NaturalistIngredientRefresh.refreshAllIngredients();
            }
        });
        
        // Also refresh ingredients after server starts (when tags are loaded)
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
            NaturalistIngredientRefresh.refreshAllIngredients();
        });
    }

    void registerEntityAttributes() {
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.SNAIL.get(), Snail.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.BEAR.get(), Bear.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.BUTTERFLY.get(), Butterfly.createAttributes());
        // FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.MOTH.get(), Moth.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.FIREFLY.get(), Firefly.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.SNAKE.get(), Snake.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CORAL_SNAKE.get(), Snake.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.RATTLESNAKE.get(), Snake.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.DEER.get(), Deer.createAttributes());


        // BIRDS

        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.BLUEJAY.get(), Bird.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CANARY.get(), Bird.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CARDINAL.get(), Bird.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.ROBIN.get(), Bird.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.FINCH.get(), Bird.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.SPARROW.get(), Bird.createAttributes());


        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CATERPILLAR.get(), Caterpillar.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.RHINO.get(), Rhino.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.LION.get(), Lion.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.ELEPHANT.get(), Elephant.createAttributes());
        // ZEBRA removed
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.GIRAFFE.get(), Giraffe.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.HIPPO.get(), Hippo.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.VULTURE.get(), Vulture.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.BOAR.get(), Boar.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.DRAGONFLY.get(), Dragonfly.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CATFISH.get(), Catfish.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.ALLIGATOR.get(), Alligator.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.BASS.get(), AbstractFish.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.LIZARD.get(), Lizard.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.LIZARD_TAIL.get(), LizardTail.createAttributes());
        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.TORTOISE.get(), Tortoise.createAttributes());
        // DUCK attributes removed

//        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.TOUCAN.get(), Toucan.createAttributes());
//        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CRAB.get(), Crab.createAttributes());
//        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.MOOSE.get(), Moose.createAttributes());
//        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.CAPYBARA.get(), Capybara.createAttributes());
//        FabricDefaultAttributeRegistry.register(NaturalistEntityTypes.EMPEROR_PENGUIN.get(), EmperorPenguin.createAttributes());
    }

    void addSpawns() {
        addMobSpawn(NaturalistTags.Biomes.HAS_BEAR, MobCategory.CREATURE, NaturalistEntityTypes.BEAR.get(), NaturalistConfig.bearSpawnWeight, 1, 2);
        addMobSpawn(NaturalistTags.Biomes.HAS_DEER, MobCategory.CREATURE, NaturalistEntityTypes.DEER.get(), NaturalistConfig.deerSpawnWeight, 1, 3);
        addMobSpawn(NaturalistTags.Biomes.HAS_SNAIL, MobCategory.CREATURE, NaturalistEntityTypes.SNAIL.get(), NaturalistConfig.snailSpawnWeight, 1, 3);

        addMobSpawn(NaturalistTags.Biomes.HAS_FIREFLY, MobCategory.AMBIENT, NaturalistEntityTypes.FIREFLY.get(), NaturalistConfig.fireflySpawnWeight, 2, 3);


        addMobSpawn(NaturalistTags.Biomes.HAS_BUTTERFLY, MobCategory.AMBIENT, NaturalistEntityTypes.BUTTERFLY.get(), NaturalistConfig.butterflySpawnWeight, 3, 5);
        addMobSpawn(NaturalistTags.Biomes.HAS_SNAKE, MobCategory.CREATURE, NaturalistEntityTypes.SNAKE.get(), NaturalistConfig.snakeSpawnWeight, 1, 1);
        addMobSpawn(NaturalistTags.Biomes.HAS_RATTLESNAKE, MobCategory.CREATURE, NaturalistEntityTypes.RATTLESNAKE.get(), NaturalistConfig.rattlesnakeSpawnWeight, 1, 1);
        addMobSpawn(NaturalistTags.Biomes.HAS_CORAL_SNAKE, MobCategory.CREATURE, NaturalistEntityTypes.CORAL_SNAKE.get(), NaturalistConfig.coralSnakeSpawnWeight, 1, 1);


        addMobSpawn(NaturalistTags.Biomes.HAS_BLUEJAY, MobCategory.CREATURE, NaturalistEntityTypes.BLUEJAY.get(), NaturalistConfig.bluejaySpawnWeight, 1, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_CANARY, MobCategory.CREATURE, NaturalistEntityTypes.CANARY.get(), NaturalistConfig.canarySpawnWeight, 1, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_CARDINAL, MobCategory.CREATURE, NaturalistEntityTypes.CARDINAL.get(), NaturalistConfig.cardinalSpawnWeight, 1, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_ROBIN, MobCategory.CREATURE, NaturalistEntityTypes.ROBIN.get(), NaturalistConfig.robinSpawnWeight, 1, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_FINCH, MobCategory.CREATURE, NaturalistEntityTypes.FINCH.get(), NaturalistConfig.finchSpawnWeight, 1, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_SPARROW, MobCategory.CREATURE, NaturalistEntityTypes.SPARROW.get(), NaturalistConfig.sparrowSpawnWeight, 1, 4);

        addMobSpawn(BiomeTags.IS_FOREST, MobCategory.CREATURE, EntityType.RABBIT, NaturalistConfig.forestRabbitSpawnWeight, 2, 3);
        addMobSpawn(BiomeTags.IS_FOREST, MobCategory.CREATURE, EntityType.FOX, NaturalistConfig.forestFoxSpawnWeight, 2, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_RHINO, MobCategory.CREATURE, NaturalistEntityTypes.RHINO.get(), NaturalistConfig.rhinoSpawnWeight, 1, 3);
        addMobSpawn(NaturalistTags.Biomes.HAS_LION, MobCategory.CREATURE, NaturalistEntityTypes.LION.get(), NaturalistConfig.lionSpawnWeight, 3, 5);
        addMobSpawn(NaturalistTags.Biomes.HAS_ELEPHANT, MobCategory.CREATURE, NaturalistEntityTypes.ELEPHANT.get(), NaturalistConfig.elephantSpawnWeight, 2, 3);
        // ZEBRA spawn removed
        addMobSpawn(NaturalistTags.Biomes.HAS_GIRAFFE, MobCategory.CREATURE, NaturalistEntityTypes.GIRAFFE.get(), NaturalistConfig.giraffeSpawnWeight, 2, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_HIPPO, MobCategory.CREATURE, NaturalistEntityTypes.HIPPO.get(), NaturalistConfig.hippoSpawnWeight, 3, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_VULTURE, MobCategory.CREATURE, NaturalistEntityTypes.VULTURE.get(), NaturalistConfig.vultureSpawnWeight, 2, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_BOAR, MobCategory.CREATURE, NaturalistEntityTypes.BOAR.get(), NaturalistConfig.boarSpawnWeight, 4, 4);

        addMobSpawn(NaturalistTags.Biomes.HAS_DRAGONFLY, MobCategory.AMBIENT, NaturalistEntityTypes.DRAGONFLY.get(), NaturalistConfig.dragonflySpawnWeight, 1, 3);
        addMobSpawn(NaturalistTags.Biomes.HAS_CATFISH, MobCategory.WATER_AMBIENT, NaturalistEntityTypes.CATFISH.get(), NaturalistConfig.catfishSpawnWeight, 1, 1);
        addMobSpawn(NaturalistTags.Biomes.HAS_ALLIGATOR, MobCategory.CREATURE, NaturalistEntityTypes.ALLIGATOR.get(), NaturalistConfig.alligatorSpawnWeight, 1, 2);
        addMobSpawn(NaturalistTags.Biomes.HAS_BASS, MobCategory.WATER_AMBIENT, NaturalistEntityTypes.BASS.get(), NaturalistConfig.bassSpawnWeight, 4, 4);
        addMobSpawn(NaturalistTags.Biomes.HAS_LIZARD, MobCategory.CREATURE, NaturalistEntityTypes.LIZARD.get(), NaturalistConfig.lizardSpawnWeight, 1, 2);
        addMobSpawn(NaturalistTags.Biomes.HAS_TORTOISE, MobCategory.CREATURE, NaturalistEntityTypes.TORTOISE.get(), NaturalistConfig.tortoiseSpawnWeight, 1, 3);
        // DUCK spawn removed
//        if (NaturalistConfig.spawnFarmAnimalsInSavannas) {
//            removeSpawn(BiomeTags.IS_SAVANNA, List.of(EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
//        }
//        if (NaturalistConfig.spawnFarmAnimalsInSwamps) {
//            removeSpawn(ConventionalBiomeTags.SWAMP, List.of(EntityType.SHEEP, EntityType.PIG, EntityType.CHICKEN, EntityType.COW));
//        }
//        if (NaturalistConfig.removeForestPigs) {
//            removeSpawn(ConventionalBiomeTags.FOREST, List.of(EntityType.PIG));
//        }
    }

    void addMobSpawn(TagKey<Biome> tag, MobCategory mobCategory, EntityType<?> entityType, int weight, int minGroupSize, int maxGroupSize) {
        BiomeModifications.addSpawn(biomeSelector -> biomeSelector.hasTag(tag), mobCategory, entityType, weight, minGroupSize, maxGroupSize);
    }

    void removeSpawn(TagKey<Biome> tag, List<EntityType<?>> entityTypes) {
        entityTypes.forEach(entityType -> {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            Preconditions.checkState(BuiltInRegistries.ENTITY_TYPE.containsKey(id), "Unregistered entity type: %s", entityType);
            BiomeModifications.create(id).add(ModificationPhase.REMOVALS, biomeSelector -> biomeSelector.hasTag(tag), context -> context.getSpawnSettings().removeSpawnsOfEntityType(entityType));
        });
        
    }
}
