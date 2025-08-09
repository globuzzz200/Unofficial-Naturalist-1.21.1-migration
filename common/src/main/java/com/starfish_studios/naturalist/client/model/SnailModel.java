package com.starfish_studios.naturalist.client.model;

import com.starfish_studios.naturalist.Naturalist;
import com.starfish_studios.naturalist.common.entity.Alligator;
import com.starfish_studios.naturalist.common.entity.Snail;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.model.data.EntityModelData;

@Environment(EnvType.CLIENT)
public class SnailModel extends NaturalistGeoModel<Snail> {
    @Override
    @SuppressWarnings("removal")
    public ResourceLocation getModelResource(Snail snail) {
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "geo/entity/snail.geo.json");
    }

    @Override
    @SuppressWarnings("removal")
    public @NotNull ResourceLocation getTextureResource(@NotNull Snail snail) {
        if (snail.getName().getString().contains("Gary")) {
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snail/gary.png");
        } else if (snail.getSnailColor() != null) {
            int color = snail.getSnailColor().getId();
            return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snail/" + DyeColor.byId(color).getName() + ".png");
        }
        return ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "textures/entity/snail.png");
    }

    @Override
    protected String getAnimationName(Snail snail) {
        return "snail";
    }

    @Override
    public void setCustomAnimations(Snail animatable, long instanceId, @Nullable AnimationState animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        if (animationState == null) return;

        EntityModelData extraDataOfType = (EntityModelData) animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        GeoBone leftEye = this.getAnimationProcessor().getBone("left_eye");
        GeoBone rightEye = this.getAnimationProcessor().getBone("right_eye");
        GeoBone eyes = this.getAnimationProcessor().getBone("eyes");

        if (animatable.isBaby()) {
            eyes.setScaleX(1.5F);
            eyes.setScaleY(1.5F);
            eyes.setScaleZ(1.5F);
        } else {
            eyes.setScaleX(1.0F);
            eyes.setScaleY(1.0F);
            eyes.setScaleZ(1.0F);
        }

        if (!animatable.isClimbing() || !animatable.canHide()) {
            leftEye.setRotX(extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            leftEye.setRotY(extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
            rightEye.setRotX(extraDataOfType.headPitch() * Mth.DEG_TO_RAD);
            rightEye.setRotY(extraDataOfType.netHeadYaw() * Mth.DEG_TO_RAD);
        }
    }
}
