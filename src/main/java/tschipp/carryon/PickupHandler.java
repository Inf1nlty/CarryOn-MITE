package tschipp.carryon;

import net.minecraft.*;

/**
 * Determines which blocks and entities the player is allowed to carry.
 *
 * Only "functional" blocks are permitted: chests, furnaces, workbenches,
 * anvils, hoppers, dispensers, droppers, enchantment tables, brewing stands,
 * beacons, and their MITE variants.
 */
public class PickupHandler {

    public static boolean canPlayerPickUpBlock(EntityPlayer player, TileEntity te, World world, int x, int y, int z) {
        int blockId = world.getBlockId(x, y, z);
        Block block = Block.blocksList[blockId];
        if (block == null) return false;
        return isFunctionalBlock(block);
    }

    public static boolean canPlayerPickUpEntity(EntityPlayer player, Entity entity) {
        // Allow carrying animals (all EntityAnimal subclasses)
        if (entity instanceof EntityAnimal) return true;
        // Allow carrying baby villagers only
        if (entity instanceof EntityVillager && ((EntityVillager) entity).isChild()) return true;
        return false;
    }

    /**
     * Returns true only for "functional" interactive blocks:
     * chests, furnaces, workbenches, anvils, hoppers, dispensers,
     * droppers, enchantment tables, brewing stands, beacons, and their MITE variants.
     */
    public static boolean isFunctionalBlock(Block block) {
        // Chests (vanilla + MITE strongboxes)
        if (block == Block.chest) return true;
        if (block == Block.chestTrapped) return true;
        if (block == Block.enderChest) return true;
        if (block instanceof BlockStrongbox) return true;

        // Furnaces (vanilla + all MITE variants)
        if (block == Block.furnaceIdle || block == Block.furnaceBurning) return true;
        if (block == Block.furnaceClayIdle || block == Block.furnaceClayBurning) return true;
        if (block == Block.furnaceSandstoneIdle || block == Block.furnaceSandstoneBurning) return true;
        if (block == Block.furnaceObsidianIdle || block == Block.furnaceObsidianBurning) return true;
        if (block == Block.furnaceNetherrackIdle || block == Block.furnaceNetherrackBurning) return true;
        if (block == Block.furnaceHardenedClayIdle || block == Block.furnaceHardenedClayBurning) return true;

        // Workbench
        if (block == Block.workbench) return true;

        // Anvils (vanilla + MITE metal variants)
        if (block instanceof BlockAnvil) return true;

        // Hopper, Dispenser, Dropper
        if (block instanceof BlockHopper) return true;
        if (block instanceof BlockDispenser) return true;
        if (block == Block.dropper) return true;

        // Enchantment tables (vanilla + emerald)
        if (block instanceof BlockEnchantmentTable) return true;
        if (block == Block.enchantmentTableEmerald) return true;

        // Brewing stand
        if (block instanceof BlockBrewingStand) return true;

        // Beacon
        if (block instanceof BlockBeacon) return true;

        // Jukebox
        if (block instanceof BlockJukeBox) return true;

        return false;
    }
}