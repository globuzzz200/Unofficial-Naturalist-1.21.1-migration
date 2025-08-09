package com.starfish_studios.naturalist.core.registry;

import com.starfish_studios.naturalist.Naturalist;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class NaturalistTags {
    public static class BlockTags {
        public static final TagKey<Block> FIREFLIES_SPAWNABLE_ON = tag("fireflies_spawnable_on");
        public static final TagKey<Block> DRAGONFLIES_SPAWNABLE_ON = tag("dragonflies_spawnable_on");
        public static final TagKey<Block> BUTTERFLIES_SPAWNABLE_ON = tag("butterflies_spawnable_on");
        public static final TagKey<Block> VULTURES_SPAWNABLE_ON = tag("vultures_spawnable_on");
        public static final TagKey<Block> DUCKS_SPAWNABLE_ON = tag("ducks_spawnable_on");
        public static final TagKey<Block> RHINO_CHARGE_BREAKABLE = tag("rhino_charge_breakable");
        public static final TagKey<Block> VULTURE_PERCH_BLOCKS = tag("vulture_perch_blocks");
        public static final TagKey<Block> CATTAIL_PLACEABLE = tag("cattail_placeable");
        public static final TagKey<Block> ALLIGATOR_EGG_LAYABLE_ON = tag("alligator_egg_layable_on");
        public static final TagKey<Block> TORTOISE_EGG_LAYABLE_ON = tag("tortoise_egg_layable_on");


        private static TagKey<Block> tag(@NotNull String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, name));
        }
    }

    public static class ItemTags {
        // All animal food tags removed - converted to hardcoded ingredients in entity classes
        
        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, name));
        }
    }

    public static class EntityTypes {
        // SAFE_EGG_WALKERS - converted to hardcoded in AlligatorEggBlock and TortoiseEggBlock
        // ANIMAL_CRATE_BLACKLISTED - converted to hardcoded (empty) in AnimalCrateBlockItem
        // BEAR_HOSTILES - converted to hardcoded in Bear.java
        // SNAKE_HOSTILES - converted to hardcoded in Snake.java
        // DEER_PREDATORS - converted to hardcoded in Deer.java
        // LION_HOSTILES - converted to hardcoded in Lion.java
        // VULTURE_HOSTILES - converted to hardcoded in Vulture.java
        // CATFISH_HOSTILES - converted to hardcoded in Catfish.java
        // ALLIGATOR_HOSTILES - converted to hardcoded in Alligator.java
        // BOAR_HOSTILES - not used in code
        
        public static final TagKey<EntityType<?>> OSTRICH_PREDATORS = tag("ostrich_predators");
        public static final TagKey<EntityType<?>> NATURALIST_ENTITIES = tag("naturalist_entities");

        private static @NotNull TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, name));
        }
    }

    public static class Biomes {
        public static final TagKey<Biome> HAS_ALLIGATOR = tag("has_alligator");
        public static final TagKey<Biome> HAS_BASS = tag("has_bass");
        public static final TagKey<Biome> HAS_BEAR = tag("has_bear");
        public static final TagKey<Biome> HAS_BLUEJAY = tag("has_bluejay");
        public static final TagKey<Biome> HAS_BOAR = tag("has_boar");
        public static final TagKey<Biome> HAS_BUTTERFLY = tag("has_butterfly");
        public static final TagKey<Biome> HAS_CANARY = tag("has_canary");
        public static final TagKey<Biome> HAS_CARDINAL = tag("has_cardinal");
        public static final TagKey<Biome> HAS_CATFISH = tag("has_catfish");
        public static final TagKey<Biome> HAS_CORAL_SNAKE = tag("has_coral_snake");
        public static final TagKey<Biome> HAS_DEER = tag("has_deer");
        public static final TagKey<Biome> HAS_DRAGONFLY = tag("has_dragonfly");
        public static final TagKey<Biome> HAS_DUCK = tag("has_duck");
        public static final TagKey<Biome> HAS_ELEPHANT = tag("has_elephant");
        public static final TagKey<Biome> HAS_FINCH = tag("has_finch");
        public static final TagKey<Biome> HAS_FIREFLY = tag("has_firefly");
        public static final TagKey<Biome> HAS_GIRAFFE = tag("has_giraffe");
        public static final TagKey<Biome> HAS_HIPPO = tag("has_hippo");
        public static final TagKey<Biome> HAS_LION = tag("has_lion");
        public static final TagKey<Biome> HAS_LIZARD = tag("has_lizard");
        public static final TagKey<Biome> HAS_RATTLESNAKE = tag("has_rattlesnake");
        public static final TagKey<Biome> HAS_RHINO = tag("has_rhino");
        public static final TagKey<Biome> HAS_ROBIN = tag("has_robin");
        public static final TagKey<Biome> HAS_SNAIL = tag("has_snail");
        public static final TagKey<Biome> HAS_SNAKE = tag("has_snake");
        public static final TagKey<Biome> HAS_SPARROW = tag("has_sparrow");
        public static final TagKey<Biome> HAS_TORTOISE = tag("has_tortoise");
        public static final TagKey<Biome> HAS_VULTURE = tag("has_vulture");
        public static final TagKey<Biome> HAS_ZEBRA = tag("has_zebra");

        public static final TagKey<Biome> BLACKLIST_ALLIGATOR = tag("blacklist/blacklist_alligator");
        public static final TagKey<Biome> BLACKLIST_BASS = tag("blacklist/blacklist_bass");
        public static final TagKey<Biome> BLACKLIST_BEAR = tag("blacklist/blacklist_bear");
        public static final TagKey<Biome> BLACKLIST_BLUEJAY = tag("blacklist/blacklist_bluejay");
        public static final TagKey<Biome> BLACKLIST_BOAR = tag("blacklist/blacklist_boar");
        public static final TagKey<Biome> BLACKLIST_BUTTERFLY = tag("blacklist/blacklist_butterfly");
        public static final TagKey<Biome> BLACKLIST_CANARY = tag("blacklist/blacklist_canary");
        public static final TagKey<Biome> BLACKLIST_CARDINAL = tag("blacklist/blacklist_cardinal");
        public static final TagKey<Biome> BLACKLIST_CATFISH = tag("blacklist/blacklist_catfish");
        public static final TagKey<Biome> BLACKLIST_CORAL_SNAKE = tag("blacklist/blacklist_coral_snake");
        public static final TagKey<Biome> BLACKLIST_DEER = tag("blacklist/blacklist_deer");
        public static final TagKey<Biome> BLACKLIST_DRAGONFLY = tag("blacklist/blacklist_dragonfly");
        public static final TagKey<Biome> BLACKLIST_DUCK = tag("blacklist/blacklist_duck");
        public static final TagKey<Biome> BLACKLIST_ELEPHANT = tag("blacklist/blacklist_elephant");
        public static final TagKey<Biome> BLACKLIST_FINCH = tag("blacklist/blacklist_finch");
        public static final TagKey<Biome> BLACKLIST_FIREFLY = tag("blacklist/blacklist_firefly");
        public static final TagKey<Biome> BLACKLIST_GIRAFFE = tag("blacklist/blacklist_giraffe");
        public static final TagKey<Biome> BLACKLIST_HIPPO = tag("blacklist/blacklist_hippo");
        public static final TagKey<Biome> BLACKLIST_LION = tag("blacklist/blacklist_lion");
        public static final TagKey<Biome> BLACKLIST_LIZARD = tag("blacklist/blacklist_lizard");
        public static final TagKey<Biome> BLACKLIST_RATTLESNAKE = tag("blacklist/blacklist_rattlesnake");
        public static final TagKey<Biome> BLACKLIST_RHINO = tag("blacklist/blacklist_rhino");
        public static final TagKey<Biome> BLACKLIST_ROBIN = tag("blacklist/blacklist_robin");
        public static final TagKey<Biome> BLACKLIST_SNAIL = tag("blacklist/blacklist_snail");
        public static final TagKey<Biome> BLACKLIST_SNAKE = tag("blacklist/blacklist_snake");
        public static final TagKey<Biome> BLACKLIST_SPARROW = tag("blacklist/blacklist_sparrow");
        public static final TagKey<Biome> BLACKLIST_TORTOISE = tag("blacklist/blacklist_tortoise");
        public static final TagKey<Biome> BLACKLIST_VULTURE = tag("blacklist/blacklist_vulture");
        public static final TagKey<Biome> BLACKLIST_ZEBRA = tag("blacklist/blacklist_zebra");



        private static @NotNull TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, name));
        }
    }
}
