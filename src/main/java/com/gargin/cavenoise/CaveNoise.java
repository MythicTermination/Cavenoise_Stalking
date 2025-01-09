package com.gargin.cavenoise;

import com.mojang.logging.LogUtils;
import com.gargin.cavenoise.entity.ModEntityTypes;
import com.gargin.cavenoise.entity.client.CaveDwellerRenderer;
import com.gargin.cavenoise.entity.custom.CaveDwellerEntity;
import com.gargin.cavenoise.item.ModItems;
import com.gargin.cavenoise.sound.CaveSoundInstance;
import com.gargin.cavenoise.sound.ModSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import software.bernie.geckolib.GeckoLib;

@Mod("cavenoise")
public class CaveNoise {
   public static final String MODID = "cavenoise";
   private static final Logger LOGGER = LogUtils.getLogger();
   private final boolean USING_FAST_TIMERS = false;
   private final boolean SPEED_ALL_CLOCKS = false;
   private final float SPEED_MOD = 5.0F;
   private float currentSpeedMod = 1.0F;
   private int creepyCaveNoiseStart = 8000;
   private int creepyCaveNoiseEndBuild = 1000;
   private float creepyCaveNoiseMinVol = 0.1F;
   private float creepyCaveNoiseMaxVol = 1.0F;
   private int vanillaCaveNoiseStartBuild = 15000;
   private int vanillaCaveNoiseEndBuild = 2000;
   private int vanillaCaveNoiseStartMinTime = 8000;
   private int vanillaCaveNoiseStartMaxTime = 10000;
   private int vanillaCaveNoiseEndMinTime = 4000;
   private int vanillaCaveNoiseEndMaxTime = 6000;
   private int stalkNoiseMinTime = 800;
   private int stalkNoiseMaxTime = 1000;
   private int ticksCalmResetMin;
   private int ticksCalmResetMax;
   private int ticksCalmResetCooldown;
   private int ticksNoiseResetMin;
   private int ticksNoiseResetMax;
   private int calmTimer = 0;
   private int noiseTimer = 0;
   private int stalkNoiseTimer = 0;
   private int vanillaNoiseTimer = 0;
   private boolean canSpawn = false;
   private double chanceToSpawnPerTick = 0.005D;
   private double chanceToCooldown = 0.4D;
   private boolean anySpelunkers = false;
   private List<Player> spelunkers = new ArrayList();
   private List<ServerPlayer> players = new ArrayList();

   public CaveNoise() {
      IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
      modEventBus.addListener(this::commonSetup);
      ModItems.register(modEventBus);
      ModEntityTypes.register(modEventBus);
      MinecraftForge.EVENT_BUS.register(this);
      modEventBus.addListener(this::addCreative);
      GeckoLib.initialize();
      ModSounds.register(modEventBus);
      this.ticksCalmResetMin = 15000;
      this.ticksCalmResetMax = 18000;
      this.ticksCalmResetCooldown = 16000;
      this.ticksNoiseResetMin = 2000;
      this.ticksNoiseResetMax = 1600;
      this.calmTimer = 25000;
      this.noiseTimer = 4800;
   }

   private void commonSetup(FMLCommonSetupEvent event) {
      LOGGER.info("HELLO FROM COMMON SETUP");
      LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
   }

   private void addCreative(BuildCreativeModeTabContentsEvent event) {
      if (event.getTabKey() != CreativeModeTabs.INGREDIENTS && event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
         event.accept(ModItems.CAVE_DWELLER_SPAWN_EGG);
      }

   }

   @SubscribeEvent
   public void onServerStarting(ServerStartingEvent event) {
      LOGGER.info("HELLO from server starting");
      this.resetCalmTimer();
   }

