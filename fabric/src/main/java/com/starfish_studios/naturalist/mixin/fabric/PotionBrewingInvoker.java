package com.starfish_studios.naturalist.mixin.fabric;

import net.minecraft.world.item.Item;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PotionBrewing.class)
public interface PotionBrewingInvoker {
    // @Invoker("addMix") - Method may have been renamed or removed in this Minecraft version
    // static void invokeAddMix(Holder<Potion> potionEntry, Item potionIngredient, Holder<Potion> potionResult) {
    //     throw new AssertionError();
    // }
}
