package com.gargin.cavenoise.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.gargin.cavenoise.entity.custom.CaveDwellerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class CaveDwellerRenderer extends GeoEntityRenderer<CaveDwellerEntity> {
   private GeoRenderLayer eyesRenderLayer;

   public CaveDwellerRenderer(Context renderManager) {
      super(renderManager, new CaveDwellerModel());
      this.shadowRadius = 0.3F;
      this.eyesRenderLayer = new CaveDwellerEyesLayer(this);
      this.addRenderLayer(this.eyesRenderLayer);
   }

   public ResourceLocation getTextureLocation(CaveDwellerEntity instance) {
      return new ResourceLocation("cavenoise", "textures/entity/cave_dweller_texture.png");
   }

   public void render(CaveDwellerEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
      if (entity.isBaby()) {
         poseStack.scale(0.1F, 0.1F, 0.1F);
      } else {
         poseStack.scale(1.3F, 1.3F, 1.3F);
      }

      super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
   }
}
