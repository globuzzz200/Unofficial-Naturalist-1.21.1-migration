package com.starfish_studios.naturalist.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.starfish_studios.naturalist.core.registry.NaturalistRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record BugNetInteractionRecipe(ResourceLocation id, EntityType<?> entityType, ItemStack dropStack) implements Recipe<RecipeInput> {

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider provider) {
        return dropStack;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return dropStack;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NaturalistRecipes.BUG_NET_SERIALIZER;
    }

    @Override
    public RecipeType<?> getType() {
        return NaturalistRecipes.BUG_NET;
    }

    public static class Serializer implements RecipeSerializer<BugNetInteractionRecipe> {
        
        @Override
        public MapCodec<BugNetInteractionRecipe> codec() {
            return RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(BugNetInteractionRecipe::id),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(BugNetInteractionRecipe::entityType),
                ItemStack.CODEC.fieldOf("drop_stack").forGetter(BugNetInteractionRecipe::dropStack)
            ).apply(instance, BugNetInteractionRecipe::new));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, BugNetInteractionRecipe> streamCodec() {
            return StreamCodec.of(
                (buffer, recipe) -> toNetwork(buffer, recipe),
                (buffer) -> fromNetwork(null, buffer)
            );
        }
        
        public BugNetInteractionRecipe fromNetwork(ResourceLocation recipeId, RegistryFriendlyByteBuf buffer) {
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(buffer.readResourceLocation());
            return new BugNetInteractionRecipe(recipeId, entityType, output);
        }

        public void toNetwork(RegistryFriendlyByteBuf buffer, BugNetInteractionRecipe recipe) {
            ItemStack.STREAM_CODEC.encode(buffer, recipe.dropStack);
            buffer.writeResourceLocation(BuiltInRegistries.ENTITY_TYPE.getKey(recipe.entityType));
        }
    }
}