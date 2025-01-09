package com.gargin.cavenoise.entity.custom;

import java.util.Random;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public class DwellerStalkGoal extends Goal {
   private final CaveDwellerEntity cavedweller;
   private double speedModifier;
   private int minTicksTillFlip = 400;
   private int maxTicksTillFlip = 600;
   private int ticksTillFlip;
   private int flipClock = 0;
   private Path path;
   private double pathedTargetX;
   private double pathedTargetY;
   private double pathedTargetZ;
   private int ticksUntilNextPathRecalculation;
   private int ticksUntilNextAttack;
   private int failedPathFindingPenalty = 0;
   private boolean followingTargetEvenIfNotSeen = true;
   private boolean canPenalize = true;
   private float distanceForAggro = 15.0F;
   private LivingEntity stalkingTarget;
   Random rand = new Random();

   public DwellerStalkGoal(CaveDwellerEntity pCaveDweller, double pSpeedModifier, float pDistanceForAggro) {
      this.distanceForAggro = pDistanceForAggro;
      this.cavedweller = pCaveDweller;
      this.speedModifier = pSpeedModifier;
   }

   public boolean canUse() {
      if (this.cavedweller.isInvisible()) {
         return false;
      } else {
         if (this.cavedweller.getTarget() == null) {
            this.stalkingTarget = this.getTargetToStalk();
         } else {
            this.stalkingTarget = this.cavedweller.getTarget();
         }

         return this.stalkingTarget == null ? false : this.cavedweller.rRollResult == 3 || this.cavedweller.forcedStalk;
      }
   }

   public boolean canContinueToUse() {
      if (this.cavedweller.isInvisible()) {
         return false;
      } else {
         return this.stalkingTarget == null ? false : this.cavedweller.rRollResult == 3 || this.cavedweller.forcedStalk;
      }
   }

   public void switchToAggroIfPlayerInRange() {
      if (this.stalkingTarget.distanceTo(this.cavedweller) < this.distanceForAggro && this.cavedweller.inPlayerLineOfSight() && this.cavedweller.isPlayerLookingTowards()) {
         this.cavedweller.rRollResult = 0;
         this.cavedweller.forcedStalk = false;
      }

   }

   public void start() {
      this.cavedweller.getEntityData().set(CaveDwellerEntity.STALKING_ACCESSOR, true);
      this.ticksTillFlip = this.minTicksTillFlip + this.rand.nextInt(this.maxTicksTillFlip - this.minTicksTillFlip);
      super.start();
   }

   public void stop() {
      this.cavedweller.getEntityData().set(CaveDwellerEntity.STALKING_ACCESSOR, false);
      this.cavedweller.getNavigation().stop();
      super.stop();
   }

   private LivingEntity getTargetToStalk() {
      return this.cavedweller.level().getNearestPlayer(this.cavedweller, 200.0D);
   }

   public void tick() {
      this.switchToAggroIfPlayerInRange();
      LivingEntity livingentity = this.stalkingTarget;
      if (livingentity != null) {
         this.cavedweller.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
         double d0 = this.cavedweller.getPerceivedTargetDistanceSquareForMeleeAttack(livingentity);
         this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
         if ((this.followingTargetEvenIfNotSeen || this.cavedweller.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.cavedweller.getRandom().nextFloat() < 0.05F)) {
            this.pathedTargetX = livingentity.getX();
            this.pathedTargetY = livingentity.getY();
            this.pathedTargetZ = livingentity.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.cavedweller.getRandom().nextInt(7);
            if (this.canPenalize) {
               this.ticksUntilNextPathRecalculation += this.failedPathFindingPenalty;
               if (this.cavedweller.getNavigation().getPath() != null) {
                  Node finalPathPoint = this.cavedweller.getNavigation().getPath().getEndNode();
                  if (finalPathPoint != null && livingentity.distanceToSqr((double)finalPathPoint.x, (double)finalPathPoint.y, (double)finalPathPoint.z) < 1.0D) {
                     this.failedPathFindingPenalty = 0;
                  } else {
                     this.failedPathFindingPenalty += 10;
                  }
               } else {
                  this.failedPathFindingPenalty += 10;
               }
            }

            if (d0 > 1024.0D) {
               this.ticksUntilNextPathRecalculation += 10;
            } else if (d0 > 256.0D) {
               this.ticksUntilNextPathRecalculation += 5;
            }

            if (!this.cavedweller.getNavigation().moveTo(livingentity, this.speedModifier)) {
               this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
         }
      }

      if (this.cavedweller.rRollResult == 3) {
         ++this.flipClock;
         if (this.flipClock > this.ticksTillFlip) {
            this.flipToAggroOrFlee();
         }
      }

   }

   private void flipToAggroOrFlee() {
      if (this.rand.nextBoolean()) {
         this.cavedweller.rRollResult = 0;
      } else {
         this.cavedweller.rRollResult = 2;
      }

   }
}
