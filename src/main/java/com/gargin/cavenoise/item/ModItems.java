package com.gargin.cavenoise.item;

import com.gargin.cavenoise.entity.ModEntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
   public static final DeferredRegister<Item> ITEMS;
   public static final RegistryObject<Item> WORM;
   public static final RegistryObject<Item> BABY_SPIDER;
   public static final RegistryObject<Item> CAVE_DWELLER_SPAWN_EGG;

   public static void register(IEventBus eventBus) {
      ITEMS.register(eventBus);
   }

   static {
      ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "cavenoise");
      WORM = ITEMS.register("worm", () -> {
         return new Item(new Properties());
      });
      BABY_SPIDER = ITEMS.register("baby_spider", () -> {
         return new Item(new Properties());
      });
      CAVE_DWELLER_SPAWN_EGG = ITEMS.register("cave_dweller_spawn_egg", () -> {
         return new ForgeSpawnEggItem(ModEntityTypes.CAVE_DWELLER, 12895428, 790333, new Properties());
      });
   }
}
