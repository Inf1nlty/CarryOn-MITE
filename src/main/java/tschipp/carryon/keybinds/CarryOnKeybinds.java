package tschipp.carryon.keybinds;

import net.minecraft.KeyBinding;
import org.lwjgl.input.Keyboard;

/**
 * Carry key handler, modelled after ShopKeyHandler from newshop mod.
 *
 * The carry action uses Left Shift (sneak key), so we use the vanilla
 * keyBindSneak to detect it — no custom key needed.
 * A custom KeyBinding is still registered so it appears in controls
 * and can be rebound by the player.
 */
public class CarryOnKeybinds {

    /** The carry key — defaults to Left Shift (KEY_LSHIFT = 42). */
    public static KeyBinding carryKey;

    public static void init() {
        carryKey = new KeyBinding("key.carryon.carry", Keyboard.KEY_LSHIFT);
    }

    /**
     * Returns true when the carry key (default: Left Shift) is currently held down.
     * Called every client tick. Does NOT require the player to exist.
     */
    public static boolean isCarryKeyDown() {
        if (carryKey == null) return false;
        int code = carryKey.keyCode;
        if (code < 0) {
            // Mouse button
            return org.lwjgl.input.Mouse.isButtonDown(code + 100);
        }
        return Keyboard.isKeyDown(code);
    }

    /**
     * Client-tick polling. Called from CarryOnEvents.onClientTick().
     * Syncs the live keyboard state so that PlayerMixin / PickupHandler
     * can read it without touching NBT.
     */
    public static void onClientTick() {
        // Nothing extra needed — callers just call isCarryKeyDown() directly.
    }
}
