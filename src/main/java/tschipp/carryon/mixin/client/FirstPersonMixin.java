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
 * Injects into ItemRenderer.renderItemInFirstPerson to render the carried
 * block/entity in first-person view.
 */
@Mixin(ItemRenderer.class)
public class FirstPersonMixin {

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"))
    public void onRenderItem(float partialTicks, CallbackInfo info) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) return;

        EntityPlayer player = mc.thePlayer;
        ItemStack stack = player.getHeldItemStack();
        if (stack == null) return;

        if (stack.getItem() == CarryOnEvents.TILE_ITEM)
            BlockRendererLayer.renderFirstPerson(player, stack, partialTicks);
        else if (stack.getItem() == CarryOnEvents.ENTITY_ITEM)
            EntityRendererLayer.renderFirstPerson(player, stack, partialTicks);
    }

}