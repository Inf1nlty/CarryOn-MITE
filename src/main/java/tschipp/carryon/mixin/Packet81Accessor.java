package tschipp.carryon.mixin;

import net.minecraft.Packet81RightClick;
import net.minecraft.RightClickFilter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Packet81RightClick.class)
public interface Packet81Accessor {
    @Accessor("partial_tick")
    float getPartialTick();

    @Accessor("filter")
    RightClickFilter getFilter();
}
