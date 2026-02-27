package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.render.BlockRendererLayer;
import tschipp.carryon.render.EntityRendererLayer;
import tschipp.carryon.render.ICarrying;

@Mixin(RenderPlayer.class)
public abstract class PlayerRendererMixin {

    @Shadow private ModelBiped modelBipedMain;

    @Inject(method = "renderSpecials", at = @At("RETURN"))
    private void onRenderSpecials(AbstractClientPlayer player, float partialTick, CallbackInfo info)
    {
        ItemStack stack = player.getHeldItemStack();
        ICarrying model = (ICarrying) this.modelBipedMain;

        if (stack != null && stack.getItem() == CarryOnEvents.TILE_ITEM)
        {
            model.setCarryingBlock(true);
            model.setCarryingEntity(false);

            BlockRendererLayer.renderThirdPerson(player, partialTick);

        }
        else if (stack != null && stack.getItem() == CarryOnEvents.ENTITY_ITEM)
        {
            model.setCarryingBlock(false);
            model.setCarryingEntity(true);
            EntityRendererLayer.renderThirdPerson(player, partialTick);
        }
        else
        {
            model.setCarryingBlock(false);
            model.setCarryingEntity(false);
        }
    }
}