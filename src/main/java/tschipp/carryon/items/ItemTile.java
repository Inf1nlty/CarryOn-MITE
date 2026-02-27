package tschipp.carryon.items;

import net.minecraft.*;
import tschipp.carryon.CarryOnData;

public class ItemTile extends Item {

    public static final String TILE_DATA_KEY = "tileData";

    public ItemTile(int id) {
        super(id, "carryon:carryon_tile", 1);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("carryon.tile_item");
    }

    @Override
    public Icon getIconFromSubtype(int subtype) {
        return this.itemIcon;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (hasTileData(stack)) {
            Block block = getBlock(stack);
            if (block != null && block.blockID != 0) {
                ItemStack blockStack = new ItemStack(block, 1, getMeta(stack));
                return blockStack.getItem().getItemStackDisplayName(blockStack);
            }
        }

        return StatCollector.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down)
    {
        ItemStack stack = player.getHeldItemStack();

        if (!hasTileData(stack)) return false;

        RaycastCollision collision = player.getSelectedObject(partial_tick, false);

        if (collision == null || !collision.isBlock()) return false;

        World world = player.worldObj;

        int x = collision.block_hit_x, y = collision.block_hit_y, z = collision.block_hit_z;
        int placeX = collision.neighbor_block_x, placeY = collision.neighbor_block_y, placeZ = collision.neighbor_block_z;

        Block clicked = Block.blocksList[world.getBlockId(x, y, z)];

        if (clicked != null && clicked.isAlwaysReplaceable())
        {
            placeX = x; placeY = y; placeZ = z;
        }

        Block containedBlock = getBlock(stack);

        int containedMeta = getMeta(stack);

        if (containedBlock == null || containedBlock.blockID == 0) return false;

        int existingId = world.getBlockId(placeX, placeY, placeZ);

        Block existing = Block.blocksList[existingId];

        if (existingId != 0 && (existing == null || !existing.isAlwaysReplaceable())) return false;

        if (!player.canPlayerEdit(placeX, placeY, placeZ, stack)) return false;

        world.setBlock(placeX, placeY, placeZ, containedBlock.blockID, containedMeta, 3);

        StepSound stepSound = containedBlock.stepSound;

        world.playSoundEffect(placeX + 0.5, placeY + 0.5, placeZ + 0.5, stepSound.getPlaceSound(), (stepSound.getVolume() + 1.0F) / 2.0F, stepSound.getPitch() * 0.8F);

        NBTTagCompound tileData = getTileData(stack);

        if (tileData != null && !tileData.hasNoTags())
        {
            TileEntity tileEntity = world.getBlockTileEntity(placeX, placeY, placeZ);
            if (tileEntity != null)
            {
                tileData.setInteger("x", placeX);
                tileData.setInteger("y", placeY);
                tileData.setInteger("z", placeZ);
                tileEntity.readFromNBT(tileData);
            }
        }

        clearTileData(stack);

        if (!world.isRemote) player.setHeldItemStack(null);

        return true;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
    {
        if (hasTileData(stack))
        {
            if (entity instanceof EntityLivingBase living)
            {
                if (living instanceof EntityPlayer p && p.inCreativeMode()) return;

                living.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 1, potionLevel(stack), false));
            }
        }
        else if (isSelected)
        {
            stack.stackSize = 0;
        }
    }

    public static boolean hasTileData(ItemStack stack)
    {
        return stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey(TILE_DATA_KEY) && stack.stackTagCompound.hasKey("blockId");
    }

    public static boolean storeTileData(TileEntity tile, World world, int x, int y, int z, ItemStack stack)
    {
        if (stack == null || stack.stackSize == 0) return false;

        NBTTagCompound tileNbt = new NBTTagCompound();

        if (tile != null) tile.writeToNBT(tileNbt);

        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();

        NBTTagCompound tag = stack.stackTagCompound;

        if (tag.hasKey(TILE_DATA_KEY)) return false;

        tag.setCompoundTag(TILE_DATA_KEY, tileNbt);
        tag.setInteger("blockId", world.getBlockId(x, y, z));
        tag.setInteger("blockMeta", world.getBlockMetadata(x, y, z));
        tag.setByte(CarryOnData.NO_DROP_KEY, (byte) 1);

        return true;
    }

    public static void clearTileData(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null)
        {
            stack.stackTagCompound.removeTag(TILE_DATA_KEY);
            stack.stackTagCompound.removeTag("blockId");
            stack.stackTagCompound.removeTag("blockMeta");
            stack.stackTagCompound.removeTag(CarryOnData.NO_DROP_KEY);
        }
    }

    public static NBTTagCompound getTileData(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey(TILE_DATA_KEY))
            return stack.stackTagCompound.getCompoundTag(TILE_DATA_KEY);

        return null;
    }

    public static Block getBlock(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("blockId"))
            return Block.blocksList[stack.stackTagCompound.getInteger("blockId")];

        return null;
    }

    public static int getMeta(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("blockMeta"))
            return stack.stackTagCompound.getInteger("blockMeta");

        return 0;
    }

    public static boolean isLocked(int x, int y, int z, World world)
    {
        TileEntity te = world.getBlockTileEntity(x, y, z);

        if (te == null) return false;

        NBTTagCompound tag = new NBTTagCompound();

        te.writeToNBT(tag);

        return tag.hasKey("Lock") && !tag.getString("Lock").isEmpty();
    }

    private int potionLevel(ItemStack stack)
    {
        NBTTagCompound tileData = getTileData(stack);

        if (tileData == null) return 1;

        return Math.max(1, Math.min(4, tileData.toString().length() / 500));
    }
}