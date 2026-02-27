package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.PickupHandler;

/**
 * CLIENT-SIDE hook: makes animals and baby villagers respond to right-click
 * when the player is sneaking with an empty hand, so the engine sends a
 * Packet81RightClick(entity) to the server. Actual pickup is in NetServerHandlerMixin.
 *
 * We must target EntityAnimal and EntityVillager directly because they both
 * override onEntityRightClicked — injecting into Entity.class alone is bypassed.
 */
@Mixin({EntityAnimal.class, EntityVillager.class})
public abstract class EntityMixin {

    @Inject(method = "onEntityRightClicked", at = @At("HEAD"), cancellable = true)
    public void onInteract(EntityPlayer player, ItemStack heldStack, CallbackInfoReturnable<Boolean> info) {
        if (!player.worldObj.isRemote) return;

        if (!player.isSneaking()) return;
        if (player.hasHeldItem()) return;

        Entity entity = (Entity)(Object) this;
        if (entity.isDead) return;
        if (!PickupHandler.canPlayerPickUpEntity(player, entity)) return;

        // Return true so the engine sends a Packet81RightClick(entity) to the server
        info.setReturnValue(true);
        info.cancel();
    }

}