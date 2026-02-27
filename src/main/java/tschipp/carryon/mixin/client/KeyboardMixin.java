package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;

@Mixin(Minecraft.class)
public abstract class KeyboardMixin {

    @Shadow public EntityClientPlayerMP thePlayer;

    /** Checks whether the player is currently holding a CarryOn carry item. */
    @Unique
    private boolean carryon_isCarrying()
    {
        if (thePlayer == null) return false;

        ItemStack held = thePlayer.getHeldItemStack();

        return held != null && (held.getItem() == CarryOnEvents.TILE_ITEM || held.getItem() == CarryOnEvents.ENTITY_ITEM);
    }

    /**
     * Cancels inventory opening if the player is carrying something.
     */
    @Inject(method = "runTick", at = @At(value = "NEW", target = "net/minecraft/GuiInventory"), cancellable = true)
    private void cancelInventoryIfCarrying(CallbackInfo ci)
    {
        if (carryon_isCarrying())
        {
            ci.cancel();
        }
    }

    /**
     * Redirects inventory.changeCurrentItem(dwheel) — the scroll wheel path.
     * When carrying, we swallow the call entirely.
     */
    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "net/minecraft/InventoryPlayer.changeCurrentItem(I)V"))
    private void redirectScrollHotbar(InventoryPlayer inventory, int direction)
    {
        if (!carryon_isCarrying())
        {
            inventory.changeCurrentItem(direction);
        }
    }

    /**
     * Redirects inventory.currentItem = var12 — the number-key path.
     * When carrying, we write back the current slot unchanged.
     */
    @Redirect(method = "runTick", at = @At(value = "FIELD", target = "net/minecraft/InventoryPlayer.currentItem:I", opcode = Opcodes.PUTFIELD))
    private void redirectNumberKeyHotbar(InventoryPlayer inventory, int newSlot)
    {
        if (!carryon_isCarrying())
        {
            inventory.currentItem = newSlot;
        }
    }

    /**
     * Bottom-level drop key suppression: intercept KeyBinding.onTick(keyCode) in the
     * keyboard event loop. When carrying, skip the call for keyBindDrop so pressTime
     * never increments — the key is completely inert regardless of how fast it is pressed.
     */
    @Redirect(
        method = "runTick",
        at = @At(
            value = "INVOKE",
            target = "net/minecraft/KeyBinding.onTick(I)V"
        )
    )
    private void redirectOnTick(int keyCode) {
        if (carryon_isCarrying()) {
            GameSettings gs = ((Minecraft) (Object) this).gameSettings;
            if (gs != null && keyCode == gs.keyBindDrop.keyCode) return;
        }
        KeyBinding.onTick(keyCode);
    }
}