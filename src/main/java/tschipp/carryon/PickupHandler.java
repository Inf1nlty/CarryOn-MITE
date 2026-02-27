package tschipp.carryon;

import net.minecraft.Entity;
import net.minecraft.EntityPlayer;
import net.minecraft.TileEntity;
import net.minecraft.World;

public class PickupHandler {

    public static boolean canPlayerPickUpBlock(EntityPlayer player, TileEntity te, World world, int x, int y, int z) {
        return true;
    }

    public static boolean canPlayerPickUpEntity(EntityPlayer player, Entity entity) {
        return true;
    }

}