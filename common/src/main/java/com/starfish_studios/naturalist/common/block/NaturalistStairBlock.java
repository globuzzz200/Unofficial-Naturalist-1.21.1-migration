package com.starfish_studios.naturalist.common.block;

import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NaturalistStairBlock extends StairBlock {
    public static final MapCodec<NaturalistStairBlock> CODEC = RecordCodecBuilder.mapCodec(
        (instance) -> instance.group(
            BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseState)
        ).apply(instance, NaturalistStairBlock::new)
    );

    public NaturalistStairBlock(BlockState baseState, Properties properties) {
        super(baseState, properties);
    }
    
    public NaturalistStairBlock(BlockState baseState) {
        super(baseState, Properties.ofFullCopy(baseState.getBlock()));
    }

    @Override
    public MapCodec<? extends StairBlock> codec() {
        return CODEC;
    }
}