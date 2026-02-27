package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.items.ItemEntity;
import tschipp.carryon.keybinds.CarryOnKeybinds;

/**
 * Intercepts right-clicking on entities to allow picking them up.
 * In MITE 1.6.4, entity interaction uses Entity.onEntityRightClicked(EntityPlayer, ItemStack).
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "onEntityRightClicked", at = @At("HEAD"), cancellable = true)
    public void onInteract(EntityPlayer player, ItemStack heldStack, CallbackInfoReturnable<Boolean> info) {
        Entity entity = (Entity) (Object) this;

        // Only pick up when player has nothing in hand and carry key is held
        ItemStack main = player.getHeldItemStack();
        if (main != null) return;
        if (!CarryOnKeybinds.isCarryKeyDown()) return;

        // Don't pick up items/players/projectiles etc.
        if (entity instanceof EntityPlayer) return;
        if (entity instanceof EntityItem) return;
        if (entity instanceof EntityArrow) return;

        // Don't pick up if entity is dead
        if (entity.isDead) return;

        if (PickupHandler.canPlayerPickUpEntity(player, entity)) {
            ItemStack stack = new ItemStack(CarryOnEvents.ENTITY_ITEM);
            if (ItemEntity.storeEntityData(entity, player.worldObj, stack)) {
                entity.setDead();
                if (!player.worldObj.isRemote) {
                    player.setHeldItemStack(stack);
                }
                info.setReturnValue(true);
                info.cancel();
            }
        }
    }

}