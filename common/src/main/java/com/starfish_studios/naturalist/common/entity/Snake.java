package com.starfish_studios.naturalist.common.entity;

import com.starfish_studios.naturalist.common.entity.core.ClimbingAnimal;
import com.starfish_studios.naturalist.common.entity.core.NaturalistAnimal;
import com.starfish_studios.naturalist.common.entity.core.SleepingAnimal;
import com.starfish_studios.naturalist.common.entity.core.ai.goal.SearchForItemsGoal;
import com.starfish_studios.naturalist.common.entity.core.ai.goal.SleepGoal;
import com.starfish_studios.naturalist.core.registry.NaturalistEntityTypes;
import com.starfish_studios.naturalist.core.registry.NaturalistSoundEvents;
import com.starfish_studios.naturalist.core.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Path;
import com.starfish_studios.naturalist.common.entity.core.NaturalistGeoEntity;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

public class Snake extends ClimbingAnimal implements SleepingAnimal, NeutralMob, NaturalistGeoEntity {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static Ingredient foodItems;
    private static Ingredient tameItems;
    
    private static Ingredient getFoodItems() {
        if (foodItems == null) {
            foodItems = Ingredient.of(
                net.minecraft.world.item.Items.CHICKEN,
                net.minecraft.world.item.Items.RABBIT,
                net.minecraft.world.item.Items.RABBIT_FOOT,
                net.minecraft.world.item.Items.EGG
            );
        }
        return foodItems;
    }
    
    private static Ingredient getTameItems() {
        if (tameItems == null) {
            // Use same items as food since snake_tame_items.json doesn't exist
            tameItems = Ingredient.of(
                net.minecraft.world.item.Items.CHICKEN,
                net.minecraft.world.item.Items.RABBIT,
                net.minecraft.world.item.Items.RABBIT_FOOT,
                net.minecraft.world.item.Items.EGG
            );
        }
        return tameItems;
    }
    
    public static void refreshIngredients() {
        foodItems = null;
        tameItems = null;
    }
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    private static final EntityDataAccessor<Integer> REMAINING_ANGER_TIME = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EAT_COUNTER = SynchedEntityData.defineId(Snake.class, EntityDataSerializers.INT);
    @Nullable
    private UUID persistentAngerTarget;

    protected static final RawAnimation MOVE = RawAnimation.begin().thenPlay("animation.sf_nba.snake.move");
    protected static final RawAnimation SLEEP = RawAnimation.begin().thenLoop("animation.sf_nba.snake.sleep");
    protected static final RawAnimation CLIMB = RawAnimation.begin().thenLoop("animation.sf_nba.snake.climb");
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay("animation.sf_nba.snake.attack");
    protected static final RawAnimation TONGUE = RawAnimation.begin().thenPlay("animation.sf_nba.snake.tongue");
    protected static final RawAnimation RATTLE = RawAnimation.begin().thenLoop("animation.sf_nba.snake.rattle");

    public Snake(EntityType<? extends NaturalistAnimal> entityType, Level level) {
        super(entityType, level);
        this.setCanPickUpLoot(true);
    }

    // ATTRIBUTES/GOALS/LOGIC

