package com.gargin.cavenoise.entity.custom;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DwellerFleeGoal extends Goal {
   private final CaveDwellerEntity cavedweller;
   private final float ticksTillLeave;
   private final float ticksTillFlee;
   private float currentTicksTillLeave;
   private float currentTicksTillFlee;
   private boolean shouldLeave;
   private double fleeX;
   private double fleeY;
   private double fleeZ;
   private int ticksUntilNextPathRecalculation;
   private double speedModifier;

   public DwellerFleeGoal(CaveDwellerEntity pCaveDweller, float pTicksTillLeave, double pSpeedModifier) {
      this.cavedweller = pCaveDweller;
      this.ticksTillLeave = pTicksTillLeave;
      this.currentTicksTillLeave = pTicksTillLeave;
      this.ticksTillFlee = 10.0F;
      this.currentTicksTillFlee = this.ticksTillFlee;
      this.speedModifier = pSpeedModifier;
   }

   public boolean canUse() {
      if (this.cavedweller.isInvisible()) {
         return false;
      } else {
         return this.cavedweller.rRollResult == 2 && !this.cavedweller.forcedStalk ? this.cavedweller.getTarget() != null : false;
      }
   }

   public boolean canContinueToUse() {
      return this.cavedweller.rRollResult == 2 && !this.cavedweller.forcedStalk ? this.cavedweller.getTarget() != null : false;
   }

   public void start() {
      this.getSpotToWalk();
      this.cavedweller.spottedByPlayer = false;
      this.shouldLeave = false;
   }

   public void stop() {
   }

   public boolean isPlayerLookingTowards() {
      LivingEntity pendingTarget = this.cavedweller.getTarget();
      Minecraft minecraft = Minecraft.getInstance();
      boolean yawPlayerLookingTowards = false;
      float fov = (float)(Integer)minecraft.options.fov().get();
      float yFovMod = 0.65F;
      float fovMod = (35.0F / fov - 1.0F) * 0.4F + 1.0F;
      fov *= fovMod;
      Vec3 a = pendingTarget.position();
      Vec3 b = this.cavedweller.position();
      Vec2 dist = new Vec2((float)b.x - (float)a.x, (float)b.z - (float)a.z);
      dist = dist.normalized();
      double newAngle = Math.toDegrees(Math.atan2((double)dist.x, (double)dist.y));
      float lookX = (float)pendingTarget.getViewVector(1.0F).x;
      float lookZ = (float)pendingTarget.getViewVector(1.0F).z;
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
      float lookY = (float)pendingTarget.getViewVector(1.0F).y;
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

   public boolean inPlayerLineOfSight() {
      return this.cavedweller.getTarget() != null ? this.cavedweller.getTarget().hasLineOfSight(this.cavedweller) : false;
   }

   public double loopAngle(double angle) {
      double var4;
      if (angle > 360.0D) {
         return var4 = angle - 360.0D;
      } else {
         return angle < 0.0D ? (var4 = angle + 360.0D) : angle;
      }
   }

   private boolean getSpotToWalk() {
      Random rand = new Random();
      double randX = rand.nextDouble() - 0.5D;
      double randY = (double)(rand.nextInt(64) - 32);
      double randZ = rand.nextDouble() - 0.5D;
      if (randX > 0.0D) {
         this.fleeX = (this.cavedweller.getX() + 1.0D) * 64.0D;
      } else {
         this.fleeX = (this.cavedweller.getX() - 1.0D) * 64.0D;
      }

      this.fleeY = this.cavedweller.getY() + randY;
      if (randZ > 0.0D) {
         this.fleeZ = (this.cavedweller.getZ() + 1.0D) * 64.0D;
      } else {
         this.fleeZ = (this.cavedweller.getZ() - 1.0D) * 64.0D;
      }

      MutableBlockPos blockpos$mutableblockpos = new MutableBlockPos(this.fleeX, this.fleeY, this.fleeZ);

      while(blockpos$mutableblockpos.getY() > this.cavedweller.level.getMinBuildHeight() && !this.cavedweller.level.getBlockState(blockpos$mutableblockpos).getMaterial().blocksMotion()) {
         blockpos$mutableblockpos.move(Direction.DOWN);
      }

      BlockState blockstate = this.cavedweller.level.getBlockState(blockpos$mutableblockpos);
      boolean flag = blockstate.getMaterial().blocksMotion();
      boolean flag1 = blockstate.getFluidState().is(FluidTags.WATER);
      return flag && !flag1;
   }

   public void tickStareClock() {
      --this.currentTicksTillLeave;
      if (this.currentTicksTillLeave < 0.0F) {
         this.shouldLeave = true;
      }

   }

   void tickFleeClock() {
      --this.currentTicksTillFlee;
   }

   public void fleeTick() {
      this.cavedweller.playFleeSound();
      this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
      if (this.ticksUntilNextPathRecalculation <= 0) {
         this.ticksUntilNextPathRecalculation = 2;
         if (!this.cavedweller.getNavigation().moveTo(this.fleeX, this.fleeY, this.fleeZ, this.speedModifier)) {
            this.ticksUntilNextPathRecalculation += 2;
         }

         this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
      }

   }

   public void tick() {
      if (this.shouldLeave && (!this.isPlayerLookingTowards() || !this.inPlayerLineOfSight())) {
         this.cavedweller.discard();
      }

      this.tickFleeClock();
      this.tickStareClock();
      if (this.currentTicksTillFlee <= 0.0F) {
         this.fleeTick();
         this.cavedweller.isFleeing = true;
         this.cavedweller.getEntityData().set(CaveDwellerEntity.FLEEING_ACCESSOR, true);
      } else {
         this.cavedweller.getLookControl().setLookAt(this.cavedweller.getTarget(), 180.0F, 1.0F);
      }

   }
}
