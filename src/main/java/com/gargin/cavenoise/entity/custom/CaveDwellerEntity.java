package com.gargin.cavenoise.entity.custom;

import com.gargin.cavenoise.sound.ModSounds;
import java.util.Random;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.Animation.LoopType;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.network.GeckoLibNetwork;
import software.bernie.geckolib.network.packet.EntityAnimTriggerPacket;

public class CaveDwellerEntity extends Monster implements GeoEntity {
   private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
   public int rRollResult = 4;
   public boolean forcedStalk = false;
   public boolean isAggro;
   private float chanceOfSpawningAsStalker = 0.6F;
   private boolean isAggroState;
   private boolean returnShort = false;
   private boolean inTwoBlockSpace = false;
   public boolean spottedByPlayer = false;
   public boolean spottedOld = false;
   private boolean shouldClearAnim = true;
   public boolean squeezeCrawling = false;
   public boolean isFleeing;
   public boolean startedMovingChase = false;
   private float waitToStartAnimatorController = 20.0F;
   private Vec3 oldPos;
   private int ticksTillRemove;
   private float defaultMaxUpStep = 2.0F;
   private RawAnimation OLD_RUN;
   private RawAnimation IDLE;
   private RawAnimation CHASE;
   private RawAnimation CHASE_IDLE;
   private RawAnimation CROUCH_RUN;
   private RawAnimation CROUCH_IDLE;
   private RawAnimation CALM_RUN;
   private RawAnimation CALM_STILL;
   private RawAnimation IS_SPOTTED;
   private RawAnimation CRAWL;
   private RawAnimation FLEE;
   private RawAnimation STALK;
   private RawAnimation STALK_IDLE;
   private RawAnimation CLIMB;
   private RawAnimation currentAnim;
   public static final EntityDataAccessor<Boolean> FLEEING_ACCESSOR;
   public static final EntityDataAccessor<Boolean> CROUCHING_ACCESSOR;
   public static final EntityDataAccessor<Boolean> AGGRO_ACCESSOR;
   public static final EntityDataAccessor<Boolean> SQUEEZING_ACCESSOR;
   public static final EntityDataAccessor<Boolean> SPOTTED_ACCESSOR;
   public static final EntityDataAccessor<Boolean> CLIMBING_ACCESSOR;
   public static final EntityDataAccessor<Boolean> STALKING_ACCESSOR;
   public Logger logger;
   private float twoBlockSpaceCooldown;
   private float twoBlockSpaceTimer;
   private float movingCooldown;
   private float movingClock;
   private int chaseSoundClockReset;
   private int climbSoundClockReset;
   private int climbSoundClock;
   private int chaseSoundClock;
   private boolean alreadyPlayedFleeSound;
   private boolean alreadyPlayedSpottedSound;
   private boolean startedPlayingChaseSound;
   private boolean alreadyPlayedDeathSound;

