package tschipp.carryon.items;

import net.minecraft.*;

public class ItemEntity extends Item {

    public static final String ENTITY_DATA_KEY = "entityData";

    public ItemEntity(int id) {
        super(id, "apple", 1);  // texture overridden by ItemRegistryEvent.register()
        this.setMaxStackSize(1);
        this.setUnlocalizedName("carryon.entity_item");
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (hasEntityData(stack)) {
            String entityName = getEntityName(stack);
            if (entityName != null && !entityName.isEmpty()) {
                return StatCollector.translateToLocal("entity." + entityName + ".name");
            }
        }
        return "";
    }

    public static boolean hasEntityData(ItemStack stack) {
        if (stack == null) return false;
        if (stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            return tag.hasKey(ENTITY_DATA_KEY) && tag.hasKey("entity");
        }
        return false;
    }

    public static boolean storeEntityData(Entity entity, World world, ItemStack stack) {
        if (entity == null || stack == null || stack.stackSize == 0)
            return false;

        NBTTagCompound entityData = new NBTTagCompound();
        entity.writeToNBT(entityData);

        // Get entity name from EntityList
        String name = EntityList.getEntityString(entity);
        if (name == null || name.isEmpty())
            return false;

        if (stack.stackTagCompound == null) {
            stack.stackTagCompound = new NBTTagCompound();
        }
        NBTTagCompound tag = stack.stackTagCompound;
        if (tag.hasKey(ENTITY_DATA_KEY))
            return false;

        tag.setCompoundTag(ENTITY_DATA_KEY, entityData);
        tag.setString("entity", name);
        return true;
    }

    /**
     * Called when right-clicking while holding the entity item.
     * Places the entity at the targeted block.
     */
    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null || !hasEntityData(stack)) {
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

        int placeX = rc.neighbor_block_x;
        int placeY = rc.neighbor_block_y;
        int placeZ = rc.neighbor_block_z;

        Block clickedBlock = Block.blocksList[world.getBlockId(x, y, z)];
        if (clickedBlock != null && clickedBlock.isAlwaysReplaceable()) {
            placeX = x;
            placeY = y;
            placeZ = z;
        }

        if (!world.isRemote) {
            Entity entity = getEntity(stack, world);
            if (entity != null) {
                entity.setPosition(placeX + 0.5, placeY, placeZ + 0.5);
                entity.rotationYaw = 180 + player.rotationYaw;
                entity.rotationPitch = 0.0f;
                world.spawnEntityInWorld(entity);
                clearEntityData(stack);
                player.setHeldItemStack(null);
                return true;
            }
        } else {
            // Client side - consume optimistically
            return true;
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (hasEntityData(stack)) {
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

    public static void clearEntityData(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            tag.removeTag(ENTITY_DATA_KEY);
            tag.removeTag("entity");
        }
    }

    public static NBTTagCompound getEntityData(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            if (tag.hasKey(ENTITY_DATA_KEY)) {
                return tag.getCompoundTag(ENTITY_DATA_KEY);
            }
        }
        return null;
    }

    public static Entity getEntity(ItemStack stack, World world) {
        if (world == null || !hasEntityData(stack)) return null;

        String name = getEntityName(stack);
        if (name == null || name.isEmpty()) return null;

        NBTTagCompound entityData = getEntityData(stack);
        Entity entity = EntityList.createEntityByName(name, world);
        if (entity != null && entityData != null) {
            entity.readFromNBT(entityData);
        }
        return entity;
    }

    public static String getEntityName(ItemStack stack) {
        if (stack != null && stack.stackTagCompound != null) {
            NBTTagCompound tag = stack.stackTagCompound;
            if (tag.hasKey("entity")) {
                return tag.getString("entity");
            }
        }
        return null;
    }

    private int potionLevel(ItemStack stack) {
        // Estimate entity size from stored NBT instead of spawning the entity
        NBTTagCompound data = getEntityData(stack);
        if (data == null) return 1;
        // Larger entity data = heavier carry penalty
        int i = data.toString().length() / 500 + 1;
        if (i > 4) i = 4;
        return i;
    }
}
