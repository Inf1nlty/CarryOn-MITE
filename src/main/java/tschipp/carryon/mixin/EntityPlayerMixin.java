package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tschipp.carryon.CarryOnData;
import tschipp.carryon.interfaces.ICarryOnData;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin implements ICarryOnData {

    @Unique private NBTTagCompound carryon_data = new NBTTagCompound();

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo info)
    {
        carryon_data = compound.hasKey("CarryOnData") ? compound.getCompoundTag("CarryOnData") : new NBTTagCompound();
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfo info)
    {
        if (carryon_data != null && !carryon_data.hasNoTags()) compound.setCompoundTag("CarryOnData", carryon_data);
    }

    /** Blocks dropping the held item on the server if it carries the no-drop NBT tag. */
    @Inject(method = "dropOneItem", at = @At("HEAD"), cancellable = true)
    public void carryon$blockDrop(boolean dropAll, CallbackInfoReturnable<EntityItem> info)
    {
        EntityPlayer self = (EntityPlayer)(Object) this;

        if (self.worldObj.isRemote) return;

        ItemStack held = self.inventory.getCurrentItemStack();

        if (held != null && held.stackTagCompound != null && held.stackTagCompound.hasKey(CarryOnData.NO_DROP_KEY))
            info.setReturnValue(null);
    }

    @Override
    public NBTTagCompound carryOn$getCarryOnData()
    {
        if (carryon_data == null) carryon_data = new NBTTagCompound();

        return carryon_data;
    }

    @Override
    public void carryOn$setCarryOnData(NBTTagCompound tag)
    {
        carryon_data = tag;
    }
}