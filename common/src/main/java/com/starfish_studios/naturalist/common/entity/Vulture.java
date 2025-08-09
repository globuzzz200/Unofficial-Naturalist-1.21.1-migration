package com.starfish_studios.naturalist.common.entity;

import com.starfish_studios.naturalist.common.entity.core.ai.goal.FlyingWanderGoal;
import com.starfish_studios.naturalist.common.entity.core.ai.navigation.SmartBodyHelper;
import com.starfish_studios.naturalist.core.registry.NaturalistSoundEvents;
import com.starfish_studios.naturalist.core.registry.NaturalistTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;import net.minecraft.world.level.LevelAccessor;
import com.starfish_studios.naturalist.Naturalist;import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.starfish_studios.naturalist.common.entity.core.NaturalistGeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class Vulture extends PathfinderMob implements NaturalistGeoEntity, FlyingAnimal {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.ROTTEN_FLESH);
    private int ticksSinceEaten;

    protected static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.sf_nba.vulture.fly");


    public Vulture(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.setCanPickUpLoot(true);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.WATER, -1.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathType.COCOA, -1.0F);
        this.setPathfindingMalus(PathType.FENCE, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 0.0F);
    }


    @Override
    protected @NotNull BodyRotationControl createBodyControl() {
        return new SmartBodyHelper(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0F).add(Attributes.FLYING_SPEED, 0.6F).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 4.0F);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new VultureAttackGoal(this, 1.2F, true));
        this.goalSelector.addGoal(2, new VultureSearchForFoodGoal(this, 1.2F, FOOD_ITEMS, 12, 24));
        this.goalSelector.addGoal(3, new HighAltitudeFlyingWanderGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Monster.class, 10, false, false, entity -> isVultureHostile(entity) && !FOOD_ITEMS.test(this.getMainHandItem())));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, false, false, entity -> entity.getHealth() <= 6 && !entity.getMainHandItem().isEmpty() && !FOOD_ITEMS.test(this.getMainHandItem())));
    }

    public static boolean checkVultureSpawnRules(EntityType<Vulture> entityType, LevelAccessor state, MobSpawnType type, @NotNull BlockPos pos, RandomSource random) {
        return state.getBlockState(pos.below()).is(NaturalistTags.BlockTags.VULTURES_SPAWNABLE_ON) && state.getRawBrightness(pos, 0) > 8;
    }

    @Override
    protected void dropAllDeathLoot(@NotNull ServerLevel serverLevel, @NotNull DamageSource damageSource) {
        super.dropAllDeathLoot(serverLevel, damageSource);

        if (!this.level().isClientSide()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack itemStack = this.getItemBySlot(slot);
                if (!itemStack.isEmpty()) {
                    this.spawnAtLocation(itemStack);
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }


    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance effectInstance) {
        if (effectInstance.getEffect() == MobEffects.HUNGER) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return NaturalistSoundEvents.VULTURE_HURT.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return NaturalistSoundEvents.VULTURE_DEATH.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NaturalistSoundEvents.VULTURE_AMBIENT.get();
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return pSource.equals(this.damageSources().cactus()) || super.isInvulnerableTo(pSource);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean shouldHurt;
        float damage = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float knockback = (float)this.getAttributeValue(Attributes.ATTACK_KNOCKBACK);
        if (target instanceof LivingEntity livingEntity) {
            // damage += livingEntity.getMobType().equals(MobCategory.valueOf("UNDEAD")) ? damage : 0;
            // knockback += (float)EnchantmentHelper.getKnockbackBonus(this);
        }
        if (shouldHurt = target.hurt(target.damageSources().mobAttack(this), damage)) {
            if (knockback > 0.0f && target instanceof LivingEntity) {
                ((LivingEntity)target).knockback(knockback * 0.5f, Mth.sin(this.getYRot() * ((float)Math.PI / 180)), -Mth.cos(this.getYRot() * ((float)Math.PI / 180)));
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            // this.doEnchantDamageEffects(this, target);
            this.setLastHurtMob(target);
        }
        return shouldHurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.level().getProfiler().push("looting");
        if (!this.level().isClientSide && this.canPickUpLoot() && this.isAlive() && !this.dead && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            for(ItemEntity itementity : this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(1.0D, 1.0D, 1.0D))) {
                if (!itementity.isRemoved() && !itementity.getItem().isEmpty() && this.wantsToPickUp(itementity.getItem())) {
                    this.pickUpItem(itementity);
                }
            }
        }
        this.level().getProfiler().pop();
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            ItemStack stack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (stack.has(DataComponents.FOOD)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack finishedStack = stack.finishUsingItem(this.level(), this);
                    if (!finishedStack.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, finishedStack);
                    }
                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1f) {
                    this.playSound(this.getEatingSound(stack), 1.0f, 1.0f);
                    this.level().broadcastEntityEvent(this, (byte)45);
                }
            }
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 45) {
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    Vec3 vec3 = new Vec3(((double)this.random.nextFloat() - 0.5) * 0.1, Math.random() * 0.1 + 0.1, 0.0).xRot(-this.getXRot() * ((float)Math.PI / 180)).yRot(-this.getYRot() * ((float)Math.PI / 180));
                    this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, itemStack), this.getX() + this.getLookAngle().x / 2.0, this.getY(), this.getZ() + this.getLookAngle().z / 2.0, vec3.x, vec3.y + 0.05, vec3.z);
                }
            }
        }
    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        return !FOOD_ITEMS.test(this.getMainHandItem());
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return FOOD_ITEMS.test(pStack) && !FOOD_ITEMS.test(this.getMainHandItem());
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        if (this.canHoldItem(itemstack)) {
            if (!this.getMainHandItem().isEmpty() && !FOOD_ITEMS.test(this.getMainHandItem())) {
                this.dropItemStack(this.getMainHandItem());
            }
            this.onItemPickup(pItemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.take(pItemEntity, itemstack.getCount());
            pItemEntity.discard();
            this.ticksSinceEaten = 0;
        }
    }

    private void dropItemStack(ItemStack pStack) {
        ItemEntity itementity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), pStack);
        this.level().addFreshEntity(itementity);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        VulturePathNavigation navigation = new VulturePathNavigation(this, pLevel);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Naturalist.MOD_ID, "entities/vulture"));
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    private boolean isVultureHostile(Entity entity) {
        EntityType<?> entityType = entity.getType();
        return entityType == EntityType.ZOMBIE ||
               entityType == EntityType.ZOMBIE_VILLAGER ||
               entityType == EntityType.HUSK ||
               entityType == EntityType.ZOMBIFIED_PIGLIN ||
               entityType == EntityType.ZOGLIN ||
               entityType == EntityType.ZOMBIE_HORSE;
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    private <E extends Vulture> PlayState predicate(final AnimationState event) {
        event.getController().setAnimation(FLY);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(final AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::predicate));
    }


    static class VulturePathNavigation extends FlyingPathNavigation {
        public VulturePathNavigation(@NotNull Mob mob, Level level) {
            super(mob, level);
        }

        @Override
        public boolean isStableDestination(@NotNull BlockPos pos) {
            return super.isStableDestination(pos) && this.mob.level().getBlockState(pos).is(NaturalistTags.BlockTags.VULTURE_PERCH_BLOCKS);
        }
    }

    static class VultureAttackGoal extends MeleeAttackGoal {

        public VultureAttackGoal(@NotNull PathfinderMob mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
            super(mob, speedModifier, followingTargetEvenIfNotSeen);
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof Player player) {
                if (player.getHealth() > 6 || player.getMainHandItem().isEmpty())  {
                    return false;
                }
            }
            return this.mob.getMainHandItem().isEmpty() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.mob.getTarget();
            if (target instanceof Player player) {
                if (player.getHealth() > 6 || player.getMainHandItem().isEmpty())  {
                    return false;
                }
            }
            return this.mob.getMainHandItem().isEmpty() && super.canContinueToUse();
        }

        
        protected void checkAndPerformAttack(LivingEntity enemy, double distToEnemySqr) {
            double reach = 4.0F + enemy.getBbWidth();
            if (distToEnemySqr <= reach && this.getTicksUntilNextAttack() <= 0) {
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
                if (!(enemy instanceof Player)) {
                    this.mob.doHurtTarget(enemy);
                }
                if (enemy instanceof Player && this.mob.getMainHandItem().isEmpty() && !enemy.getMainHandItem().isEmpty()) {
                    this.mob.setItemSlot(EquipmentSlot.MAINHAND, enemy.getMainHandItem().split(1));
                    Level level = this.mob.level();
                    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.2F, ((level.random.nextFloat() - level.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                    this.mob.setTarget(null);
                    this.mob.setAggressive(false);
                }
            }
        }
    }

    static class VultureSearchForFoodGoal extends Goal {
        private final Vulture mob;
        private final double speedModifier;
        private final double horizontalSearchRange;
        private final double verticalSearchRange;
        private final Ingredient ingredient;

        public VultureSearchForFoodGoal(Vulture mob, double speedModifier, Ingredient ingredient, double horizontalSearchRange, double verticalSearchRange) {
            this.setFlags(EnumSet.of(Flag.MOVE));
            this.mob = mob;
            this.speedModifier = speedModifier;
            this.ingredient = ingredient;
            this.horizontalSearchRange = horizontalSearchRange;
            this.verticalSearchRange = verticalSearchRange;
        }

        @Override
        public boolean canUse() {
            if (!Vulture.FOOD_ITEMS.test(mob.getMainHandItem())) {
                List<ItemEntity> list = mob.level().getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(horizontalSearchRange, verticalSearchRange, horizontalSearchRange), itemEntity -> ingredient.test(itemEntity.getItem()));
                return !list.isEmpty() && !Vulture.FOOD_ITEMS.test(mob.getMainHandItem());
            }
            return false;
        }

        @Override
        public void tick() {
            List<ItemEntity> list = mob.level().getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(horizontalSearchRange, verticalSearchRange, horizontalSearchRange), itemEntity -> ingredient.test(itemEntity.getItem()));
            if (!Vulture.FOOD_ITEMS.test(mob.getMainHandItem()) && !list.isEmpty()) {
                mob.getNavigation().moveTo(list.get(0), speedModifier);
            }

        }

        @Override
        public void start() {
            List<ItemEntity> list = mob.level().getEntitiesOfClass(ItemEntity.class, mob.getBoundingBox().inflate(horizontalSearchRange, verticalSearchRange, horizontalSearchRange), itemEntity -> ingredient.test(itemEntity.getItem()));
            if (!list.isEmpty()) {
                mob.getNavigation().moveTo(list.get(0), speedModifier);
            }
        }
    }

    static class HighAltitudeFlyingWanderGoal extends FlyingWanderGoal {

        private int preferredAltitude = -1;

        //Min and Max is bound by heightmap and max build height
        private static final int MIN_ALTITUDE_ABOVE_GROUND = 8;
        private static final int MAX_ALTITUDE_ABOVE_GROUND = 20;
        //Extra offset from max build height
        private static final int ALTITUDE_MARGIN_FROM_BUILD_LIMIT = 32;

        //A bit of randomness for the altitude, vulture will now change altitude once every 16-32 wander attempts
        private int wanderAttemptsUntilAltitudeChange = 0;
        private static final int ALTITUDE_CHANGE_INTERVAL_MIN = 16;
        private static final int ALTITUDE_CHANGE_INTERVAL_RANGE = 16;

        public HighAltitudeFlyingWanderGoal(Vulture mob) {
            super(mob);
        }

        @Override
        public Vec3 findPos() {
            Vec3 lookVec = mob.getViewVector(0.0F);

            if (preferredAltitude < 0 || wanderAttemptsUntilAltitudeChange <= 0) {
                BlockPos mobPos = mob.blockPosition();
                Level level = mob.level();

                int groundY = level.getHeight(Heightmap.Types.WORLD_SURFACE, mobPos.getX(), mobPos.getZ());
                int maxY = level.getMaxBuildHeight();
                int minAltitude = groundY + MIN_ALTITUDE_ABOVE_GROUND;
                int maxAltitude = Math.min(groundY + MAX_ALTITUDE_ABOVE_GROUND, maxY - ALTITUDE_MARGIN_FROM_BUILD_LIMIT);
                preferredAltitude = Mth.nextInt(mob.getRandom(), minAltitude, maxAltitude);
                wanderAttemptsUntilAltitudeChange = ALTITUDE_CHANGE_INTERVAL_MIN + mob.getRandom().nextInt(ALTITUDE_CHANGE_INTERVAL_RANGE);
            } else {
                wanderAttemptsUntilAltitudeChange--;
            }

            double offsetX = lookVec.x * 8 + mob.getRandom().nextInt(5) - 2;
            double offsetZ = lookVec.z * 8 + mob.getRandom().nextInt(5) - 2;

            return new Vec3(mob.getX() + offsetX, preferredAltitude, mob.getZ() + offsetZ);
        }
    }
}
