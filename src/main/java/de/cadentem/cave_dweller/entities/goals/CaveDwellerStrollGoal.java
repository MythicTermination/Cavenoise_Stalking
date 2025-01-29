package de.cadentem.cave_dweller.entities.goals;

import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class CaveDwellerStrollGoal extends WaterAvoidingRandomStrollGoal {
    private final CaveDwellerEntity caveDweller;
    public CaveDwellerStrollGoal(final CaveDwellerEntity mob, double speedModifier) {
        super(mob, speedModifier);
        this.caveDweller = mob;
    }

    @Override
    public boolean canUse() {
        return this.caveDweller.currentRoll == Roll.STROLL && super.canUse() && !this.caveDweller.forcedStalk;
    }

    @Override
    public boolean canContinueToUse() {
        return this.caveDweller.currentRoll == Roll.STROLL && super.canContinueToUse();
    }
}
