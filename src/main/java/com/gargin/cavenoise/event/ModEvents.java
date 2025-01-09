package com.gargin.cavenoise.event;

import com.gargin.cavenoise.entity.ModEntityTypes;
import com.gargin.cavenoise.entity.custom.CaveDwellerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

public class ModEvents {
   @EventBusSubscriber(
      modid = "cavenoise",
      bus = Bus.MOD
   )
   public static class ModEventBusEvents {
      @SubscribeEvent
      public static void entityAttributeEvent(EntityAttributeCreationEvent event) {
         event.put((EntityType)ModEntityTypes.CAVE_DWELLER.get(), CaveDwellerEntity.setAttributes());
      }
   }
}
