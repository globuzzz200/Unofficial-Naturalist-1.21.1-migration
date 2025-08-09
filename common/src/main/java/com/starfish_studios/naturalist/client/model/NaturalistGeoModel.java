package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.model.GeoModel;

import java.util.Optional;

/**
 * Base GeoModel class that supports flexible animation file loading.
 * Tries both .animation.json and .rp_anim.json formats for backward compatibility.
 */
public abstract class NaturalistGeoModel<T extends GeoAnimatable> extends GeoModel<T> {
    
    /**
     * Gets the base animation name for this entity (without file extension)
     * @param animatable The entity instance
     * @return The base name for animation files (e.g., "alligator")
     */
    protected abstract String getAnimationName(T animatable);
    
    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        String animationName = getAnimationName(animatable);
        
        // Try .animation.json first (new format)
        ResourceLocation animationJson = ResourceLocation.fromNamespaceAndPath(
            Naturalist.MOD_ID, "animations/" + animationName + ".animation.json"
        );
        
        if (resourceExists(animationJson)) {
            return animationJson;
        }
        
        // Fallback to .rp_anim.json (old format)
        ResourceLocation rpAnimJson = ResourceLocation.fromNamespaceAndPath(
            Naturalist.MOD_ID, "animations/" + animationName + ".rp_anim.json"
        );
        
        if (resourceExists(rpAnimJson)) {
            return rpAnimJson;
        }
        
        // If neither exists, return the preferred format for error handling
        return animationJson;
    }
    
    /**
     * Check if a resource exists in the resource pack
     */
    private boolean resourceExists(ResourceLocation location) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
            return resource.isPresent();
        } catch (Exception e) {
            return false;
        }
    }
}