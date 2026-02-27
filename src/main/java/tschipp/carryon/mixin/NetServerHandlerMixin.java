package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.items.ItemEntity;
import tschipp.carryon.items.ItemTile;

import java.util.HashMap;
import java.util.Map;

/**
 * Injects into NetServerHandler.handleRightClick (server-side packet handler).
 *
 * This is the correct place to intercept: it runs purely on the server thread,
 * before onPlayerRightClickChecked (which is final and cannot be injected),
 * and before Block.onBlockActivated opens any GUI.
 *
 * When the player is sneaking with an empty hand and targets a carriable block
 * or entity, we perform the pickup and cancel the packet entirely.
 */
@Mixin(NetServerHandler.class)
public class NetServerHandlerMixin {

    @Shadow
    public ServerPlayer playerEntity;

    private static final Map<Integer, Long> pickupCooldown = new HashMap<>();
    private static final long PICKUP_COOLDOWN_MS = 500L;

    @Inject(method = "handleRightClick", at = @At("HEAD"), cancellable = true)
    private void carryon$onHandleRightClick(Packet81RightClick packet, CallbackInfo ci) {
        ServerPlayer player = this.playerEntity;
        if (player == null) return;

        // Must be sneaking with empty hand
        if (!player.isSneaking()) return;
        if (player.hasHeldItem()) return;

        World world = player.worldObj;
        if (world == null) return;

        // Already carrying something — block GUI opening for carry items
        // (this case is also handled in BlockStateMixin client-side)
        ItemStack held = player.getHeldItemStack();
        if (held != null && (held.getItem() == CarryOnEvents.TILE_ITEM
                || held.getItem() == CarryOnEvents.ENTITY_ITEM)) {
            ci.cancel();
            return;
        }

        // Need a raycasting packet to know the target block coords
        if (!packet.requiresRaycasting()) return;

        float partialTick = ((Packet81Accessor)(Object) packet).getPartialTick();

        // Reconstruct RaycastCollision server-side, same as handleRightClick does
        double prevPosX = player.posX; double prevPosY = player.posY; double prevPosZ = player.posZ;
        float prevYaw = player.rotationYaw; float prevPitch = player.rotationPitch;
        double prevPrevX = player.prevPosX; double prevPrevY = player.prevPosY; double prevPrevZ = player.prevPosZ;
        float prevPrevYaw = player.prevRotationYaw; float prevPrevPitch = player.prevRotationPitch;
        float prevYSize = player.ySize;
        AxisAlignedBB prevBB = player.boundingBox.copy();

        player.posX = packet.pos_x; player.posY = packet.pos_y; player.posZ = packet.pos_z;
        player.rotationYaw = packet.rotation_yaw; player.rotationPitch = packet.rotation_pitch;
        player.prevPosX = packet.prev_pos_x; player.prevPosY = packet.prev_pos_y; player.prevPosZ = packet.prev_pos_z;
        player.prevRotationYaw = packet.prev_rotation_yaw; player.prevRotationPitch = packet.prev_rotation_pitch;
        player.ySize = packet.y_size;
        player.boundingBox.setBB(packet.bb);

        RaycastCollision rc = player.getSelectedObject(partialTick, false, false, null);

        player.posX = prevPosX; player.posY = prevPosY; player.posZ = prevPosZ;
        player.rotationYaw = prevYaw; player.rotationPitch = prevPitch;
        player.prevPosX = prevPrevX; player.prevPosY = prevPrevY; player.prevPosZ = prevPrevZ;
        player.prevRotationYaw = prevPrevYaw; player.prevRotationPitch = prevPrevPitch;
        player.ySize = prevYSize;
        player.boundingBox.setBB(prevBB);

        if (rc == null) return;

        // --- Block pickup ---
        if (rc.isBlock()) {
            Block block = rc.getBlockHit();
            if (block == null || !PickupHandler.isFunctionalBlock(block)) return;

            int x = rc.block_hit_x, y = rc.block_hit_y, z = rc.block_hit_z;

            if (block.getBlockHardness(world.getBlockMetadata(x, y, z)) < 0) return;
            if (ItemTile.isLocked(x, y, z, world)) return;

            // Cooldown: prevent double-pickup within 500 ms
            int playerId = player.entityId;
            long now = System.currentTimeMillis();
            Long last = pickupCooldown.get(playerId);
            if (last != null && now - last < PICKUP_COOLDOWN_MS) {
                ci.cancel();
                return;
            }
            pickupCooldown.put(playerId, now);

            TileEntity te = world.getBlockTileEntity(x, y, z);
            ItemStack stack = new ItemStack(CarryOnEvents.TILE_ITEM);
            if (ItemTile.storeTileData(te, world, x, y, z, stack)) {
                world.removeBlockTileEntity(x, y, z);
                world.setBlockToAir(x, y, z);
                player.setHeldItemStack(stack);
                ci.cancel();
            }
            return;
        }

        // --- Entity pickup ---
        if (rc.isEntity()) {
            Entity entity = rc.getEntityHit();
            if (entity == null || entity.isDead) return;
            if (entity instanceof EntityPlayer) return;
            if (entity instanceof EntityItem) return;
            if (entity instanceof EntityArrow) return;
            if (!PickupHandler.canPlayerPickUpEntity(player, entity)) return;

            int playerId = player.entityId;
            long now = System.currentTimeMillis();
            Long last = pickupCooldown.get(playerId);
            if (last != null && now - last < PICKUP_COOLDOWN_MS) {
                ci.cancel();
                return;
            }
            pickupCooldown.put(playerId, now);

            ItemStack stack = new ItemStack(CarryOnEvents.ENTITY_ITEM);
            if (ItemEntity.storeEntityData(entity, world, stack)) {
                entity.setDead();
                player.setHeldItemStack(stack);
                ci.cancel();
            }
        }
    }
}
