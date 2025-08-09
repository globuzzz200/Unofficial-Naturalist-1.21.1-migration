package com.starfish_studios.naturalist.common.entity.core.ai.goal;

import com.starfish_studios.naturalist.core.registry.NaturalistRegistry;
import com.starfish_studios.naturalist.core.registry.NaturalistSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class AttackTortoiseEggGoal extends RemoveBlockGoal {
    public AttackTortoiseEggGoal(Block block, @NotNull PathfinderMob pathfinderMob, double d, int i) {
        super(NaturalistRegistry.TORTOISE_EGG.get(), pathfinderMob, d, i);
    }
    public void playDestroyProgressSound(@NotNull LevelAccessor level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
    }

    public void playBreakSound(Level level, BlockPos pos) {
        level.playSound(null, pos, NaturalistSoundEvents.TORTOISE_EGG_BREAK.get(), SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);
    }

    public double acceptedDistance() {
        return 1.14;
    }
}
