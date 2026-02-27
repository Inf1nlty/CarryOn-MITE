package tschipp.carryon.items;

import net.minecraft.*;

public class ItemEntity extends Item {

    public static final String ENTITY_DATA_KEY = "entityData";

    public ItemEntity(int id) {
        super(id, "carryon:carryon_entity", 1);
        this.setMaxStackSize(1);
        this.setUnlocalizedName("carryon.entity_item");
    }

    @Override
    public Icon getIconFromSubtype(int subtype) {
        return this.itemIcon;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (hasEntityData(stack)) {
            String name = getEntityName(stack);
            if (name != null && !name.isEmpty())
                return StatCollector.translateToLocal("entity." + name + ".name");
        }
        return StatCollector.translateToLocal(this.getUnlocalizedName() + ".name");
    }

    @Override
    public boolean onItemRightClick(EntityPlayer player, float partial_tick, boolean ctrl_is_down)
    {
        ItemStack stack = player.getHeldItemStack();

        if (!hasEntityData(stack)) return false;

        RaycastCollision raycastCollision = player.getSelectedObject(partial_tick, false);

        if (raycastCollision == null || !raycastCollision.isBlock()) return false;

        World world = player.worldObj;

        int x = raycastCollision.block_hit_x, y = raycastCollision.block_hit_y, z = raycastCollision.block_hit_z;
        int placeX = raycastCollision.neighbor_block_x, placeY = raycastCollision.neighbor_block_y, placeZ = raycastCollision.neighbor_block_z;

        Block clicked = Block.blocksList[world.getBlockId(x, y, z)];

        if (clicked != null && clicked.isAlwaysReplaceable())
        {
            placeX = x; placeY = y; placeZ = z;
        }

        if (!world.isRemote)
        {
            Entity entity = getEntity(stack, world);

            if (entity != null)
            {
                entity.setPosition(placeX + 0.5, placeY, placeZ + 0.5);
                entity.rotationYaw   = 180 + player.rotationYaw;
                entity.rotationPitch = 0.0f;

                world.spawnEntityInWorld(entity);

                clearEntityData(stack);

                player.setHeldItemStack(null);

                return true;
            }
        }
        else
        {
            return true;
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
    {
        if (hasEntityData(stack))
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

    public static boolean hasEntityData(ItemStack stack)
    {
        return stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey(ENTITY_DATA_KEY) && stack.stackTagCompound.hasKey("entity");
    }

    public static boolean storeEntityData(Entity entity, World world, ItemStack stack)
    {
        if (entity == null || stack == null || stack.stackSize == 0) return false;

        String name = EntityList.getEntityString(entity);

        if (name == null || name.isEmpty()) return false;

        NBTTagCompound entityData = new NBTTagCompound();

        entity.writeToNBT(entityData);

        if (stack.stackTagCompound == null) stack.stackTagCompound = new NBTTagCompound();

        NBTTagCompound tag = stack.stackTagCompound;

        if (tag.hasKey(ENTITY_DATA_KEY)) return false;

        tag.setCompoundTag(ENTITY_DATA_KEY, entityData);
        tag.setString("entity", name);

        return true;
    }

    public static void clearEntityData(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null)
        {
            stack.stackTagCompound.removeTag(ENTITY_DATA_KEY);
            stack.stackTagCompound.removeTag("entity");
        }
    }

    public static NBTTagCompound getEntityData(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey(ENTITY_DATA_KEY))
            return stack.stackTagCompound.getCompoundTag(ENTITY_DATA_KEY);

        return null;
    }

    public static Entity getEntity(ItemStack stack, World world)
    {
        if (world == null || !hasEntityData(stack)) return null;

        String name = getEntityName(stack);

        if (name == null || name.isEmpty()) return null;

        Entity entity = EntityList.createEntityByName(name, world);

        if (entity != null) entity.readFromNBT(getEntityData(stack));

        return entity;
    }

    public static String getEntityName(ItemStack stack)
    {
        if (stack != null && stack.stackTagCompound != null && stack.stackTagCompound.hasKey("entity"))
            return stack.stackTagCompound.getString("entity");
        return null;
    }

    private int potionLevel(ItemStack stack)
    {
        NBTTagCompound data = getEntityData(stack);

        if (data == null) return 1;

        return Math.max(1, Math.min(4, data.toString().length() / 500 + 1));
    }
}