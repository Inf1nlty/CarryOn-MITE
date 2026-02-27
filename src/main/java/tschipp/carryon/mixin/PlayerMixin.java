package tschipp.carryon.mixin;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.interfaces.ICarryOnData;

/**
 * Injects carry-on data storage into EntityPlayer.
 * The carry data NBT holds per-player carry metadata (e.g. what's being carried),
 * NOT keybind state — key state is polled live via Keyboard.isKeyDown().
 */
@Mixin(EntityPlayer.class)
public abstract class PlayerMixin implements ICarryOnData {

    @Unique
    private NBTTagCompound carryon_data = new NBTTagCompound();

    // Inject into readEntityFromNBT to load carry data
    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo info) {
        if (compound.hasKey("CarryOnData")) {
            this.carryon_data = compound.getCompoundTag("CarryOnData");
        } else {
            this.carryon_data = new NBTTagCompound();
        }
    }

    // Inject into writeEntityToNBT to save carry data
    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfo info) {
        if (this.carryon_data != null && !this.carryon_data.hasNoTags()) {
            compound.setCompoundTag("CarryOnData", this.carryon_data);
        }
    }


    @Override
    public NBTTagCompound getCarryOnData() {
        if (this.carryon_data == null) {
            this.carryon_data = new NBTTagCompound();
        }
        return this.carryon_data;
    }

    @Override
    public void setCarryOnData(NBTTagCompound tag) {
        this.carryon_data = tag;
    }

}