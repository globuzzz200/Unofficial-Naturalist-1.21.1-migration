package com.starfish_studios.naturalist.mixin;

import com.starfish_studios.naturalist.common.entity.core.ai.goal.AttackAlligatorEggGoal;
import com.starfish_studios.naturalist.common.entity.core.ai.goal.AttackTortoiseEggGoal;
import com.starfish_studios.naturalist.core.registry.NaturalistRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Zombie.class)
public class ZombieMixin extends Monster {

    protected ZombieMixin(@NotNull EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "registerGoals")
    private void registerGoals(CallbackInfo info) {
        this.goalSelector.addGoal(2, new AttackAlligatorEggGoal(NaturalistRegistry.ALLIGATOR_EGG.get(), this, 1.0D, 3));
        this.goalSelector.addGoal(2, new AttackTortoiseEggGoal(NaturalistRegistry.TORTOISE_EGG.get(), this, 1.0D, 3));
    }
}
