package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.PickupHandler;
import tschipp.carryon.items.ItemEntity;
import tschipp.carryon.items.ItemTile;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(NetServerHandler.class)
public class NetServerHandlerMixin {

    @Shadow public ServerPlayer playerEntity;

    @Unique private static final Map<Integer, Long> pickupCooldown = new HashMap<>();

    @Unique private static final long COOLDOWN_MS = 500L;

    @Inject(method = "handleRightClick", at = @At("HEAD"), cancellable = true)
    private void carryon$handleRightClick(Packet81RightClick packet, CallbackInfo ci)
    {
        ServerPlayer player = this.playerEntity;

        if (player == null || !player.isSneaking() || player.hasHeldItem()) return;

        World world = player.worldObj;

        if (world == null) return;

        ItemStack held = player.getHeldItemStack();

        if (held != null && (held.getItem() == CarryOnEvents.TILE_ITEM || held.getItem() == CarryOnEvents.ENTITY_ITEM))
        {
            ci.cancel();
            return;
        }

        // Entity-only packet — animal/villager right-click
        if (packet.filter.allowsEntityInteractionOnly())
        {
            Entity entity = world.getEntityByID(packet.entity_id);

            if (entity != null && !entity.isDead && PickupHandler.canPlayerPickUpEntity(player, entity)

                    && checkCooldown(player.entityId))
            {
                ItemStack stack = new ItemStack(CarryOnEvents.ENTITY_ITEM);

                if (ItemEntity.storeEntityData(entity, world, stack))
                {
                    entity.setDead();
                    player.setHeldItemStack(stack);
                    ci.cancel();
                }
            }
            return;
        }

        if (!packet.requiresRaycasting()) return;

        // Temporarily apply packet position for server-side raycast
        RaycastCollision rc = withPacketPos(player, packet, () -> player.getSelectedObject(packet.partial_tick, false, false, null));

        if (rc == null) return;

        if (rc.isBlock()) {

            Block block = rc.getBlockHit();

            int x = rc.block_hit_x, y = rc.block_hit_y, z = rc.block_hit_z;

            if (block == null || !PickupHandler.isFunctionalBlock(block)) return;

            if (block.getBlockHardness(world.getBlockMetadata(x, y, z)) < 0) return;

            if (ItemTile.isLocked(x, y, z, world)) return;

            if (!checkCooldown(player.entityId)) {
                ci.cancel(); return;
            }

            ItemStack stack = new ItemStack(CarryOnEvents.TILE_ITEM);

            if (ItemTile.storeTileData(world.getBlockTileEntity(x, y, z), world, x, y, z, stack))
            {
                world.removeBlockTileEntity(x, y, z);
                world.setBlockToAir(x, y, z);

                player.setHeldItemStack(stack);
                ci.cancel();
            }
        }
        else if (rc.isEntity())
        {
            Entity entity = rc.getEntityHit();

            if (entity == null || entity.isDead) return;

            if (!PickupHandler.canPlayerPickUpEntity(player, entity)) return;

            if (!checkCooldown(player.entityId))
            {
                ci.cancel(); return;
            }

            ItemStack stack = new ItemStack(CarryOnEvents.ENTITY_ITEM);

            if (ItemEntity.storeEntityData(entity, world, stack))
            {
                entity.setDead();
                player.setHeldItemStack(stack);
                ci.cancel();
            }
        }
    }

    /** Returns true and records timestamp if outside cooldown window; false if still cooling down. */
    @Unique
    private static boolean checkCooldown(int playerId)
    {
        long now = System.currentTimeMillis();
        Long last = pickupCooldown.get(playerId);

        if (last != null && now - last < COOLDOWN_MS)
            return false;

        pickupCooldown.put(playerId, now);
        return true;
    }

    /** Temporarily applies packet position/rotation to the player for a server-side raycast. */
    @Unique
    private static RaycastCollision withPacketPos(ServerPlayer player, Packet81RightClick pkt, Supplier<RaycastCollision> fn)
    {
        double ox = player.posX, oy = player.posY, oz = player.posZ;
        float oyaw = player.rotationYaw, opitch = player.rotationPitch;
        double opx = player.prevPosX, opy = player.prevPosY, opz = player.prevPosZ;
        float opyaw = player.prevRotationYaw, oppitch = player.prevRotationPitch;
        float oySize = player.ySize;
        AxisAlignedBB obb = player.boundingBox.copy();

        player.posX = pkt.pos_x; player.posY = pkt.pos_y; player.posZ = pkt.pos_z;
        player.rotationYaw = pkt.rotation_yaw; player.rotationPitch = pkt.rotation_pitch;
        player.prevPosX = pkt.prev_pos_x; player.prevPosY = pkt.prev_pos_y; player.prevPosZ = pkt.prev_pos_z;
        player.prevRotationYaw = pkt.prev_rotation_yaw; player.prevRotationPitch = pkt.prev_rotation_pitch;
        player.ySize = pkt.y_size;
        player.boundingBox.setBB(pkt.bb);

        RaycastCollision rc = fn.get();

        player.posX = ox; player.posY = oy; player.posZ = oz;
        player.rotationYaw = oyaw; player.rotationPitch = opitch;
        player.prevPosX = opx; player.prevPosY = opy; player.prevPosZ = opz;
        player.prevRotationYaw = opyaw; player.prevRotationPitch = oppitch;
        player.ySize = oySize;
        player.boundingBox.setBB(obb);

        return rc;
    }
}