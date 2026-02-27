package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.CarryOnEvents;

/**
 * CLIENT-SIDE guard on Entity.onEntityRightClicked.
 * Server-side entity pickup is handled in NetServerHandlerMixin.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "onEntityRightClicked", at = @At("HEAD"), cancellable = true)
    public void onInteract(EntityPlayer player, ItemStack heldStack, CallbackInfoReturnable<Boolean> info) {
        // Only client-side suppression needed here
        if (!player.worldObj.isRemote) return;

        if (!player.isSneaking()) return;
        if (player.hasHeldItem()) return;

        Entity entity = (Entity)(Object) this;
        if (entity instanceof EntityPlayer) return;
        if (entity instanceof EntityItem) return;
        if (entity instanceof EntityArrow) return;
        if (entity.isDead) return;
        if (!PickupHandler.canPlayerPickUpEntity(player, entity)) return;

        // Suppress client-side interaction; server will handle via NetServerHandlerMixin
        info.setReturnValue(false);
        info.cancel();
    }

}