package com.starfish_studios.naturalist.common.block;

import com.starfish_studios.naturalist.common.entity.*;
import com.starfish_studios.naturalist.core.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.core.registry.NaturalistSoundEvents;
import com.starfish_studios.naturalist.core.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;

public class AlligatorEggBlock extends TurtleEggBlock {
    public AlligatorEggBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.shouldUpdateHatchLevel(level)) {
            int i = state.getValue(HATCH);
            if (i < 2) {
                level.playSound(null, pos, NaturalistSoundEvents.GATOR_EGG_CRACK.get(), SoundSource.BLOCKS, 0.7f, 0.9f + random.nextFloat() * 0.2f);
                level.setBlock(pos, state.setValue(HATCH, i + 1), 2);
            } else {
                level.playSound(null, pos, NaturalistSoundEvents.GATOR_EGG_HATCH.get(), SoundSource.BLOCKS, 0.7f, 0.9f + random.nextFloat() * 0.2f);
                level.removeBlock(pos, false);
                for (int j = 0; j < state.getValue(EGGS); ++j) {
                    level.levelEvent(2001, pos, Block.getId(state));
                    Alligator alligator = NaturalistEntityTypes.ALLIGATOR.get().create(level);
                    alligator.setAge(-24000);
                    alligator.moveTo((double)pos.getX() + 0.3 + (double)j * 0.2, pos.getY(), (double)pos.getZ() + 0.3, 0.0f, 0.0f);
                    level.addFreshEntity(alligator);
                }
            }
        }
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            level.levelEvent(2005, pos, 0);
        }
    }

    private boolean shouldUpdateHatchLevel(Level level) {
        return level.random.nextInt(500) == 0;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, @NotNull Entity entity) {
        if (!entity.isSteppingCarefully()) {
            this.destroyEgg(level, state, pos, entity, 100);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!(entity instanceof Zombie)) {
            this.destroyEgg(level, state, pos, entity, 3);
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    private void destroyEgg(@NotNull Level level, BlockState state, @NotNull BlockPos pos, Entity entity, int chance) {
        if (!this.canDestroyEgg(level, entity)) {
            return;
        }
        if (!level.isClientSide && level.random.nextInt(chance) == 0) {
            this.decreaseEggs(level, pos, state);
        }
    }

    private boolean canDestroyEgg(Level level, Entity entity) {
        if (!(entity instanceof Alligator) && !isSafeEggWalker(entity)) {
            if (!(entity instanceof LivingEntity)) {
                return false;
            } else {
                return entity instanceof Player || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            }
        } else {
            return false;
        }
    }

    private boolean isSafeEggWalker(Entity entity) {
        EntityType<?> entityType = entity.getType();
        return entityType == NaturalistEntityTypes.BUTTERFLY.get() ||
               entityType == NaturalistEntityTypes.CARDINAL.get() ||
               entityType == NaturalistEntityTypes.BLUEJAY.get() ||
               entityType == NaturalistEntityTypes.SPARROW.get() ||
               entityType == NaturalistEntityTypes.FINCH.get() ||
               entityType == NaturalistEntityTypes.ROBIN.get() ||
               entityType == NaturalistEntityTypes.CANARY.get() ||
               entityType == NaturalistEntityTypes.DRAGONFLY.get() ||
               entityType == NaturalistEntityTypes.FIREFLY.get() ||
               entityType == EntityType.BAT;
        // Note: alexsmobs:shoebill excluded as it's optional and not guaranteed to exist
    }

    private void decreaseEggs(@NotNull Level level, BlockPos pos, BlockState state) {
        level.playSound(null, pos, NaturalistSoundEvents.GATOR_EGG_BREAK.get(), SoundSource.BLOCKS, 0.7f, 0.9f + level.random.nextFloat() * 0.2f);
        int i = state.getValue(EGGS);
        if (i <= 1) {
            level.destroyBlock(pos, false);
        } else {
            level.setBlock(pos, state.setValue(EGGS, i - 1), 2);
            level.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(state));
            level.levelEvent(2001, pos, Block.getId(state));
        }
    }
}