   @SubscribeEvent
   public void serverTick(ServerTickEvent event) {
      Iterable<Entity> entities = event.getServer().getLevel(Level.OVERWORLD).getEntities().getAll();
      AtomicBoolean dwellerExists = new AtomicBoolean(false);
      entities.forEach((entity) -> {
         if (entity instanceof CaveDwellerEntity) {
            dwellerExists.set(true);
            this.resetCalmTimer();
         }

      });
      this.noiseTimer -= (int)(1.0F * this.currentSpeedMod);
      this.vanillaNoiseTimer -= (int)(1.0F * this.currentSpeedMod);
      this.stalkNoiseTimer -= (int)(1.0F * this.currentSpeedMod);
      if (!dwellerExists.get()) {
         if (this.noiseTimer <= 0 && this.calmTimer <= this.creepyCaveNoiseStart) {
            event.getServer().getLevel(Level.OVERWORLD).getPlayers(this::playCaveSoundToSpelunkers);
         }

         if (this.vanillaNoiseTimer <= 0 && this.calmTimer <= this.vanillaCaveNoiseStartBuild) {
            event.getServer().getLevel(Level.OVERWORLD).getPlayers(this::playVanillaCaveSoundToSpelunkers);
         }
      } else if (this.stalkNoiseTimer <= 0) {
         event.getServer().getLevel(Level.OVERWORLD).getPlayers(this::playStalkSoundToSpelunkers);
      }

      if (this.calmTimer <= 0) {
         this.canSpawn = true;
      } else {
         this.canSpawn = false;
      }

      this.calmTimer -= (int)(1.0F * this.currentSpeedMod);
      if (this.canSpawn && !dwellerExists.get()) {
         Random rand = new Random();
         if (rand.nextDouble() <= this.chanceToSpawnPerTick) {
            this.spelunkers.clear();
            this.anySpelunkers = false;
            event.getServer().getLevel(Level.OVERWORLD).getPlayers(this::listSpelunkers);
            if (this.anySpelunkers) {
               Player victim = (Player)this.spelunkers.get(rand.nextInt(this.spelunkers.size()));
               event.getServer().getLevel(Level.OVERWORLD).getPlayers(this::playCaveSoundToSpelunkers);
               CaveDwellerEntity cavedweller = new CaveDwellerEntity((EntityType)ModEntityTypes.CAVE_DWELLER.get(), event.getServer().getLevel(Level.OVERWORLD));
               cavedweller.setInvisible(true);
               System.out.println("SPAWNED CD");
               cavedweller.setPos(cavedweller.generatePos(victim));
               System.out.println("POS: " + cavedweller.position());
               System.out.println("ADDED SUCCESSFULLY: " + event.getServer().getLevel(Level.OVERWORLD).addFreshEntity(cavedweller));
               this.resetCalmTimer();
            }
         }
      }

   }

   public boolean listSpelunkers(ServerPlayer player) {
      if (this.checkIfPlayerIsSpelunker(player)) {
         this.anySpelunkers = true;
         this.spelunkers.add(player);
      }

      return true;
   }

   public boolean playCaveSoundToSpelunkers(ServerPlayer player) {
      float a = (float)((this.calmTimer - this.creepyCaveNoiseEndBuild) / (this.creepyCaveNoiseStart - this.creepyCaveNoiseEndBuild));
      float b = 1.0F - a;
      b = Math.max(0.0F, b);
      b = Math.min(1.0F, b);
      float vol = this.creepyCaveNoiseMinVol + (this.creepyCaveNoiseMaxVol - this.creepyCaveNoiseMinVol) * b;
      Random rand = new Random();
      Level level = player.level();
      BlockPos playerBlockPos = new BlockPos((int)Math.floor(player.position().x), (int)Math.floor(player.position().y), (int)Math.floor(player.position().z));
      if (this.checkIfPlayerIsSpelunker(player) && !player.isCreative() && !player.isSpectator()) {
         switch(rand.nextInt(4)) {
         case 0:
            Minecraft.getInstance().getSoundManager().play(new CaveSoundInstance((SoundEvent)ModSounds.CAVENOISE_1.get(), vol, playerBlockPos));
            break;
         case 1:
            Minecraft.getInstance().getSoundManager().play(new CaveSoundInstance((SoundEvent)ModSounds.CAVENOISE_2.get(), vol, playerBlockPos));
            break;
         case 2:
            Minecraft.getInstance().getSoundManager().play(new CaveSoundInstance((SoundEvent)ModSounds.CAVENOISE_3.get(), vol, playerBlockPos));
            break;
         case 3:
            Minecraft.getInstance().getSoundManager().play(new CaveSoundInstance((SoundEvent)ModSounds.CAVENOISE_4.get(), vol, playerBlockPos));
         }

         this.resetNoiseTimer();
      }

      return true;
   }

   private void resetNoiseTimer() {
      Random rand = new Random();
      this.noiseTimer = this.ticksNoiseResetMin + rand.nextInt(this.ticksNoiseResetMax);
   }

   public boolean playVanillaCaveSoundToSpelunkers(ServerPlayer player) {
      Level level = player.level();
      BlockPos playerBlockPos = new BlockPos((int)Math.floor(player.position().x), (int)Math.floor(player.position().y), (int)Math.floor(player.position().z));
      if (this.checkIfPlayerIsSpelunker(player) && !player.isCreative() && !player.isSpectator()) {
         Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance((SoundEvent)SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0F, 1.0F, RandomSource.create(), playerBlockPos));
         this.resetVanillaNoiseTimer();
      }

