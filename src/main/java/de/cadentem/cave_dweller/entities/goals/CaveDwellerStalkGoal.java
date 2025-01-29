package de.cadentem.cave_dweller.entities.goals;

import java.util.Random;

import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public class CaveDwellerStalkGoal extends Goal {
   private final CaveDwellerEntity caveDweller;
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

   public CaveDwellerStalkGoal(CaveDwellerEntity pCaveDweller, double pSpeedModifier, float pDistanceForAggro) {
      this.distanceForAggro = pDistanceForAggro;
      this.caveDweller = pCaveDweller;
      this.speedModifier = pSpeedModifier;
   }

   public boolean canUse() {
      if (this.caveDweller.isInvisible()) {
         return false;
      } else {
         if (this.caveDweller.getTarget() == null) {
            this.stalkingTarget = this.getTargetToStalk();
         } else {
            this.stalkingTarget = this.caveDweller.getTarget();
         }

         return this.stalkingTarget != null && (this.caveDweller.currentRoll.rollValue == 3 || this.caveDweller.forcedStalk);
      }
   }

   public boolean canContinueToUse() {
      if (this.caveDweller.isInvisible()) {
         return false;
      } else {
         return this.stalkingTarget != null && (this.caveDweller.currentRoll.rollValue == 3 || this.caveDweller.forcedStalk);
      }
   }

   public void switchToAggroIfPlayerInRange() {
      if (this.stalkingTarget.distanceTo(this.caveDweller) < this.distanceForAggro && this.caveDweller.inPlayerLineOfSight() && this.caveDweller.isPlayerLookingTowards()) {
         this.caveDweller.currentRoll.rollValue = 0;
         this.caveDweller.forcedStalk = false;
      }

   }

   public void start() {
      this.caveDweller.getEntityData().set(CaveDwellerEntity.STALKING_ACCESSOR, true);
      this.ticksTillFlip = this.minTicksTillFlip + this.rand.nextInt(this.maxTicksTillFlip - this.minTicksTillFlip);
      super.start();
   }

   public void stop() {
      this.caveDweller.getEntityData().set(CaveDwellerEntity.STALKING_ACCESSOR, false);
      this.caveDweller.getNavigation().stop();
      super.stop();
   }

   private LivingEntity getTargetToStalk() {
      return this.caveDweller.level.getNearestPlayer(this.caveDweller, 200.0D);
   }

   public void tick() {
      this.switchToAggroIfPlayerInRange();
      LivingEntity livingentity = this.stalkingTarget;
      if (livingentity != null) {
         this.caveDweller.getLookControl().setLookAt(livingentity, 30.0F, 30.0F);
         double d0 = this.caveDweller.getMeleeAttackRangeSqr(livingentity);
         this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);
         if ((this.followingTargetEvenIfNotSeen || this.caveDweller.getSensing().hasLineOfSight(livingentity)) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || livingentity.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.caveDweller.getRandom().nextFloat() < 0.05F)) {
            this.pathedTargetX = livingentity.getX();
            this.pathedTargetY = livingentity.getY();
            this.pathedTargetZ = livingentity.getZ();
            this.ticksUntilNextPathRecalculation = 4 + this.caveDweller.getRandom().nextInt(7);
            if (this.canPenalize) {
               this.ticksUntilNextPathRecalculation += this.failedPathFindingPenalty;
               if (this.caveDweller.getNavigation().getPath() != null) {
                  Node finalPathPoint = this.caveDweller.getNavigation().getPath().getEndNode();
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

            if (!this.caveDweller.getNavigation().moveTo(livingentity, this.speedModifier)) {
               this.ticksUntilNextPathRecalculation += 15;
            }

            this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
         }
      }

      if (this.caveDweller.currentRoll.rollValue == 3) {
         ++this.flipClock;
         if (this.flipClock > this.ticksTillFlip) {
            this.flipToAggroOrFlee();
         }
      }

   }

   private void flipToAggroOrFlee() {
      if (this.rand.nextBoolean()) {
         this.caveDweller.currentRoll.rollValue = 0;
      } else {
         this.caveDweller.currentRoll.rollValue = 2;
      }

   }
}
