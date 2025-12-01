package com.siesque.tfmgcastingfix.mixin;

import com.drmangotea.tfmg.content.machinery.metallurgy.casting_basin.CastingBasinBlockEntity;
import com.drmangotea.tfmg.content.machinery.metallurgy.casting_basin.CastingBasinRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.createmod.catnip.animation.LerpedFloat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CastingBasinRenderer.class)
public abstract class CastingBasinRendererMixin {
    @Unique
    private static final ThreadLocal<CastingBasinBlockEntity> CURRENT_BE = new ThreadLocal<>();

    @Inject(
            method = "renderSafe(Lcom/drmangotea/tfmg/content/machinery/metallurgy/casting_basin/CastingBasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            remap = false,
            at = @At("HEAD")
    )
    private void onRenderHead(CastingBasinBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        CURRENT_BE.set(be);
    }

    @Inject(
            method = "renderSafe(Lcom/drmangotea/tfmg/content/machinery/metallurgy/casting_basin/CastingBasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            remap = false,
            at = @At("RETURN")
    )
    private void onRenderReturn(CastingBasinBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay, CallbackInfo ci) {
        CURRENT_BE.remove();
    }

    @ModifyConstant(
            method = "renderSafe(Lcom/drmangotea/tfmg/content/machinery/metallurgy/casting_basin/CastingBasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            remap = false,
            constant = @Constant(floatValue = 400.0f)
    )
    private float replaceMaxProgress(float constant) {
        CastingBasinBlockEntity be = CURRENT_BE.get();

        try {
            if (be.recipe != null) {
                int required = be.recipe.getIngrenient().getRequiredAmount();
                return required * 2.77f; // approximate scaling factor to match original visual progress
            }

            return constant;
        } catch (Throwable t) {
            return constant;
        }
    }

    @Redirect(method = "renderSafe(Lcom/drmangotea/tfmg/content/machinery/metallurgy/casting_basin/CastingBasinBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            remap = false, at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/animation/LerpedFloat;getValue(F)F"))
    private float redirectGetValue(LerpedFloat instance, float v) {
        CastingBasinBlockEntity be = CURRENT_BE.get();

        try {
            if (be.recipe != null) {
                int required = be.recipe.getIngrenient().getRequiredAmount();
                return Math.min(instance.getValue(v), required);
            }

            return instance.getValue(v);
        } catch (Throwable t) {
            return instance.getValue(v);
        }
    }
}
