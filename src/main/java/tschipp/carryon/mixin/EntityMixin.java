package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.PickupHandler;

@Mixin({EntityAnimal.class, EntityVillager.class})
public abstract class EntityMixin {

    @Inject(method = "onEntityRightClicked", at = @At("HEAD"), cancellable = true)
    public void onInteract(EntityPlayer player, ItemStack heldStack, CallbackInfoReturnable<Boolean> info)
    {
        if (!player.worldObj.isRemote) return;

        if (!player.isSneaking() || player.hasHeldItem()) return;

        Entity entity = (Entity)(Object) this;

        if (entity.isDead) return;

        if (!PickupHandler.canPlayerPickUpEntity(player, entity)) return;

        info.setReturnValue(true);
        info.cancel();
    }

}