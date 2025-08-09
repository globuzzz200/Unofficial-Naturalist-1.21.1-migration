package com.starfish_studios.naturalist.mixin;

import com.starfish_studios.naturalist.common.entity.Catfish;
import com.starfish_studios.naturalist.common.entity.Lion;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public class CreeperMixin extends Monster {
    protected CreeperMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At(value = "HEAD"), method = "registerGoals")
    public void onRegisterGoals(CallbackInfo ci) {
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Lion.class, 6.0f, 1.0, 1.2));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Catfish.class, 6.0f, 1.0, 1.2));
    }
}
