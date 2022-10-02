package net.pevori.queencats.entity.custom;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.pevori.queencats.entity.ModEntities;
import net.pevori.queencats.entity.goals.HumanoidCatMeleeGoal;
import net.pevori.queencats.entity.variant.PrincessCatVariant;
import net.pevori.queencats.entity.variant.QueenCatVariant;
import net.pevori.queencats.item.ModItems;
import net.pevori.queencats.sound.ModSounds;

import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class PrincessCatEntity extends HumanoidCatEntity implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);

    public PrincessCatEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return null;
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0f)
                .add(EntityAttributes.GENERIC_ATTACK_SPEED, 1.0f)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3f);
    }

    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new SitGoal(this));
        this.goalSelector.add(2, new HumanoidCatMeleeGoal(this, 1.25D));
        this.goalSelector.add(3, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f, false));
        this.goalSelector.add(5, new TemptGoal(this, 1.0f, Ingredient.ofItems(ModItems.GOLDEN_FISH), false));
        this.goalSelector.add(5, new WanderAroundPointOfInterestGoal(this, 1.0f, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0, 1));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));

        this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
        this.targetSelector.add(2, new AttackWithOwnerGoal(this));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.isMoving() && !isSitting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.humanoidcat.walk", true));
            return PlayState.CONTINUE;
        }

        if (this.isSitting()) {
            event.getController()
                    .setAnimation(new AnimationBuilder().addAnimation("animation.humanoidcat.sitting", true));
            return PlayState.CONTINUE;
        }

        if (this.getAttackingState()) {
            event.getController()
                    .setAnimation(new AnimationBuilder().addAnimation("animation.humanoidcat.attack", true));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.humanoidcat.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.HUMANOID_CAT_AMBIENT;
    }

    @Override
    public SoundEvent getEatSound(ItemStack stack) {
        return ModSounds.HUMANOID_CAT_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.HUMANOID_CAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.HUMANOID_CAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15f, 1.0f);
    }

    /* TAMEABLE ENTITY */
    private static final TrackedData<Boolean> SITTING = DataTracker.registerData(PrincessCatEntity.class,
            TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);
        Item item = itemstack.getItem();

        Ingredient itemForHealing = Ingredient.ofItems(Items.COD, Items.SALMON, ModItems.GOLDEN_FISH);
        Item itemForTaming = ModItems.GOLDEN_FISH;
        Item itemForGrowth = ModItems.KEMOMIMI_POTION;

        if (item instanceof DyeItem && this.isOwner(player)) {
            DyeColor dyeColor = ((DyeItem) item).getColor();
            if (dyeColor == DyeColor.BLACK) {
                this.setVariant(PrincessCatVariant.BLACK);
            } else if (dyeColor == DyeColor.WHITE) {
                this.setVariant(PrincessCatVariant.WHITE);
            } else if (dyeColor == DyeColor.ORANGE) {
                this.setVariant(PrincessCatVariant.CALICO);
            } else if (dyeColor == DyeColor.GRAY) {
                this.setVariant(PrincessCatVariant.CALLAS);
            }

            if (!player.getAbilities().creativeMode) {
                itemstack.decrement(1);
            }

            this.setPersistent();
            return ActionResult.CONSUME;
        }

        if (item == itemForGrowth && isTamed() && this.isOwner(player)) {
            if (!player.getAbilities().creativeMode) {
                itemstack.decrement(1);
            }
            startGrowth();
            return ActionResult.CONSUME;
        }

        if ((itemForHealing.test(itemstack)) && isTamed() && this.getHealth() < getMaxHealth()) {
            if (this.world.isClient()) {
                return ActionResult.CONSUME;
            } else {
                if (!player.getAbilities().creativeMode) {
                    itemstack.decrement(1);
                }

                if (!this.world.isClient()) {
                    this.eat(player, hand, itemstack);
                    this.heal(10.0f);

                    if (this.getHealth() > getMaxHealth()) {
                        this.setHealth(getMaxHealth());
                    }

                    this.playSound(ModSounds.HUMANOID_CAT_EAT, 1.0f, 1.0f);
                }

                return ActionResult.SUCCESS;
            }
        }

        else if (item == itemForTaming && !isTamed()) {
            if (this.world.isClient()) {
                return ActionResult.CONSUME;
            } else {
                if (!player.getAbilities().creativeMode) {
                    itemstack.decrement(1);
                }

                if (!this.world.isClient()) {
                    super.setOwner(player);
                    this.navigation.recalculatePath();
                    this.setTarget(null);
                    this.world.sendEntityStatus(this, (byte) 7);
                    setSit(true);
                    this.setHealth(getMaxHealth());
                }

                return ActionResult.SUCCESS;
            }
        }

        if (isTamed() && this.isOwner(player) && !this.world.isClient() && hand == Hand.MAIN_HAND) {
            setSit(!isSitting());
            return ActionResult.SUCCESS;
        }

        if (itemstack.getItem() == itemForTaming) {
            return ActionResult.PASS;
        }

        return super.interactMob(player, hand);
    }

    public void startGrowth() {
        PrincessCatVariant variant = this.getVariant();
        QueenCatEntity queenCatEntity = ModEntities.QUEEN_CAT.create(world);
        queenCatEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), this.getPitch());
        queenCatEntity.setAiDisabled(this.isAiDisabled());

        if (variant == PrincessCatVariant.BLACK) {
            queenCatEntity.setVariant(QueenCatVariant.BLACK);
        } else if (variant == PrincessCatVariant.WHITE) {
            queenCatEntity.setVariant(QueenCatVariant.WHITE);
        }

        if (this.hasCustomName()) {
            queenCatEntity.setCustomName(this.getCustomName());
            queenCatEntity.setCustomNameVisible(this.isCustomNameVisible());
        }
        queenCatEntity.setPersistent();
        queenCatEntity.setOwnerUuid(this.getOwnerUuid());
        queenCatEntity.setTamed(true);
        queenCatEntity.setSitting(this.isSitting());
        world.spawnEntity(queenCatEntity);
        this.discard();
    }

    public void setSit(boolean sitting) {
        this.dataTracker.set(SITTING, sitting);
        super.setSitting(sitting);
    }

    public boolean isSitting() {
        return this.dataTracker.get(SITTING);
    }

    @Override
    public boolean canAttackWithOwner(LivingEntity target, LivingEntity owner) {
        if (target instanceof CreeperEntity || target instanceof GhastEntity) {
            return false;
        }
        if (target instanceof QueenCatEntity) {
            QueenCatEntity queenCatEntity = (QueenCatEntity) target;
            return !queenCatEntity.isTamed() || queenCatEntity.getOwner() != owner;
        }
        if (target instanceof PrincessCatEntity) {
            PrincessCatEntity princessCatEntity = (PrincessCatEntity) target;
            return !princessCatEntity.isTamed() || princessCatEntity.getOwner() != owner;
        }
        if (target instanceof PlayerEntity && owner instanceof PlayerEntity
                && !((PlayerEntity) owner).shouldDamagePlayer((PlayerEntity) target)) {
            return false;
        }
        if (target instanceof HorseEntity && ((HorseEntity) target).isTame()) {
            return false;
        }
        return !(target instanceof TameableEntity) || !((TameableEntity) target).isTamed();
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed) {
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(40.0D);
            getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(3.5D);
            getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue((double) 0.3f);
        } else {
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0D);
            getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(2.0D);
            getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue((double) 0.3f);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("isSitting", this.dataTracker.get(SITTING));
        nbt.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(SITTING, nbt.getBoolean("isSitting"));
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public AbstractTeam getScoreboardTeam() {
        return super.getScoreboardTeam();
    }

    public boolean canBeLeashedBy(PlayerEntity player) {
        return false;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(ATTACK_STATE, false);
        this.dataTracker.startTracking(DATA_ID_TYPE_VARIANT, 0);
    }

    /* VARIANTS */
    private static final TrackedData<Integer> DATA_ID_TYPE_VARIANT = DataTracker.registerData(PrincessCatEntity.class,
            TrackedDataHandlerRegistry.INTEGER);

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason,
            @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        PrincessCatVariant variant = Util.getRandom(PrincessCatVariant.values(), this.random);
        setVariant(variant);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public PrincessCatVariant getVariant() {
        return PrincessCatVariant.byId(this.getTypeVariant() & 255);
    }

    private int getTypeVariant() {
        return this.dataTracker.get(DATA_ID_TYPE_VARIANT);
    }

    protected void setVariant(PrincessCatVariant variant) {
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }

}