package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.render.ICarrying;

@Mixin(RenderPlayer.class)
public abstract class ArmorRendererMixin {

    @Shadow private ModelBiped modelArmorChestplate;
    @Shadow private ModelBiped modelArmor;

    @Inject(method = "setArmorModel", at = @At("RETURN"))
    private void onSetArmorModel(AbstractClientPlayer player, int pass, float partialTick, CallbackInfoReturnable<Integer> info)
    {
        ItemStack stack = player.getHeldItemStack();

        boolean carryBlock = stack != null && stack.getItem() == CarryOnEvents.TILE_ITEM;
        boolean carryEntity = stack != null && stack.getItem() == CarryOnEvents.ENTITY_ITEM;

        if (modelArmorChestplate instanceof ICarrying carrying) {
            carrying.setCarryingBlock(carryBlock); carrying.setCarryingEntity(carryEntity);
        }
        if (modelArmor           instanceof ICarrying c) {
            c.setCarryingBlock(carryBlock); c.setCarryingEntity(carryEntity);
        }
    }
}