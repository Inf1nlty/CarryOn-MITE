package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.interfaces.ICarryOnData;

@Mixin(EntityPlayer.class)
public abstract class PlayerMixin implements ICarryOnData {

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