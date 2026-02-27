package tschipp.carryon.mixin.client;

import net.minecraft.GameSettings;
import net.minecraft.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.keybinds.CarryOnKeybinds;

import java.util.Arrays;

/**
 * Appends the carry keybinding into GameSettings.keyBindings so it
 * appears in the controls screen and persists in options.txt.
 * Modelled after ShopKeyBindings / GameSettingsMixin from newshop mod.
 */
@Mixin(GameSettings.class)
public abstract class GameSettingsMixin {

    @Shadow
    public KeyBinding[] keyBindings;

    @Inject(method = "initKeybindings", at = @At("RETURN"))
    private void carryon$injectKeys(CallbackInfo ci) {
        // CarryOnKeybinds.init() is called from CarryOn.onInitialize() before
        // GameSettings is constructed, so carryKey is always non-null here.
        KeyBinding key = CarryOnKeybinds.carryKey;
        if (key == null) return;

        // Guard against double-registration on world reload
        for (KeyBinding existing : keyBindings) {
            if (existing == key) return;
        }

        KeyBinding[] expanded = Arrays.copyOf(keyBindings, keyBindings.length + 1);
        expanded[keyBindings.length] = key;
        keyBindings = expanded;
    }
}
