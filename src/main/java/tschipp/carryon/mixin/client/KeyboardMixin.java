package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;

/**
 * Prevents the player from opening their inventory while carrying.
 *
 * In MITE 1.6.4, the inventory is opened in Minecraft.runTick() via:
 *   while (gameSettings.keyBindInventory.isPressed()) {
 *       displayGuiScreen(new GuiInventory(thePlayer));
 *   }
 *
 * We inject before the GuiInventory constructor is invoked to cancel it.
 */
@Mixin(Minecraft.class)
public abstract class KeyboardMixin {

    @Shadow
    public EntityClientPlayerMP thePlayer;

    /**
     * Cancels inventory opening if the player is carrying something.
     */
    @Inject(
        method = "runTick",
        at = @At(
            value = "NEW",
            target = "net/minecraft/GuiInventory"
        ),
        cancellable = true
    )
    private void cancelInventoryIfCarrying(CallbackInfo ci) {
        if (thePlayer != null) {
            ItemStack held = thePlayer.getHeldItemStack();
            if (held != null && (held.getItem() == CarryOnEvents.TILE_ITEM
                    || held.getItem() == CarryOnEvents.ENTITY_ITEM)) {
                ci.cancel();
            }
        }
    }

}