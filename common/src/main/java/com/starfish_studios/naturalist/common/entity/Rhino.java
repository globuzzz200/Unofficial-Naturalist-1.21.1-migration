package com.starfish_studios.naturalist.common.entity;

import com.starfish_studios.naturalist.common.entity.core.NaturalistAnimal;
import com.starfish_studios.naturalist.common.entity.core.ai.goal.BabyHurtByTargetGoal;
import com.starfish_studios.naturalist.common.entity.core.ai.goal.BabyPanicGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import com.starfish_studios.naturalist.common.entity.core.ai.navigation.SmartBodyHelper;
import com.starfish_studios.naturalist.core.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.core.registry.NaturalistSoundEvents;
import com.starfish_studios.naturalist.core.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;import net.minecraft.world.level.ServerLevelAccessor;
import com.starfish_studios.naturalist.Naturalist;import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.starfish_studios.naturalist.common.entity.core.NaturalistGeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class Rhino extends NaturalistAnimal implements NaturalistGeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.WHEAT, Items.CARROT, Items.APPLE, Blocks.HAY_BLOCK.asItem(), Items.SWEET_BERRIES);
    private static final EntityDataAccessor<Integer> CHARGE_COOLDOWN_TICKS = SynchedEntityData.defineId(Rhino.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> HAS_TARGET = SynchedEntityData.defineId(Rhino.class, EntityDataSerializers.BOOLEAN);
    private int stunnedTick;
    private boolean canBePushed = true;
    
    protected static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.sf_nba.rhino.idle");
    protected static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.sf_nba.rhino.walk");
    protected static final RawAnimation RUN = RawAnimation.begin().thenLoop("animation.sf_nba.rhino.run");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.rhino.attack");
    protected static final RawAnimation FOOT = RawAnimation.begin().thenPlay("animation.sf_nba.rhino.foot");
    protected static final RawAnimation STUNNED = RawAnimation.begin().thenLoop("animation.sf_nba.rhino.stunned");

    public Rhino(EntityType<? extends NaturalistAnimal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0D).add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.6D).add(Attributes.FOLLOW_RANGE, 12.0D);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
        if (pSpawnData == null) {
            pSpawnData = new AgeableMobGroupData(1.0F);
        }

        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD_ITEMS.test(stack);
    }

    @Override
    protected @NotNull BodyRotationControl createBodyControl() {
        return new SmartBodyHelper(this);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "entities/rhino"));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RhinoMeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new RhinoPrepareChargeGoal(this));
        this.goalSelector.addGoal(3, new RhinoChargeGoal(this, 2.5F));
        // this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, LivingEntity.class, 8.0F, 0.8D, 1.2D, (p_213619_0_) -> p_213619_0_.getItemBySlot(EquipmentSlot.HEAD).getItem() == Items.CARVED_PUMPKIN));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.25D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(5, new BabyPanicGoal(this, 2.0D));
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new BabyHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new RhinoNearestAttackablePlayerTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, false, (p_213619_0_) -> p_213619_0_.getItemBySlot(EquipmentSlot.HEAD).getItem() == Items.CARVED_PUMPKIN));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return NaturalistEntityTypes.RHINO.get().create(serverLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CHARGE_COOLDOWN_TICKS, 0);
        builder.define(HAS_TARGET, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("StunTick", this.stunnedTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.stunnedTick = compound.getInt("StunTick");
    }

    public void setChargeCooldownTicks(int ticks) {
        this.entityData.set(CHARGE_COOLDOWN_TICKS, ticks);
    }

    public int getChargeCooldownTicks() {
        return this.entityData.get(CHARGE_COOLDOWN_TICKS);
    }

    public boolean hasChargeCooldown() {
        return this.entityData.get(CHARGE_COOLDOWN_TICKS) > 0;
    }

    public void resetChargeCooldownTicks() {
        this.entityData.set(CHARGE_COOLDOWN_TICKS, 50);
    }

    public void setHasTarget(boolean hasTarget) {
        this.entityData.set(HAS_TARGET, hasTarget);
    }

    public boolean hasTarget() {
        return this.entityData.get(HAS_TARGET);
    }

    @Override
    public int getMaxHeadYRot() {
        return this.isSprinting() ? 1 : 50;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isAlive()) {
            return;
        }
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(this.isImmobile() ? 0.0 : 0.2);
        if (this.stunnedTick > 0) {
            --this.stunnedTick;
            this.stunEffect();
        }
    }

    private void stunEffect() {
        if (this.random.nextInt(6) == 0) {
            double d = this.getX() - (double)this.getBbWidth() * Math.sin(this.yBodyRot * ((float)Math.PI / 180)) + (this.random.nextDouble() * 0.6 - 0.3);
            double e = this.getY() + (double)this.getBbHeight() - 0.3;
            double f = this.getZ() + (double)this.getBbWidth() * Math.cos(this.yBodyRot * ((float)Math.PI / 180)) + (this.random.nextDouble() * 0.6 - 0.3);
            this.level().addParticle(ParticleTypes.CRIT, d, e, f, 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || this.stunnedTick > 0;
    }

    @Override
    protected void blockedByShield(LivingEntity defender) {
        this.stunnedTick = 60;
        this.resetChargeCooldownTicks();
        this.getNavigation().stop();
        this.playSound(SoundEvents.RAVAGER_STUNNED, 1.0f, 1.0f);
        this.level().broadcastEntityEvent(this, (byte)39);
        defender.push(this);
        defender.hurtMarked = true;
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 39) {
            this.stunnedTick = 60;
        }
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        if (this.getMoveControl().hasWanted()) {
            this.setSprinting(this.getMoveControl().getSpeedModifier() >= 1.5D);
        } else {
            this.setSprinting(false);
        }
    }

    private boolean isWithinYRange(LivingEntity target) {
        if (target == null) {
            return false;
        }
        return Math.abs(target.getY() - this.getY()) < 3;
    }

    @Override
    public boolean isPushable() {
        return this.canBePushed;
    }

    @Override
    public float getVoicePitch() {
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? NaturalistSoundEvents.RHINO_AMBIENT_BABY.get() : NaturalistSoundEvents.RHINO_AMBIENT.get();
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Rhino> PlayState predicate(final AnimationState event) {
        if (this.stunnedTick > 0) {
            event.getController().setAnimation(STUNNED);
            event.getController().setAnimationSpeed(1.0F);
        } else if (event.isMoving()) {
            if (this.isSprinting()) {
                event.getController().setAnimation(RUN);
                event.getController().setAnimationSpeed(3.0F);
            } else {
                event.getController().setAnimation(WALK);
                event.getController().setAnimationSpeed(1.0F);
            }
        } else if (this.hasChargeCooldown() && this.hasTarget()) {
            event.getController().setAnimation(FOOT);
            event.getController().setAnimationSpeed(1.0F);
        } else {
            event.getController().setAnimation(IDLE);
            event.getController().setAnimationSpeed(1.0F);
        }
        return PlayState.CONTINUE;
    }

    private void soundListener(SoundKeyframeEvent<Rhino> event) {
        Rhino rhino = event.getAnimatable();
        if (rhino.level().isClientSide) {
            if (event.getKeyframeData().getSound().equals("scrape")) {
                rhino.level().playLocalSound(rhino.getX(), rhino.getY(), rhino.getZ(), NaturalistSoundEvents.RHINO_SCRAPE.get(), rhino.getSoundSource(), 1.0F, rhino.getVoicePitch(), false);
            }
        }
    }

    private <E extends Rhino> PlayState attackPredicate(final AnimationState event) {
//        if (this.swinging && event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
//            event.getController().forceAnimationReset();
//
//            event.getController().setAnimation(ATTACK);
//            event.getController().setAnimationSpeed(0.8F);
//            this.swinging = false;
//        }
//        return PlayState.CONTINUE;

        if (this.swinging && event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.setAnimation(ATTACK);
            event.getController().setAnimationSpeed(1.3F);
            event.getController().forceAnimationReset();
        }
        this.swinging = false;

        return PlayState.CONTINUE;
    }



    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        // TODO: used to be 5
        // data.setResetSpeedInTicks(5);
        AnimationController<Rhino> controller = new AnimationController<>(this, "controller", 5, this::predicate);
        controller.setSoundKeyframeHandler(this::soundListener);
        controllers.add(controller);
        controllers.add(new AnimationController<>(this, "attackController", 5, this::attackPredicate));
    }


    static class RhinoPrepareChargeGoal extends Goal {
        protected final Rhino rhino;

        public RhinoPrepareChargeGoal(Rhino rhino) {
            this.rhino = rhino;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.rhino.getTarget();
            if (target == null || !target.isAlive() || this.rhino.stunnedTick > 0 || !this.rhino.isWithinYRange(target)) {
                this.rhino.resetChargeCooldownTicks();
                return false;
            }
            return target instanceof Player && rhino.hasChargeCooldown();
        }

        @Override
        public void start() {
            LivingEntity target = this.rhino.getTarget();
            if (target == null) {
                return;
            }
            this.rhino.setHasTarget(true);
            this.rhino.resetChargeCooldownTicks();
            this.rhino.canBePushed = false;
        }

        @Override
        public void stop() {
            this.rhino.setHasTarget(false);
            this.rhino.canBePushed = true;
        }

        @Override
        public void tick() {
            LivingEntity target = this.rhino.getTarget();
            if (target == null) {
                return;
            }
            this.rhino.getLookControl().setLookAt(target);
            this.rhino.setChargeCooldownTicks(Math.max(0, this.rhino.getChargeCooldownTicks() - 1));
        }
    }

    static class RhinoChargeGoal extends Goal {
        protected final Rhino mob;
        private final double speedModifier;
        private @Nullable Path path;
        private Vec3 chargeDirection;

        public RhinoChargeGoal(Rhino pathfinderMob, double speedModifier) {
            this.mob = pathfinderMob;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            this.chargeDirection = Vec3.ZERO;
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive() || this.mob.hasChargeCooldown() || this.mob.stunnedTick > 0) {
                return false;
            }
            this.path = this.mob.getNavigation().createPath(target, 0);
            return target instanceof Player && this.path != null;
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            if (target == null || !target.isAlive() || this.mob.hasChargeCooldown() || this.mob.stunnedTick > 0) {
                return false;
            }
            return !this.mob.getNavigation().isDone();
        }

        @Override
        public void start() {
            BlockPos blockPosition = this.mob.blockPosition();
            BlockPos target = this.path.getTarget();
            this.chargeDirection = new Vec3(blockPosition.getX() - target.getX(), 0.0, blockPosition.getZ() - target.getZ()).normalize();
            this.mob.getNavigation().moveTo(this.path, this.speedModifier);
            this.mob.setAggressive(true);
        }

        @Override
        public void stop() {
            this.mob.resetChargeCooldownTicks();
            this.mob.getNavigation().stop();

            this.mob.swinging = false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.mob.getLookControl().setLookAt(Vec3.atCenterOf(this.path.getTarget()));
            if (this.mob.horizontalCollision && this.mob.onGround()) {
                this.mob.jumpFromGround();
            }
            if (this.mob.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                AABB boundingBox = this.mob.getBoundingBox().inflate(0.2);
                for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(boundingBox.minX), Mth.floor(boundingBox.minY), Mth.floor(boundingBox.minZ), Mth.floor(boundingBox.maxX), Mth.floor(boundingBox.maxY), Mth.floor(boundingBox.maxZ))) {
                    BlockState state = this.mob.level().getBlockState(pos);
                    if (!state.is(NaturalistTags.BlockTags.RHINO_CHARGE_BREAKABLE)) continue;
                    this.mob.level().destroyBlock(pos, true, this.mob);
                }
            }
            if (!this.mob.level().isClientSide()) {
                ((ServerLevel) this.mob.level()).sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, this.mob.getX(), this.mob.getY(), this.mob.getZ(), 5, this.mob.getBbWidth() / 4.0F, 0, this.mob.getBbWidth() / 4.0F, 0.01D);
            }
            if (this.mob.level().getGameTime() % 2L == 0L) {
                this.mob.playSound(SoundEvents.HOGLIN_STEP, 0.5F, this.mob.getVoicePitch());
            }
            this.tryToHurt();
        }

        protected void tryToHurt() {
            List<LivingEntity> nearbyEntities = this.mob.level().getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat(), this.mob, this.mob.getBoundingBox());
            if (!nearbyEntities.isEmpty()) {
                LivingEntity livingEntity = nearbyEntities.get(0);
                if (!(livingEntity instanceof Rhino)) {
                    livingEntity.hurt(livingEntity.damageSources().mobAttack(this.mob), (float) this.mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    float speed = Mth.clamp(this.mob.getSpeed() * 1.65f, 0.2f, 3.0f);
                    float shieldBlockModifier = livingEntity.isDamageSourceBlocked(livingEntity.damageSources().mobAttack(this.mob)) ? 0.5f : 1.0f;
                    livingEntity.knockback(shieldBlockModifier * speed * 2.0D, this.chargeDirection.x(), this.chargeDirection.z());
                    double knockbackResistance = Math.max(0.0, 1.0 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(0.0, 0.4f * knockbackResistance, 0.0));
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    if (livingEntity.equals(this.mob.getTarget())) {
                        this.stop();
                    }
                }
            }
        }
    }

    static class RhinoNearestAttackablePlayerTargetGoal extends NearestAttackableTargetGoal<Player> {
        private final Rhino rhino;

        public RhinoNearestAttackablePlayerTargetGoal(Rhino mob) {
            super(mob, Player.class, 10, true, true, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test);
            this.rhino = mob;
        }

        @Override
        public boolean canUse() {
            if (this.rhino.isBaby()) {
                return false;
            }
            if (super.canUse()) {
                if (!rhino.isWithinYRange(target)) {
                    return false;
                }
                List<Rhino> nearbyEntities = this.rhino.level().getEntitiesOfClass(Rhino.class, this.rhino.getBoundingBox().inflate(8.0, 4.0, 8.0));
                for (Rhino mob : nearbyEntities) {
                    if (!mob.isBaby()) continue;
                    return true;
                }
            }
            return false;
        }
    }

    static class RhinoMeleeAttackGoal extends MeleeAttackGoal {
        public RhinoMeleeAttackGoal(PathfinderMob pathfinderMob, double speedModifier, boolean followEvenIfNotSeen) {
            super(pathfinderMob, speedModifier, followEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof Player) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public void stop() {
            super.stop();
            this.mob.swinging = false;
        }
    }
}
