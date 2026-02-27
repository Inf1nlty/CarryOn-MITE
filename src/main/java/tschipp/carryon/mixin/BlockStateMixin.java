package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.items.ItemTile;
import tschipp.carryon.keybinds.CarryOnKeybinds;

/**
 * Intercepts block activation (right-click) to allow picking up tile entities.
 * In MITE 1.6.4, this hooks into Block.onBlockActivated.
 *
 * Note: We also need to prevent block activation when holding a carry item.
 */
@Mixin(Block.class)
public class BlockStateMixin {

    /**
     * Injected at the HEAD of onBlockActivated to intercept sneaking empty-handed
     * right-clicks and pick up the block (with tile entity data).
     *
     * Also cancels block activation when holding a carry item (to prevent opening
     * chests, etc. while carrying something).
     */
    @Inject(
        method = "onBlockActivated",
        at = @At("HEAD"),
        cancellable = true
    )
    public void onBlockActivated(
            World world, int x, int y, int z,
            EntityPlayer player, EnumFace face,
            float offsetX, float offsetY, float offsetZ,
            CallbackInfoReturnable<Boolean> info) {

        ItemStack main = player.getHeldItemStack();

        // If holding a carry item, cancel block activation but return false so
        // onItemRightClick can still run for placement.
        if (main != null && (main.getItem() == CarryOnEvents.TILE_ITEM || main.getItem() == CarryOnEvents.ENTITY_ITEM)) {
            info.setReturnValue(false);
            info.cancel();
            return;
        }

        // Sneaking+carry key with empty hand: pick up the block
        if (main == null && CarryOnKeybinds.isCarryKeyDown()) {
            Block block = (Block) (Object) this;

            // Don't pick up air or unbreakable blocks
            if (block.blockID == 0) return;
            if (block.getBlockHardness(world.getBlockMetadata(x, y, z)) < 0) return;

            // Don't pick up locked blocks (e.g., chests with lock keys)
            if (ItemTile.isLocked(x, y, z, world)) return;

            TileEntity te = world.getBlockTileEntity(x, y, z);

            if (PickupHandler.canPlayerPickUpBlock(player, te, world, x, y, z)) {
                ItemStack stack = new ItemStack(CarryOnEvents.TILE_ITEM);

                if (ItemTile.storeTileData(te, world, x, y, z, stack)) {
                    try {
                        world.removeBlockTileEntity(x, y, z);
                        world.setBlockToAir(x, y, z);
                        if (!world.isRemote) {
                            player.setHeldItemStack(stack);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    info.setReturnValue(true);
                    info.cancel();
                }
            }
        }
    }

}
