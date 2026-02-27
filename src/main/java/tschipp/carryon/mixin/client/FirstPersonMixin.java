package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.render.BlockRendererLayer;
import tschipp.carryon.render.EntityRendererLayer;

/**
 * Injects into ItemRenderer.renderItemInFirstPerson.
 *
 * When holding a carry item, we cancel the default sprite rendering
 * (which would show garbage icons) and render our own block/entity
 * via BlockRendererLayer / EntityRendererLayer.
 */
@Mixin(ItemRenderer.class)
public class FirstPersonMixin {

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(float partialTicks, CallbackInfo info) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;

        ItemStack stack = mc.thePlayer.getHeldItemStack();
        if (stack == null) return;

        if (stack.getItem() == CarryOnEvents.TILE_ITEM) {
            info.cancel(); // suppress default sprite rendering
            BlockRendererLayer.renderFirstPerson(mc.thePlayer, stack, partialTicks);
        } else if (stack.getItem() == CarryOnEvents.ENTITY_ITEM) {
            info.cancel(); // suppress default sprite rendering
            EntityRendererLayer.renderFirstPerson(mc.thePlayer, stack, partialTicks);
        }
    }

}