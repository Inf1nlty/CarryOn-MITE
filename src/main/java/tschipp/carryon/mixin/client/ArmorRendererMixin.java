package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.render.ICarrying;

/**
 * Ensures the armor biped models also have correct ICarrying state.
 * In MITE 1.6.4, armor is rendered through RenderPlayer using separate
 * ModelBiped instances (modelArmorChestplate, modelArmor).
 * We hook into RenderPlayer.setArmorModel to sync the ICarrying state.
 */
@Mixin(RenderPlayer.class)
public abstract class ArmorRendererMixin {

    @Shadow
    private ModelBiped modelArmorChestplate;

    @Shadow
    private ModelBiped modelArmor;

    @Inject(method = "setArmorModel", at = @At("RETURN"))
    private void onSetArmorModel(AbstractClientPlayer player, int pass, float partialTick, CallbackInfoReturnable<Integer> info) {
        ItemStack stack = player.getHeldItemStack();
        boolean isCarryingBlock = stack != null && stack.getItem() == CarryOnEvents.TILE_ITEM;
        boolean isCarryingEntity = stack != null && stack.getItem() == CarryOnEvents.ENTITY_ITEM;

        if (modelArmorChestplate instanceof ICarrying) {
            ((ICarrying) modelArmorChestplate).setCarryingBlock(isCarryingBlock);
            ((ICarrying) modelArmorChestplate).setCarryingEntity(isCarryingEntity);
        }
        if (modelArmor instanceof ICarrying) {
            ((ICarrying) modelArmor).setCarryingBlock(isCarryingBlock);
            ((ICarrying) modelArmor).setCarryingEntity(isCarryingEntity);
        }
    }

}