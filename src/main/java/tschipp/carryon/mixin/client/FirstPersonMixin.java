package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.render.BlockRendererLayer;
import tschipp.carryon.render.EntityRendererLayer;

@Mixin(ItemRenderer.class)
public class FirstPersonMixin {

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void onRenderItem(float partialTicks, CallbackInfo info)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;

        ItemStack stack = mc.thePlayer.getHeldItemStack();
        if (stack == null) return;

        if (stack.getItem() == CarryOnEvents.TILE_ITEM)
        {
            info.cancel();
            BlockRendererLayer.renderFirstPerson(mc.thePlayer, stack, partialTicks);
        }
        else if (stack.getItem() == CarryOnEvents.ENTITY_ITEM)
        {
            info.cancel();
            EntityRendererLayer.renderFirstPerson(mc.thePlayer, stack, partialTicks);
        }
    }

}