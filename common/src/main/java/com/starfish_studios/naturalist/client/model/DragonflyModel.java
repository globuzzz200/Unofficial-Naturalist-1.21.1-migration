package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.common.entity.Dragonfly;
import net.minecraft.resources.ResourceLocation;

public class DragonflyModel extends NaturalistGeoModel<Dragonfly> {
    public static final ResourceLocation[] TEXTURE_LOCATIONS = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/dragonfly/blue.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/dragonfly/green.png"),
            ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/dragonfly/red.png")
    };

    @Override
    public ResourceLocation getModelResource(Dragonfly object) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/dragonfly.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Dragonfly object) {
        return TEXTURE_LOCATIONS[object.getVariant()];
    }

    @Override
    protected String getAnimationName(Dragonfly animatable) {
        return "dragonfly";
    }
}