   public CaveDwellerEntity(EntityType<? extends CaveDwellerEntity> pEntityType, Level pLevel) {
      super(pEntityType, pLevel);
      this.OLD_RUN = RawAnimation.begin().then("animation.cave_dweller.run", LoopType.LOOP);
      this.IDLE = RawAnimation.begin().then("animation.cave_dweller.idle", LoopType.LOOP);
      this.CHASE = RawAnimation.begin().then("animation.cave_dweller.new_run", LoopType.LOOP);
      this.CHASE_IDLE = RawAnimation.begin().then("animation.cave_dweller.run_idle", LoopType.LOOP);
      this.CROUCH_RUN = RawAnimation.begin().then("animation.cave_dweller.crouch_run_new", LoopType.LOOP);
      this.CROUCH_IDLE = RawAnimation.begin().then("animation.cave_dweller.crouch_idle", LoopType.LOOP);
      this.CALM_RUN = RawAnimation.begin().then("animation.cave_dweller.calm_move", LoopType.LOOP);
      this.CALM_STILL = RawAnimation.begin().then("animation.cave_dweller.calm_idle", LoopType.LOOP);
      this.IS_SPOTTED = RawAnimation.begin().then("animation.cave_dweller.spotted", LoopType.HOLD_ON_LAST_FRAME);
      this.CRAWL = RawAnimation.begin().then("animation.cave_dweller.crawl", LoopType.HOLD_ON_LAST_FRAME);
      this.FLEE = RawAnimation.begin().then("animation.cave_dweller.flee", LoopType.LOOP);
      this.STALK = RawAnimation.begin().then("animation.cave_dweller.stalking", LoopType.LOOP);
      this.STALK_IDLE = RawAnimation.begin().then("animation.cave_dweller.stalking_idle", LoopType.HOLD_ON_LAST_FRAME);
      this.CLIMB = RawAnimation.begin().then("animation.cave_dweller.climb", LoopType.LOOP);
      this.logger = LogManager.getLogManager().getLogger("cavenoise");
      this.twoBlockSpaceTimer = 0.0F;
      this.movingCooldown = 3.0F;
      this.movingClock = 3.0F;
      this.chaseSoundClockReset = 80;
      this.climbSoundClockReset = 10;
      this.climbSoundClock = 0;
      this.chaseSoundClock = 0;
      this.alreadyPlayedFleeSound = false;
      this.alreadyPlayedSpottedSound = false;
      this.startedPlayingChaseSound = false;
      this.alreadyPlayedDeathSound = false;
      this.setMaxUpStep(this.defaultMaxUpStep);
      this.refreshDimensions();
      this.twoBlockSpaceCooldown = 5.0F;
      this.oldPos = this.position();
      this.ticksTillRemove = 6000;
      ItemStack enchantedBoots = new ItemStack(Items.CHAINMAIL_BOOTS);
      enchantedBoots.enchant(Enchantments.DEPTH_STRIDER, 3);
      this.setItemSlot(EquipmentSlot.FEET, enchantedBoots);
      this.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 999999, 100, true, false));
      this.forcedStalk = true;
   }

   public static AttributeSupplier setAttributes() {
      return Monster.createMobAttributes().add(Attributes.MAX_HEALTH, 65.0D).add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.ATTACK_SPEED, 0.35D).add(Attributes.MOVEMENT_SPEED, 0.6000000238418579D).add(Attributes.FOLLOW_RANGE, 100.0D).add(Attributes.ATTACK_KNOCKBACK, 2.0D).build();
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(FLEEING_ACCESSOR, false);
      this.entityData.define(CROUCHING_ACCESSOR, false);
      this.entityData.define(AGGRO_ACCESSOR, false);
      this.entityData.define(SQUEEZING_ACCESSOR, false);
      this.entityData.define(SPOTTED_ACCESSOR, false);
      this.entityData.define(CLIMBING_ACCESSOR, false);
      this.entityData.define(STALKING_ACCESSOR, false);
   }

   protected void registerGoals() {
      this.goalSelector.addGoal(2, new DwellerStareGoal(this, 100.0F));
      this.goalSelector.addGoal(2, new DwellerChaseGoal(this, this, 0.8500000238418579D, true, 20.0F));
      this.goalSelector.addGoal(2, new DwellerFleeGoal(this, 20.0F, 1.0D));
      this.goalSelector.addGoal(2, new DwellerStalkGoal(this, 0.5D, 15.0F));
      this.goalSelector.addGoal(2, new DwellerStrollGoal(this, 0.7D));
      this.goalSelector.addGoal(2, new DwellerBreakInvisGoal(this));
      this.targetSelector.addGoal(1, new DwellerTargetTooCloseGoal(this, 12.0F));
      this.targetSelector.addGoal(2, new DwellerTargetSeesMeGoal(this));
   }

   protected boolean canRide(Entity pVehicle) {
      return false;
   }

   public Vec3 generatePos(Entity player) {
      Vec3 playerPos = player.position();
      Random rand = new Random();
      double randX = (double)(rand.nextInt(70) - 35);
      double randZ = (double)(rand.nextInt(70) - 35);
      double posX = playerPos.x + randX;
      double posY = playerPos.y + 10.0D;
      double posZ = playerPos.z + randZ;

      for(int runFor = 100; runFor >= 0; --posY) {
         BlockPos blockPosition = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY), (int)Math.floor(posZ));
         BlockPos blockPosition2 = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY + 1.0D), (int)Math.floor(posZ));
         BlockPos blockPosition3 = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY + 2.0D), (int)Math.floor(posZ));
         BlockPos blockPosition4 = new BlockPos((int)Math.floor(posX), (int)Math.floor(posY - 1.0D), (int)Math.floor(posZ));
         --runFor;
         if (!this.level().getBlockState(blockPosition).blocksMotion() && !this.level().getBlockState(blockPosition2).blocksMotion() && !this.level().getBlockState(blockPosition3).blocksMotion() && this.level().getBlockState(blockPosition4).blocksMotion()) {
            break;
         }
      }

      return new Vec3(posX, posY, posZ);
   }

   public void tick() {
      --this.ticksTillRemove;
      if (this.ticksTillRemove <= 0 && (!this.isPlayerLookingTowards() || !this.inPlayerLineOfSight())) {
         this.playDisappearSound();
         this.discard();
      }

      MutableBlockPos blockpos$mutableblockpos = new MutableBlockPos(this.position().x, this.position().y + 2.0D, this.position().z);
      BlockState blockstate = this.level().getBlockState(blockpos$mutableblockpos);
      boolean flag = blockstate.blocksMotion();
      if (flag) {
         this.twoBlockSpaceTimer = this.twoBlockSpaceCooldown;
         this.inTwoBlockSpace = true;
         if (this.getTarget() != null) {
         }
      } else {
         --this.twoBlockSpaceTimer;
         if (this.twoBlockSpaceTimer <= 0.0F) {
            this.inTwoBlockSpace = false;
         }
      }

      if (this.isAggro || this.isFleeing) {
         this.shouldClearAnim = false;
         this.spottedByPlayer = false;
         this.entityData.set(SPOTTED_ACCESSOR, false);
      }

      super.tick();
      this.entityData.set(CROUCHING_ACCESSOR, this.inTwoBlockSpace);
      if ((Boolean)this.entityData.get(SPOTTED_ACCESSOR)) {
         this.playSpottedSound();
      }

   }

   public boolean isMoving() {
      Vec3 velocity = this.getDeltaMovement();
      float avgVelocity = (float)(Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0F;
      if (this.getTarget() != null) {
      }

      return avgVelocity > 0.03F;
   }

   private void TriggeredAnimationControllerTick() {
      int testNum = 0;
      --this.waitToStartAnimatorController;
      if (this.waitToStartAnimatorController <= 0.0F) {
         if (this.squeezeCrawling) {
            int var2 = testNum + 1;
            this.triggerAnim("controller", "crawl");
            this.currentAnim = this.CRAWL;
            return;
         }

         if (this.spottedByPlayer) {
            this.triggerAnim("controller", "is_spotted");
            this.currentAnim = this.IS_SPOTTED;
         }
      }

      if (this.getTarget() != null) {
      }

   }

   public void triggerDwellerAnim(@Nullable String controllerName, String animName, RawAnimation animRaw) {
      RawAnimation anim = this.currentAnim;
      if (this.getTarget() != null && anim != null) {
      }

      if (anim != null) {
         if (anim != animRaw) {
            if (this.getTarget() != null) {
               this.getTarget().sendSystemMessage(Component.nullToEmpty("anim does not match name, setting." + anim + " -> " + animName));
            }

            if (this.level().isClientSide()) {
               this.getAnimatableInstanceCache().getManagerForId((long)this.getId()).tryTriggerAnimation(controllerName, animName);
            } else {
               GeckoLibNetwork.send(new EntityAnimTriggerPacket(this.getId(), controllerName, animName), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> {
                  return this;
               }));
            }
         }
      } else {
         if (this.getTarget() != null) {
            this.getTarget().sendSystemMessage(Component.nullToEmpty("anim null and setting"));
         }

         if (this.level().isClientSide()) {
            this.getAnimatableInstanceCache().getManagerForId((long)this.getId()).tryTriggerAnimation(controllerName, animName);
         } else {
            GeckoLibNetwork.send(new EntityAnimTriggerPacket(this.getId(), controllerName, animName), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> {
               return this;
            }));
         }
      }

   }

   public void rRoll() {
      Random rand = new Random();
      this.forcedStalk = false;
      this.rRollResult = rand.nextInt(4);
   }

   public boolean shouldSpawnAsStalker() {
      Random rand = new Random();
      float stalkerResult = rand.nextFloat();
      float var3;
      int var10001 = (var3 = stalkerResult - this.chanceOfSpawningAsStalker) == 0.0F ? 0 : (var3 < 0.0F ? -1 : 1);
      System.out.println("spawned as stalker: " + (var10001 < 0));
      return stalkerResult < this.chanceOfSpawningAsStalker;
   }

   public Path createShortPath(LivingEntity pathTarget) {
      this.returnShort = true;
      this.refreshDimensions();
      this.setMaxUpStep(100.0F);
      Path shortPath = this.getNavigation().createPath(pathTarget, 0);
      this.setMaxUpStep(0.0F);
      this.returnShort = false;
      this.refreshDimensions();
      return shortPath;
   }

   public Path createClimbPath(LivingEntity pathTarget) {
      this.setMaxUpStep(100.0F);
      Path climbPath = this.getNavigation().createPath(pathTarget, 0);
      this.setMaxUpStep(this.defaultMaxUpStep);
      return climbPath;
   }

   private PlayState predicate(AnimationState tAnimationState) {
      if ((Boolean)this.entityData.get(AGGRO_ACCESSOR)) {
         if ((Boolean)this.entityData.get(CLIMBING_ACCESSOR)) {
            return tAnimationState.setAndContinue(this.CLIMB);
         } else if ((Boolean)this.entityData.get(SQUEEZING_ACCESSOR)) {
            return tAnimationState.setAndContinue(this.CRAWL);
         } else if ((Boolean)this.entityData.get(CROUCHING_ACCESSOR)) {
            return tAnimationState.isMoving() ? tAnimationState.setAndContinue(this.CROUCH_RUN) : tAnimationState.setAndContinue(this.CROUCH_IDLE);
         } else {
            return tAnimationState.isMoving() ? tAnimationState.setAndContinue(this.CHASE) : tAnimationState.setAndContinue(this.CHASE_IDLE);
         }
      } else if ((Boolean)this.entityData.get(FLEEING_ACCESSOR)) {
         return tAnimationState.isMoving() ? tAnimationState.setAndContinue(this.FLEE) : tAnimationState.setAndContinue(this.CHASE_IDLE);
      } else if ((Boolean)this.entityData.get(STALKING_ACCESSOR)) {
         return tAnimationState.isMoving() ? tAnimationState.setAndContinue(this.STALK) : tAnimationState.setAndContinue(this.STALK_IDLE);
      } else if ((Boolean)this.entityData.get(SPOTTED_ACCESSOR)) {
         return tAnimationState.setAndContinue(this.IS_SPOTTED);
      } else {
         return tAnimationState.isMoving() ? tAnimationState.setAndContinue(this.CALM_RUN) : tAnimationState.setAndContinue(this.CALM_STILL);
      }
   }

   public void registerControllers(ControllerRegistrar controllerRegistrar) {
      controllerRegistrar.add(new AnimationController[]{(new AnimationController(this, "controller", 3, this::predicate)).triggerableAnim("calm_run", this.CALM_RUN).triggerableAnim("calm_still", this.CALM_STILL).triggerableAnim("chase", this.CHASE).triggerableAnim("chase_idle", this.CHASE_IDLE).triggerableAnim("crouch_run", this.CROUCH_RUN).triggerableAnim("crouch_idle", this.CROUCH_IDLE).triggerableAnim("is_spotted", this.IS_SPOTTED).triggerableAnim("crawl", this.CRAWL)});
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }

   protected void playStepSound(BlockPos pPos, BlockState pState) {
      super.playStepSound(pPos, pState);
      this.playEntitySound(this.chooseStep());
   }

   private void playEntitySound(SoundEvent soundEvent) {
      this.playEntitySound(soundEvent, 1.0F, 1.0F);
   }

   private void playEntitySound(SoundEvent soundEvent, float volume, float pitch) {
      this.level().playSound((Player)null, this, soundEvent, SoundSource.HOSTILE, volume, pitch);
   }

   private void playBlockPosSound(SoundEvent soundEvent, float volume, float pitch) {
      BlockPos blockPos = this.blockPosition();
      Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(soundEvent, SoundSource.HOSTILE, volume, pitch, RandomSource.create(), blockPos));
   }

   public void playChaseSound() {
      if (this.startedPlayingChaseSound || this.isMoving()) {
         if (this.chaseSoundClock <= 0) {
            Random rand = new Random();
            switch(rand.nextInt(4)) {
            case 0:
               this.playEntitySound((SoundEvent)ModSounds.CHASE_1.get(), 3.0F, 1.0F);
               break;
            case 1:
               this.playEntitySound((SoundEvent)ModSounds.CHASE_2.get(), 3.0F, 1.0F);
               break;
            case 2:
               this.playEntitySound((SoundEvent)ModSounds.CHASE_3.get(), 3.0F, 1.0F);
               break;
            case 3:
               this.playEntitySound((SoundEvent)ModSounds.CHASE_4.get(), 3.0F, 1.0F);
            }

            this.startedPlayingChaseSound = true;
            this.resetChaseSoundClock();
         }

         --this.chaseSoundClock;
      }

   }

   public void playClimbSound() {
      if (this.climbSoundClock <= 0) {
         Random rand = new Random();
         switch(rand.nextInt(8)) {
         case 0:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_1.get(), 3.0F, 1.0F);
            break;
         case 1:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_2.get(), 3.0F, 1.0F);
            break;
         case 2:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_3.get(), 3.0F, 1.0F);
            break;
         case 3:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_4.get(), 3.0F, 1.0F);
            break;
         case 4:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_5.get(), 3.0F, 1.0F);
            break;
         case 5:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_6.get(), 3.0F, 1.0F);
            break;
         case 6:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_7.get(), 3.0F, 1.0F);
            break;
         case 7:
            this.playEntitySound((SoundEvent)ModSounds.DWELLER_CLIMB_8.get(), 3.0F, 1.0F);
         }

         this.resetClimbSoundClock();
      }

      --this.climbSoundClock;
   }

   public void playFleeSound() {
      if (!this.alreadyPlayedFleeSound) {
         Random rand = new Random();
         switch(rand.nextInt(2)) {
         case 0:
            this.playEntitySound((SoundEvent)ModSounds.FLEE_1.get(), 3.0F, 1.0F);
            break;
         case 1:
            this.playEntitySound((SoundEvent)ModSounds.FLEE_2.get(), 3.0F, 1.0F);
         }

         this.alreadyPlayedFleeSound = true;
      }

   }

   public void playSpottedSound() {
      if (!this.alreadyPlayedSpottedSound) {
         this.playEntitySound((SoundEvent)ModSounds.SPOTTED.get(), 3.0F, 1.0F);
         this.alreadyPlayedSpottedSound = true;
      }

   }

   public boolean inPlayerLineOfSight() {
      return this.getTarget() != null ? this.getTarget().hasLineOfSight(this) : false;
   }

   public boolean isPlayerLookingTowards() {
      if (this.getTarget() == null) {
         return false;
      } else {
         Minecraft minecraft = Minecraft.getInstance();
         boolean yawPlayerLookingTowards = false;
         float fov = (float)(Integer)minecraft.options.fov().get();
         float yFovMod = 0.65F;
         float fovMod = (35.0F / fov - 1.0F) * 0.4F + 1.0F;
         fov *= fovMod;
         Vec3 a = this.getTarget().position();
         Vec3 b = this.position();
         Vec2 dist = new Vec2((float)b.x - (float)a.x, (float)b.z - (float)a.z);
         dist = dist.normalized();
         double newAngle = Math.toDegrees(Math.atan2((double)dist.x, (double)dist.y));
         float lookX = (float)this.getTarget().getViewVector(1.0F).x;
         float lookZ = (float)this.getTarget().getViewVector(1.0F).z;
         double newLookAngle = Math.toDegrees(Math.atan2((double)lookX, (double)lookZ));
         double newNewAngle = this.loopAngle(newAngle - newLookAngle) + (double)fov;
         newNewAngle = this.loopAngle(newNewAngle);
         if (newNewAngle > 0.0D && newNewAngle < (double)(fov * 2.0F)) {
            yawPlayerLookingTowards = true;
         }

         boolean pitchPlayerLookingTowards = false;
         boolean shouldOnlyUsePitch = false;
         float yFov = fov * yFovMod;
         Vec2 yDist = new Vec2((float)Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.z - a.z) * (b.z - a.z)), (float)(b.y - a.y));
         yDist = yDist.normalized();
         double yAngle = Math.toDegrees(Math.atan2((double)yDist.x, (double)yDist.y));
         float lookY = (float)this.getTarget().getViewVector(1.0F).y;
         Vec2 lookDist = new Vec2((float)Math.sqrt((double)(lookX * lookX + lookZ * lookZ)), lookY);
         lookDist = lookDist.normalized();
         double yLookAngle = Math.toDegrees(Math.atan2((double)lookDist.x, (double)lookDist.y));
         double newYAngle = this.loopAngle(yAngle - yLookAngle) + (double)yFov;
         newYAngle = this.loopAngle(newYAngle);
         if (newYAngle > 0.0D && newYAngle < (double)(yFov * 2.0F)) {
            pitchPlayerLookingTowards = true;
         }

         if (!(yLookAngle < (double)(180.0F - yFov)) || !(yLookAngle > (double)yFov)) {
            shouldOnlyUsePitch = true;
         }

         return (yawPlayerLookingTowards || shouldOnlyUsePitch) && pitchPlayerLookingTowards;
      }
   }

   public double loopAngle(double angle) {
      double var4;
      if (angle > 360.0D) {
         return var4 = angle - 360.0D;
      } else {
         return angle < 0.0D ? (var4 = angle + 360.0D) : angle;
      }
   }

   public void playDisappearSound() {
      this.playBlockPosSound((SoundEvent)ModSounds.DISAPPEAR.get(), 3.0F, 1.0F);
   }

   private void resetChaseSoundClock() {
      this.chaseSoundClock = this.chaseSoundClockReset;
   }

   private void resetClimbSoundClock() {
      this.climbSoundClock = this.climbSoundClockReset;
   }

   private SoundEvent chooseStep() {
      Random rand = new Random();
      switch(rand.nextInt(4)) {
      case 0:
         return (SoundEvent)ModSounds.CHASE_STEP_1.get();
      case 1:
         return (SoundEvent)ModSounds.CHASE_STEP_2.get();
      case 2:
         return (SoundEvent)ModSounds.CHASE_STEP_3.get();
      case 3:
         return (SoundEvent)ModSounds.CHASE_STEP_4.get();
      default:
         return (SoundEvent)ModSounds.CHASE_STEP_1.get();
      }
   }

   public EntityDimensions getDimensions(Pose pPose) {
      if (this.isAggro) {
         return this.returnShort ? new EntityDimensions(0.5F, 0.9F, true) : new EntityDimensions(0.5F, 1.9F, true);
      } else {
         return new EntityDimensions(0.5F, 1.9F, true);
      }
   }

   private SoundEvent chooseHurtSound() {
      Random rand = new Random();
      switch(rand.nextInt(4)) {
      case 0:
         return (SoundEvent)ModSounds.DWELLER_HURT_1.get();
      case 1:
         return (SoundEvent)ModSounds.DWELLER_HURT_2.get();
      case 2:
         return (SoundEvent)ModSounds.DWELLER_HURT_3.get();
      case 3:
         return (SoundEvent)ModSounds.DWELLER_HURT_4.get();
      default:
         return (SoundEvent)ModSounds.DWELLER_HURT_1.get();
      }
   }

   protected void playHurtSound(DamageSource pSource) {
      SoundEvent soundevent = this.chooseHurtSound();
      if (soundevent != null) {
         this.playEntitySound(soundevent, 2.0F, 1.0F);
      }

   }

   protected void tickDeath() {
      this.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
      super.tickDeath();
      if (!this.alreadyPlayedDeathSound) {
         this.playBlockPosSound((SoundEvent)ModSounds.DWELLER_DEATH.get(), 2.0F, 1.0F);
         this.alreadyPlayedDeathSound = true;
      }

   }

   protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return this.chooseHurtSound();
   }

   protected SoundEvent getDeathSound() {
      return (SoundEvent)ModSounds.DWELLER_DEATH.get();
   }

   protected float getSoundVolume() {
      return 0.4F;
   }

   static {
      FLEEING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
      CROUCHING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
      AGGRO_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
      SQUEEZING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
      SPOTTED_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
      CLIMBING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
      STALKING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
   }
}
