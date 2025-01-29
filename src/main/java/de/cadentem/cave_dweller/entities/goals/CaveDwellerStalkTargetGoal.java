package de.cadentem.cave_dweller.entities.goals;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class CaveDwellerStalkTargetGoal extends NearestAttackableTargetGoal<Player> {
   public CaveDwellerStalkTargetGoal(Mob pMob, Class<Player> pTargetType, boolean pMustSee) {
      super(pMob, pTargetType, pMustSee);
   }
}
