package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.items.ItemTile;

/**
 * Injects into ItemRenderer.renderItem to set render_icon_override
 * so that when a carry item is rendered as a sprite (inventory, hotbar,
 * third-person held item), it shows the actual carried block's icon
 * instead of garbage/missing texture.
 *
 * render_icon_override is a static field in ItemRenderer checked inside
 * renderItem() before using the item's own icon.
 */
@Mixin(ItemRenderer.class)
public class ItemIconOverrideMixin {

    @Inject(method = "renderItem", at = @At("HEAD"))
    public void onRenderItemHead(EntityLivingBase entity, ItemStack stack, int pass, CallbackInfo ci) {
        if (stack == null) return;
        if (stack.getItem() == CarryOnEvents.TILE_ITEM) {
            Block block = ItemTile.getBlock(stack);
            if (block != null && block.blockID != 0) {
                Icon icon = block.getIcon(1, ItemTile.getMeta(stack)); // top face
                if (icon != null) {
                    ItemRenderer.render_icon_override = icon;
                }
            }
        }
        // For ENTITY_ITEM: leave icon as-is (our own transparent PNG),
        // since entities are rendered via EntityRendererLayer not as sprites.
    }

    @Inject(method = "renderItem", at = @At("RETURN"))
    public void onRenderItemReturn(EntityLivingBase entity, ItemStack stack, int pass, CallbackInfo ci) {
        // Always clear the override after rendering
        if (stack != null && stack.getItem() == CarryOnEvents.TILE_ITEM) {
            ItemRenderer.render_icon_override = null;
        }
    }
}
