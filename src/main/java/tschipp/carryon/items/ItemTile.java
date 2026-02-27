package tschipp.carryon.items;


import net.minecraft.*;

public class ItemTile extends Item {

    public static final String TILE_DATA_KEY = "tileData";
    public static final String[] DIRECTION_KEYS = new String[]{"rotation", "rot", "Direction", "face", "direction", "dir", "front"};

    public ItemTile(int id) {
        super(id, "carryon:carryon_tile", 1);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("carryon.tile_item");
    }

    /**
     * Returns the top-face icon of the carried block.
     * getIconIndex() in Item is final and calls getIconFromSubtype(subtype),
     * but subtype carries no block info here. Instead we use render_icon_override
     * set by ItemIconOverrideMixin before the renderItem call.
     * This fallback is used for hotbar/inventory 2D rendering.
     */
    @Override
    public Icon getIconFromSubtype(int subtype) {
        return this.itemIcon;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (hasTileData(stack)) {
            // Return the display name of the contained block item
            Block block = getBlock(stack);
            if (block != null && block.blockID != 0) {
                int meta = getMeta(stack);
                ItemStack blockStack = new ItemStack(block, 1, meta);
                return blockStack.getItem().getItemStackDisplayName(blockStack);
            }
        }
        // Fallback: return the translated item name so we never show a raw unlocalized key
        return StatCollector.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    /**
     * Called when an item is right-clicked on a block.
     * In MITE 1.6.4, this is handled via onItemRightClick.
     * We override to place the stored block.
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null || !hasTileData(stack)) {
            return false;
        }

        RaycastCollision rc = player.getSelectedObject(partial_tick, false);
        if (rc == null || !rc.isBlock()) {
            return false;
        }

        World world = player.worldObj;
        int x = rc.block_hit_x;
        int y = rc.block_hit_y;
        int z = rc.block_hit_z;

        // Determine placement position - use pre-computed neighbor block coords
        int placeX = rc.neighbor_block_x;
        int placeY = rc.neighbor_block_y;
        int placeZ = rc.neighbor_block_z;

        // If the clicked block is replaceable (like air, grass), place there
        Block clickedBlock = Block.blocksList[world.getBlockId(x, y, z)];
        if (clickedBlock != null && clickedBlock.isAlwaysReplaceable()) {
            placeX = x;
            placeY = y;
            placeZ = z;
        }

        Block containedBlock = getBlock(stack);
        int containedMeta = getMeta(stack);

        if (containedBlock == null || containedBlock.blockID == 0) {
            return false;
        }

        // Check if block can be placed at target position
        int existingId = world.getBlockId(placeX, placeY, placeZ);
        Block existingBlock = Block.blocksList[existingId];
        boolean canPlace = (existingId == 0 || (existingBlock != null && existingBlock.isAlwaysReplaceable()));

        if (!canPlace) {
            return false;
        }

        if (!player.canPlayerEdit(placeX, placeY, placeZ, stack)) {
            return false;
        }

        try {
            // Place block
            world.setBlock(placeX, placeY, placeZ, containedBlock.blockID, containedMeta, 3);

            // Play the block's placement sound (same as vanilla ItemBlock)
            StepSound stepSound = containedBlock.stepSound;
            world.playSoundEffect(
                    placeX + 0.5, placeY + 0.5, placeZ + 0.5,
                    stepSound.getPlaceSound(),
                    (stepSound.getVolume() + 1.0F) / 2.0F,
                    stepSound.getPitch() * 0.8F
            );

            // Restore tile entity data if available
            NBTTagCompound tileData = getTileData(stack);
            if (tileData != null && !tileData.hasNoTags()) {
                TileEntity te = world.getBlockTileEntity(placeX, placeY, placeZ);
                if (te != null) {
                    tileData.setInteger("x", placeX);
                    tileData.setInteger("y", placeY);
                    tileData.setInteger("z", placeZ);
                    te.readFromNBT(tileData);
                }
            }

            clearTileData(stack);
            if (!world.isRemote) {
                player.setHeldItemStack(null);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (hasTileData(stack)) {
            if (entity instanceof EntityLivingBase) {
                if (entity instanceof EntityPlayer && ((EntityPlayer) entity).inCreativeMode())
                    return;

                ((EntityLivingBase) entity).addPotionEffect(
                        new PotionEffect(Potion.moveSlowdown.id, 1, potionLevel(stack), false));
            }
        } else {
            if (isSelected) {
                stack.stackSize = 0;
            }
        }
    }

    public static boolean hasTileData(ItemStack stack) {
        if (stack == null) return false;
        if (stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            return tag.hasKey(TILE_DATA_KEY) && tag.hasKey("blockId");
        }
        return false;
    }

    public static boolean storeTileData(TileEntity tile, World world, int x, int y, int z, ItemStack stack) {
        if (stack == null || stack.stackSize == 0)
            return false;

        int blockId = world.getBlockId(x, y, z);
        int meta = world.getBlockMetadata(x, y, z);

        NBTTagCompound tileNbt = new NBTTagCompound();
        if (tile != null) {
            tile.writeToNBT(tileNbt);
        }

        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        NBTTagCompound tag = stack.stackTagCompound;
        if (tag.hasKey(TILE_DATA_KEY))
            return false;

        tag.setCompoundTag(TILE_DATA_KEY, tileNbt);
        tag.setInteger("blockId", blockId);
        tag.setInteger("blockMeta", meta);
        return true;
    }

    public static void clearTileData(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            tag.removeTag(TILE_DATA_KEY);
            tag.removeTag("blockId");
            tag.removeTag("blockMeta");
        }
    }

    public static NBTTagCompound getTileData(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            if (tag.hasKey(TILE_DATA_KEY)) {
                return tag.getCompoundTag(TILE_DATA_KEY);
            }
        }
        return null;
    }

    public static Block getBlock(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            if (tag.hasKey("blockId")) {
                int id = tag.getInteger("blockId");
                return Block.blocksList[id];
            }
        }
        return null;
    }

    public static int getMeta(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            if (tag.hasKey("blockMeta")) {
                return tag.getInteger("blockMeta");
            }
        }
        return 0;
    }

    public static ItemStack getItemStack(ItemStack stack) {
        Block block = getBlock(stack);
        if (block == null || block.blockID == 0) return null;
        return new ItemStack(block, 1, getMeta(stack));
    }

    public static boolean isLocked(int x, int y, int z, World world) {
        TileEntity te = world.getBlockTileEntity(x, y, z);
        if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToNBT(tag);
            return tag.hasKey("Lock") && !tag.getString("Lock").equals("");
        }
        return false;
    }

    private int potionLevel(ItemStack stack) {
        NBTTagCompound tileData = getTileData(stack);
        if (tileData == null) return 1;
        String nbt = tileData.toString();
        int i = nbt.length() / 500;
        if (i > 4) i = 4;
        if (i < 1) i = 1;
        return i;
    }
}
