package tschipp.carryon;

import net.fabricmc.api.ModInitializer;
import net.xiaoyu233.fml.reload.event.MITEEvents;
import tschipp.carryon.keybinds.CarryOnKeybinds;

public class CarryOn implements ModInitializer {

    public static String MODID = "carryon";

    @Override
    public void onInitialize() {
        MITEEvents.MITE_EVENT_BUS.register(new CarryOnEvents());

        CarryOnKeybinds.init();
    }

}