      return true;
   }

   private void resetVanillaNoiseTimer() {
      float a = (float)((this.calmTimer - this.vanillaCaveNoiseEndBuild) / (this.vanillaCaveNoiseStartBuild - this.vanillaCaveNoiseEndBuild));
      a = Math.max(0.0F, a);
      a = Math.min(1.0F, a);
      float b = 1.0F - a;
      int newMin = Math.round((float)(this.vanillaCaveNoiseEndMinTime - this.vanillaCaveNoiseStartMinTime) * b + (float)this.vanillaCaveNoiseStartMinTime);
      int newMax = Math.round((float)(this.vanillaCaveNoiseEndMaxTime - this.vanillaCaveNoiseStartMaxTime) * b + (float)this.vanillaCaveNoiseStartMaxTime);
      Random rand = new Random();
      this.vanillaNoiseTimer = rand.nextInt(newMax - newMin) + newMin;
   }

   private boolean playStalkSoundToSpelunkers(ServerPlayer player) {
      Random rand = new Random();
      BlockPos playerBlockPos = new BlockPos((int)Math.floor(player.position().x + (double)(-25 + rand.nextInt(50))), (int)Math.floor(player.position().y), (int)Math.floor(player.position().z + (double)(-25 + rand.nextInt(50))));
      if (this.checkIfPlayerIsSpelunker(player) && !player.isCreative() && !player.isSpectator()) {
         switch(rand.nextInt(5)) {
         case 0:
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance((SoundEvent)ModSounds.DWELLER_STALK_1.get(), SoundSource.AMBIENT, 2.0F, 1.0F, RandomSource.create(), playerBlockPos));
            break;
         case 1:
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance((SoundEvent)ModSounds.DWELLER_STALK_2.get(), SoundSource.AMBIENT, 2.0F, 1.0F, RandomSource.create(), playerBlockPos));
            break;
         case 2:
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance((SoundEvent)ModSounds.DWELLER_STALK_3.get(), SoundSource.AMBIENT, 2.0F, 1.0F, RandomSource.create(), playerBlockPos));
            break;
         case 3:
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance((SoundEvent)ModSounds.DWELLER_STALK_4.get(), SoundSource.AMBIENT, 2.0F, 1.0F, RandomSource.create(), playerBlockPos));
            break;
         case 4:
            Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance((SoundEvent)ModSounds.DWELLER_STALK_5.get(), SoundSource.AMBIENT, 2.0F, 1.0F, RandomSource.create(), playerBlockPos));
         }

         this.resetStalkNoiseTimer();
      }

      return true;
   }

   private void resetStalkNoiseTimer() {
      Random rand = new Random();
      this.stalkNoiseTimer = this.stalkNoiseMinTime + rand.nextInt(this.stalkNoiseMaxTime - this.stalkNoiseMinTime);
   }

   public boolean checkIfPlayerIsSpelunker(Player player) {
      if (player == null) {
         return false;
      } else {
         Level level = player.level();
         BlockPos playerBlockPos = new BlockPos((int)Math.floor(player.position().x), (int)Math.floor(player.position().y), (int)Math.floor(player.position().z));
         return player.position().y < 40.0D && !level.canSeeSky(playerBlockPos);
      }
   }

   private void resetCalmTimer() {
      Random rand = new Random();
      this.calmTimer = this.ticksCalmResetMin + rand.nextInt(this.ticksCalmResetMax);
      if (rand.nextDouble() <= this.chanceToCooldown) {
         this.calmTimer = this.ticksCalmResetCooldown + rand.nextInt(this.ticksCalmResetCooldown);
      }

   }

   @SubscribeEvent
   public void livingKnockbackEvent(LivingKnockBackEvent event) {
      if (event.getEntity() instanceof CaveDwellerEntity) {
         event.setStrength(0.0F);
      }

   }

   @SubscribeEvent
   public void mobDespawn(EntityLeaveLevelEvent event) {
      if (event.getEntity() instanceof CaveDwellerEntity) {
         System.out.println(event.toString());
      }

   }

   @EventBusSubscriber(
      modid = "cavenoise",
      bus = Bus.FORGE,
      value = {Dist.CLIENT}
   )
   public static class RegisterLayers {
      @SubscribeEvent
      public static void registerLayer(EntityRenderersEvent event) {
      }
   }

   @EventBusSubscriber(
      modid = "cavenoise",
      bus = Bus.MOD,
      value = {Dist.CLIENT}
   )
   public static class ClientModEvents {
      @SubscribeEvent
      public static void onClientSetup(FMLClientSetupEvent event) {
         CaveNoise.LOGGER.info("HELLO FROM CLIENT SETUP");
         CaveNoise.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
         EntityRenderers.register((EntityType)ModEntityTypes.CAVE_DWELLER.get(), CaveDwellerRenderer::new);
      }
   }
}
