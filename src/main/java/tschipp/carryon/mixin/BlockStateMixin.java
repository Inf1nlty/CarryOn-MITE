package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.PickupHandler;

@Mixin(Block.class)
public class BlockStateMixin {

    @Inject(method = "onBlockActivated", at = @At("HEAD"), cancellable = true)
    public void onBlockActivated(World world, int x, int y, int z, EntityPlayer player, EnumFace face, float offsetX, float offsetY, float offsetZ, CallbackInfoReturnable<Boolean> info)
    {
        if (!world.isRemote) return;

        ItemStack held = player.getHeldItemStack();

        if (held != null && (held.getItem() == CarryOnEvents.TILE_ITEM || held.getItem() == CarryOnEvents.ENTITY_ITEM))
        {
            info.setReturnValue(false);
            info.cancel();
            return;
        }

        if (player.isSneaking() && held == null && PickupHandler.isFunctionalBlock((Block)(Object) this))
        {
            info.setReturnValue(false);
            info.cancel();
        }
    }
}