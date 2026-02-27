package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.PickupHandler;

/**
 * CLIENT-SIDE ONLY guard on Block.onBlockActivated.
 *
 * The server-side pickup is handled in NetServerHandlerMixin (handleRightClick).
 * This mixin only prevents the CLIENT from opening block GUIs when:
 *   1. Player is sneaking with empty hand targeting a carriable block
 *   2. Player is already holding a carry item
 */
@Mixin(Block.class)
public class BlockStateMixin {

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    public void onBlockActivated(
            World world, int x, int y, int z,
            EntityPlayer player, EnumFace face,
            float offsetX, float offsetY, float offsetZ,
            CallbackInfoReturnable<Boolean> info) {

        // Only act client-side — server is handled by NetServerHandlerMixin
        if (!world.isRemote) return;

        ItemStack held = player.getHeldItemStack();

        // Suppress GUI if already carrying something
        if (held != null && (held.getItem() == CarryOnEvents.TILE_ITEM
                || held.getItem() == CarryOnEvents.ENTITY_ITEM)) {
            info.setReturnValue(false);
            info.cancel();
            return;
        }

        // Suppress GUI if sneaking empty-handed targeting a carriable block
        if (player.isSneaking() && held == null
                && PickupHandler.isFunctionalBlock((Block)(Object) this)) {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}
