package com.gargin.cavenoise.datagen;

import com.gargin.cavenoise.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
   public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
      super(output, "cavenoise", existingFileHelper);
   }

   protected void registerModels() {
      this.simpleItem(ModItems.BABY_SPIDER);
      this.simpleItem(ModItems.WORM);
      this.withExistingParent(ModItems.CAVE_DWELLER_SPAWN_EGG.getId().getPath(), this.mcLoc("item/template_spawn_egg"));
   }

   private ItemModelBuilder simpleItem(RegistryObject<Item> item) {
      return (ItemModelBuilder)((ItemModelBuilder)this.withExistingParent(item.getId().getPath(), new ResourceLocation("item/generated"))).texture("layer0", new ResourceLocation("cavenoise", "item/" + item.getId().getPath()));
   }
}
