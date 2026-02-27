package tschipp.carryon;

import net.fabricmc.api.ModInitializer;
import net.xiaoyu233.fml.ModResourceManager;
import net.xiaoyu233.fml.reload.event.MITEEvents;

public class CarryOn implements ModInitializer {

    public static String MODID = "carryon";

    @Override
    public void onInitialize() {
        ModResourceManager.addResourcePackDomain(MODID);

        MITEEvents.MITE_EVENT_BUS.register(new CarryOnEvents());
    }
}