    public static AttributeSupplier.Builder createAttributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FOLLOW_RANGE, 20.0D).add(Attributes.MOVEMENT_SPEED, 0.18D).add(Attributes.ATTACK_DAMAGE, 6.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SnakeMeleeAttackGoal(this, 1.75D, true));
        this.goalSelector.addGoal(2, new SearchForItemsGoal(this, 1.2F, getFoodItems(), 8.0D, 8.0D));
        this.goalSelector.addGoal(3, new SleepGoal<>(this));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        // this.goalSelector.addGoal(7, new SitWhenOrderedToGoal(this));
        // this.goalSelector.addGoal(8, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 5, true, false, livingEntity -> isSnakeHostile(livingEntity) || (livingEntity instanceof Slime slime && slime.isTiny())));
        this.targetSelector.addGoal(4, new ResetUniversalAngerTargetGoal<>(this, false));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    public static boolean checkSnakeSpawnRules(EntityType<Snake> entityType, LevelAccessor level, MobSpawnType type, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos.below()).is(BlockTags.RABBITS_SPAWNABLE_ON) && isBrightEnoughToSpawn(level, pos);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
        this.populateDefaultEquipmentSlots(random, pDifficulty);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
    }

    @Override
    protected void populateDefaultEquipmentSlots(@NotNull RandomSource random, DifficultyInstance pDifficulty) {
        if (random.nextFloat() < 0.2F) {
            float chance = random.nextFloat();
            ItemStack stack;
            if (chance < 0.05F) {
                stack = new ItemStack(Items.RABBIT_FOOT);
            } else if (chance < 0.1F) {
                stack = new ItemStack(Items.SLIME_BALL);
            } else if (chance < 0.15F) {
                stack = new ItemStack(Items.FEATHER);
            } else if (chance < 0.3F) {
                stack = new ItemStack(Items.RABBIT);
            } else {
                stack = new ItemStack(Items.CHICKEN);
            }

            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
        }
    }

    @Override
    public boolean isFood(ItemStack pStack) {
        return getFoodItems().test(pStack);
    }

    public boolean isTameFood(ItemStack pStack) {
        return getTameItems().test(pStack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
        builder.define(EAT_COUNTER, 0);
        builder.define(REMAINING_ANGER_TIME, 0);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.readPersistentAngerSaveData(this.level(), pCompound);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        this.addPersistentAngerSaveData(pCompound);
    }

    public boolean isEating() {
        return this.entityData.get(EAT_COUNTER) > 0;
    }

    public void eat(boolean eat) {
        this.entityData.set(EAT_COUNTER, eat ? 1 : 0);
    }

    private int getEatCounter() {
        return this.entityData.get(EAT_COUNTER);
    }

    private void setEatCounter(int amount) {
        this.entityData.set(EAT_COUNTER, amount);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }
        this.handleEating();
        if (!this.getMainHandItem().isEmpty()) {
            if (this.isAngry()) {
                this.stopBeingAngry();
            }
        }
        if (this.canRattle() && !this.isSleeping()) {
            this.playSound(NaturalistSoundEvents.SNAKE_RATTLE.get(), 0.15F, 1.0F);
        }
    }

    private void handleEating() {
        if (!this.isEating() && !this.isSleeping() && !this.getMainHandItem().isEmpty()) {
            this.eat(true);
        } else if (this.getMainHandItem().isEmpty()) {
            this.eat(false);
        }
        if (this.isEating()) {
            if (!this.level().isClientSide && this.getEatCounter() > 6000) {
                if (!this.getMainHandItem().isEmpty()) {
                    if (!this.level().isClientSide) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.gameEvent(GameEvent.EAT);
                    }
                }
                this.eat(false);
                return;
            }
            this.setEatCounter(this.getEatCounter() + 1);
        }
    }

    // EATING

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        EquipmentSlot slot = this.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(slot).isEmpty()) {
            return false;
        } else {
            return slot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    @Override
    protected void pickUpItem(@NotNull ItemEntity pItemEntity) {
        ItemStack stack = pItemEntity.getItem();
        if (this.getMainHandItem().isEmpty() && getFoodItems().test(stack)) {
            this.onItemPickup(pItemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, stack);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(pItemEntity, stack.getCount());
            pItemEntity.discard();
        }
    }

    private boolean isSnakeHostile(LivingEntity entity) {
        EntityType<?> entityType = entity.getType();
        return entityType == EntityType.RABBIT ||
               entityType == EntityType.CHICKEN ||
               entityType == EntityType.SILVERFISH ||
               entityType == NaturalistEntityTypes.SNAIL.get();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        // Wake up if sleeping when hurt
        if (this.isSleeping()) {
            this.setSleeping(false);
        }
        
        if (!this.getMainHandItem().isEmpty() && !this.level().isClientSide) {
            ItemEntity itemEntity = new ItemEntity(this.level(), this.getX() + this.getLookAngle().x, this.getY() + 1.0D, this.getZ() + this.getLookAngle().z, this.getMainHandItem());
            itemEntity.setPickUpDelay(80);
            itemEntity.setThrower(this);
            this.playSound(SoundEvents.FOX_SPIT, 1.0F, 1.0F);
            this.level().addFreshEntity(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        return super.hurt(pSource, pAmount);
    }

    // MOVEMENT

    @Override
    protected float getClimbSpeedMultiplier() {
        return 0.5F;
    }

    @Override
    public float getSpeed() {
        return this.getMainHandItem().isEmpty() ? super.getSpeed() : super.getSpeed() * 0.5F;
    }

    // SLEEPING

    @Override
    public boolean canSleep() {
        long dayTime = this.level().getDayTime();
        if (this.isAngry() || this.level().isWaterAt(this.blockPosition())) {
            return false;
        } else if (dayTime > 18000 && dayTime < 23000) {
            return false;
        } else return dayTime > 12000 && dayTime < 28000;
    }

    @Override
    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    // ANGER

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    @Override
    public void setRemainingPersistentAngerTime(int pTime) {
        this.entityData.set(REMAINING_ANGER_TIME, pTime);
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(REMAINING_ANGER_TIME);
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID pTarget) {
        this.persistentAngerTarget = pTarget;
    }

    @Nullable
    @Override
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    // SNAKE VARIANTS

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        if ((this.getType().equals(NaturalistEntityTypes.CORAL_SNAKE.get()) || this.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get())) && pEntity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 40));
        }
        return super.doHurtTarget(pEntity);
    }

    private boolean canRattle() {
        List<Player> players = this.level().getNearbyPlayers(TargetingConditions.forNonCombat().range(4.0D), this, this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
        if(!players.isEmpty() && this.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get()) && !players.get(0).isCreative()){
            this.setTarget(players.get(0));
        } else {
            this.setTarget(null);
        }
        return !players.isEmpty() && this.getType().equals(NaturalistEntityTypes.RATTLESNAKE.get());
    }

    @Override
    protected float getSoundVolume() {
        return 0.15F;
    }

    /*
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult interactionResult;
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.level().isClientSide) {
            if (this.isTame() && this.isOwnedBy(player)) {
                return InteractionResult.SUCCESS;
            }
            if (this.isFood(itemStack) && (this.getHealth() < this.getMaxHealth() || !this.isTame())) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                    this.usePlayerItem(player, hand, itemStack);
                    this.heal(3.0F);
                    return InteractionResult.CONSUME;
                }
                InteractionResult interactionResult2 = super.mobInteract(player, hand);
                if (!interactionResult2.consumesAction() || this.isBaby()) {
                    this.setOrderedToSit(!this.isOrderedToSit());
                }
                return interactionResult2;
            }
        } else if (this.isTameFood(itemStack)) {
            this.usePlayerItem(player, hand, itemStack);
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.setOrderedToSit(true);
                this.level().broadcastEntityEvent(this, (byte)7);
            } else {
                this.level().broadcastEntityEvent(this, (byte)6);
            }
            this.setPersistenceRequired();
            return InteractionResult.CONSUME;
        }
        if ((interactionResult = super.mobInteract(player, hand)).consumesAction()) {
            this.setPersistenceRequired();
        }
        return interactionResult;
    }

     */

    // SOUNDS

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return NaturalistSoundEvents.SNAKE_HURT.get();
    }

    // ANIMATION

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Snake> @NotNull PlayState predicate(final AnimationState event) {
        if (this.isSleeping()) {
            event.getController().setAnimation(SLEEP);
            return PlayState.CONTINUE;
        } else if (this.isClimbing()) {
            event.getController().setAnimation(CLIMB);
            return PlayState.CONTINUE;
        } else if (!(event.getLimbSwingAmount() > -0.04F && event.getLimbSwingAmount() < 0.04F)) {
            event.getController().setAnimation(MOVE);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();
        
        return PlayState.STOP;
    }

    private <E extends Snake> PlayState attackPredicate(final AnimationState event) {
        if (this.swinging && event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.getController().forceAnimationReset();
        
            event.getController().setAnimation(ATTACK);
            this.swinging = false;
        }
        return PlayState.CONTINUE;
    }

    private <E extends Snake> @NotNull PlayState tonguePredicate(final AnimationState event) {
        if (this.random.nextInt(1000) < this.ambientSoundTime && !this.isSleeping() && event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.getController().forceAnimationReset();
        
            event.getController().setAnimation(TONGUE);
        }
        return PlayState.CONTINUE;
    }

    private <E extends Snake> @NotNull PlayState rattlePredicate(final AnimationState event) {
        if (this.canRattle() && !this.isSleeping()) {
            event.getController().setAnimation(RATTLE);
            return PlayState.CONTINUE;
        }
        event.getController().forceAnimationReset();
        
        return PlayState.STOP;
    }

    private void soundListener(@NotNull SoundKeyframeEvent<Snake> event) {
        Snake snake = event.getAnimatable();
        if (snake.level().isClientSide) {
            if (event.getKeyframeData().getSound().equals("hiss")) {
                snake.level().playLocalSound(snake.getX(), snake.getY(), snake.getZ(), NaturalistSoundEvents.SNAKE_HISS.get(), snake.getSoundSource(), snake.getSoundVolume(), snake.getVoicePitch(), false);
            }
        }
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 10, this::predicate));
        controllers.add(new AnimationController<>(this, "attackController", 0, this::attackPredicate));

        AnimationController<Snake> tongueController = new AnimationController<>(this, "tongueController", 0, this::tonguePredicate);
        tongueController.setSoundKeyframeHandler(this::soundListener);
        controllers.add(tongueController);
        controllers.add(new AnimationController<>(this, "rattleController", 0, this::rattlePredicate));
    }


    // GOALS

    static class SnakeMeleeAttackGoal extends MeleeAttackGoal {

        private long lastCanUseCheck;
        private Path path;

        public SnakeMeleeAttackGoal(@NotNull PathfinderMob pMob, double pSpeedModifier, boolean pFollowingTargetEvenIfNotSeen) {
            super(pMob, pSpeedModifier, pFollowingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            return mob.getMainHandItem().isEmpty() && testUse();
        }

        boolean testUse(){
            long l = this.mob.level().getGameTime();
            if (l - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = l;
                LivingEntity livingEntity = this.mob.getTarget();
                if (livingEntity == null) {
                    return false;
                } else if (!livingEntity.isAlive()) {
                    return false;
                } else {
                    this.path = this.mob.getNavigation().createPath(livingEntity, 0);
                    if (this.path != null) {
                        return true;
                    } else {
                        double attackReach = 4.0F + livingEntity.getBbWidth();
                        return attackReach >= this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getMainHandItem().isEmpty() && super.canContinueToUse();
        }

    }
}
