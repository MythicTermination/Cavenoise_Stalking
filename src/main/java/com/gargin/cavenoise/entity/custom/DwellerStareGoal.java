package com.gargin.cavenoise.entity.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class DwellerStareGoal extends Goal {
   private final CaveDwellerEntity cavedweller;
   private final float ticksTillLeave;
   private float currentTicksTillLeave;
   private boolean shouldLeave;

   public DwellerStareGoal(CaveDwellerEntity pCaveDweller, float pTicksTillLeave) {
      this.cavedweller = pCaveDweller;
      this.ticksTillLeave = pTicksTillLeave;
      this.currentTicksTillLeave = pTicksTillLeave;
   }

   public boolean canUse() {
      if (this.cavedweller.isInvisible()) {
         return false;
      } else {
         return this.cavedweller.getTarget() == null ? false : this.cavedweller.rRollResult == 1 && !this.cavedweller.forcedStalk;
      }
   }

   public boolean canContinueToUse() {
      return this.cavedweller.getTarget() == null ? false : this.cavedweller.rRollResult == 1 && !this.cavedweller.forcedStalk;
   }

   public void start() {
      this.shouldLeave = false;
   }

   public void stop() {
   }

   public void tickStareClock() {
      --this.currentTicksTillLeave;
      if (this.currentTicksTillLeave <= 0.0F) {
         this.shouldLeave = true;
      }

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
      LivingEntity pendingTarget = this.cavedweller.getTarget();
      return pendingTarget != null ? pendingTarget.hasLineOfSight(this.cavedweller) : false;
   }

   public double loopAngle(double angle) {
      double var4;
      if (angle > 360.0D) {
         return var4 = angle - 360.0D;
      } else {
         return angle < 0.0D ? (var4 = angle + 360.0D) : angle;
      }
   }

   public void tick() {
      this.tickStareClock();
      if (this.shouldLeave && (!this.isPlayerLookingTowards() || !this.inPlayerLineOfSight())) {
         this.cavedweller.playDisappearSound();
         this.cavedweller.discard();
      }

      this.cavedweller.getLookControl().setLookAt(this.cavedweller.getTarget(), 180.0F, 1.0F);
   }
}
