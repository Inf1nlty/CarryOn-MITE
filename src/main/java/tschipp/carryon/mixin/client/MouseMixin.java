package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;

/**
 * In MITE 1.6.4, the hotbar scrolling mechanism works differently.
 * The carry item is stored in the player's hotbar, so it scrolls normally.
 * Scroll prevention is not needed since items stay in their hotbar slot.
 *
 * This mixin is kept as a placeholder for potential future use.
 */
@Mixin(EntityClientPlayerMP.class)
public abstract class MouseMixin {
    // No injection needed in MITE 1.6.4 - scroll behavior is handled differently
}