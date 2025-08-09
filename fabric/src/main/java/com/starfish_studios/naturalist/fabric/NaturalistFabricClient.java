package com.starfish_studios.naturalist.fabric;

import com.starfish_studios.naturalist.NaturalistClient;
import com.starfish_studios.naturalist.NaturalistConfig;
// ZebraModel and ZebraRenderer imports removed
import com.starfish_studios.naturalist.core.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.core.registry.NaturalistRegistry;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.resources.ResourceLocation;

public class NaturalistFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NaturalistClient.init();
        
        // Initialize MidnightConfig on client side for options screen integration
        MidnightConfig.init("naturalist", NaturalistConfig.class);
        
        registerEntityRenders();

        ItemProperties.register(NaturalistRegistry.BUTTERFLY.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "variant"), (stack, world, entity, num) -> {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag compoundTag = customData.copyTag();
            if (compoundTag.contains("Variant")) {
                return (float)compoundTag.getInt("Variant") / 5;
            }
            return 0.2F;
        });

        ItemProperties.register(NaturalistRegistry.SNAIL_BUCKET.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "color"), (stack, world, entity, num) -> {
            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag compoundTag = customData.copyTag();
            if (compoundTag.contains("Color")) {
                return (float)compoundTag.getInt("Color") / 15;
            }
            return 0.8F;
        });

        /* ItemProperties.register(NaturalistRegistry.MOTH.get(), new ResourceLocation("variant"), (stack, world, entity, num) -> {
            CompoundTag compoundTag = stack.getTag();
            if (compoundTag != null && compoundTag.contains("Variant")) {
                return (float)compoundTag.getInt("Variant") / 2;
            }
            return 0;
        });
         */

    }

    private void registerEntityRenders() {
        // DUCK_EGG renderer removed
    }
}
