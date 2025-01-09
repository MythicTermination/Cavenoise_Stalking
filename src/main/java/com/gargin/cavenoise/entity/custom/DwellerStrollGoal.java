package com.gargin.cavenoise.entity.custom;

import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class DwellerStrollGoal extends WaterAvoidingRandomStrollGoal {
   private CaveDwellerEntity cavedweller;

   public DwellerStrollGoal(CaveDwellerEntity pMob, double pSpeedModifier) {
      super(pMob, pSpeedModifier);
      this.cavedweller = pMob;
   }

   public boolean canUse() {
      return super.canUse() && this.cavedweller.rRollResult == 4 && !this.cavedweller.forcedStalk;
   }

   public boolean canContinueToUse() {
      return super.canContinueToUse() && this.cavedweller.rRollResult == 4;
   }
}
