package com.gargin.cavenoise.sound;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {
   public static final DeferredRegister<SoundEvent> SOUND_EVENTS;
   public static final RegistryObject<SoundEvent> CAVENOISE_1;
   public static final RegistryObject<SoundEvent> CAVENOISE_2;
   public static final RegistryObject<SoundEvent> CAVENOISE_3;
   public static final RegistryObject<SoundEvent> CAVENOISE_4;
   public static final RegistryObject<SoundEvent> CHASE_STEP_1;
   public static final RegistryObject<SoundEvent> CHASE_STEP_2;
   public static final RegistryObject<SoundEvent> CHASE_STEP_3;
   public static final RegistryObject<SoundEvent> CHASE_STEP_4;
   public static final RegistryObject<SoundEvent> CHASE_1;
   public static final RegistryObject<SoundEvent> CHASE_2;
   public static final RegistryObject<SoundEvent> CHASE_3;
   public static final RegistryObject<SoundEvent> CHASE_4;
   public static final RegistryObject<SoundEvent> FLEE_1;
   public static final RegistryObject<SoundEvent> FLEE_2;
   public static final RegistryObject<SoundEvent> SPOTTED;
   public static final RegistryObject<SoundEvent> DISAPPEAR;
   public static final RegistryObject<SoundEvent> DWELLER_HURT_1;
   public static final RegistryObject<SoundEvent> DWELLER_HURT_2;
   public static final RegistryObject<SoundEvent> DWELLER_HURT_3;
   public static final RegistryObject<SoundEvent> DWELLER_HURT_4;
   public static final RegistryObject<SoundEvent> DWELLER_DEATH;
   public static final RegistryObject<SoundEvent> DWELLER_STALK_1;
   public static final RegistryObject<SoundEvent> DWELLER_STALK_2;
   public static final RegistryObject<SoundEvent> DWELLER_STALK_3;
   public static final RegistryObject<SoundEvent> DWELLER_STALK_4;
   public static final RegistryObject<SoundEvent> DWELLER_STALK_5;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_1;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_2;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_3;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_4;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_5;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_6;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_7;
   public static final RegistryObject<SoundEvent> DWELLER_CLIMB_8;

   private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
      ResourceLocation id = new ResourceLocation("cavenoise", name);
      return SOUND_EVENTS.register(name, () -> {
         return SoundEvent.createVariableRangeEvent(id);
      });
   }

   public static void register(IEventBus eventBus) {
      SOUND_EVENTS.register(eventBus);
   }

   static {
      SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "cavenoise");
      CAVENOISE_1 = registerSoundEvent("cavenoise_1");
      CAVENOISE_2 = registerSoundEvent("cavenoise_2");
      CAVENOISE_3 = registerSoundEvent("cavenoise_3");
      CAVENOISE_4 = registerSoundEvent("cavenoise_4");
      CHASE_STEP_1 = registerSoundEvent("chase_step_1");
      CHASE_STEP_2 = registerSoundEvent("chase_step_2");
      CHASE_STEP_3 = registerSoundEvent("chase_step_3");
      CHASE_STEP_4 = registerSoundEvent("chase_step_4");
      CHASE_1 = registerSoundEvent("chase_1");
      CHASE_2 = registerSoundEvent("chase_2");
      CHASE_3 = registerSoundEvent("chase_3");
      CHASE_4 = registerSoundEvent("chase_4");
      FLEE_1 = registerSoundEvent("flee_1");
      FLEE_2 = registerSoundEvent("flee_2");
      SPOTTED = registerSoundEvent("spotted");
      DISAPPEAR = registerSoundEvent("disappear");
      DWELLER_HURT_1 = registerSoundEvent("dweller_hurt_1");
      DWELLER_HURT_2 = registerSoundEvent("dweller_hurt_2");
      DWELLER_HURT_3 = registerSoundEvent("dweller_hurt_3");
      DWELLER_HURT_4 = registerSoundEvent("dweller_hurt_4");
      DWELLER_DEATH = registerSoundEvent("dweller_death");
      DWELLER_STALK_1 = registerSoundEvent("stalk_1");
      DWELLER_STALK_2 = registerSoundEvent("stalk_2");
      DWELLER_STALK_3 = registerSoundEvent("stalk_3");
      DWELLER_STALK_4 = registerSoundEvent("stalk_4");
      DWELLER_STALK_5 = registerSoundEvent("stalk_5");
      DWELLER_CLIMB_1 = registerSoundEvent("climb_1");
      DWELLER_CLIMB_2 = registerSoundEvent("climb_2");
      DWELLER_CLIMB_3 = registerSoundEvent("climb_3");
      DWELLER_CLIMB_4 = registerSoundEvent("climb_4");
      DWELLER_CLIMB_5 = registerSoundEvent("climb_5");
      DWELLER_CLIMB_6 = registerSoundEvent("climb_6");
      DWELLER_CLIMB_7 = registerSoundEvent("climb_7");
      DWELLER_CLIMB_8 = registerSoundEvent("climb_8");
   }
}
