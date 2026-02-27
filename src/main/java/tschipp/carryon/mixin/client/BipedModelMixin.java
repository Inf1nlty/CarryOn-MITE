package tschipp.carryon.mixin.client;

import net.minecraft.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tschipp.carryon.render.ICarrying;

/**
 * Injects into ModelBiped to modify arm poses when carrying a block or entity.
 * In MITE 1.6.4, arm rotations are ModelRenderer.rotateAngleX/Y/Z fields.
 */
@Mixin(ModelBiped.class)
public class BipedModelMixin implements ICarrying {

    @Unique
    private boolean isCarryingBlock;

    @Unique
    private boolean isCarryingEntity;

    @Shadow
    public ModelRenderer bipedRightArm;

    @Shadow
    public ModelRenderer bipedLeftArm;

    @Shadow
    public boolean isSneak;

    @Override
    public boolean isCarryingBlock() {
        return isCarryingBlock;
    }

    @Override
    public boolean isCarryingEntity() {
        return isCarryingEntity;
    }

    @Override
    public void setCarryingBlock(boolean isCarrying) {
        this.isCarryingBlock = isCarrying;
    }

    @Override
    public void setCarryingEntity(boolean isCarrying) {
        this.isCarryingEntity = isCarrying;
    }

    /**
     * Hooks into setRotationAngles to modify arm poses when carrying.
     * ModelBiped.setRotationAngles signature in 1.6.4:
     *   setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity)
     */
    @Inject(method = "setRotationAngles", at = @At("RETURN"))
    public void onSetAngles(float limbSwing, float limbSwingAmount, float ageInTicks,
                            float netHeadYaw, float headPitch, float scaleFactor,
                            Entity entity, CallbackInfo info) {
        if (this.isCarryingBlock()) {
            bipedRightArm.rotateAngleX = -1F + (this.isSneak ? 0f : 0.2f);
            bipedLeftArm.rotateAngleX = -1F + (this.isSneak ? 0f : 0.2f);
            bipedRightArm.rotateAngleZ = 0f;
            bipedLeftArm.rotateAngleZ = 0f;
            bipedRightArm.rotateAngleY = 0f;
            bipedLeftArm.rotateAngleY = 0f;
        } else if (this.isCarryingEntity()) {
            bipedRightArm.rotateAngleX = -1.2F + (this.isSneak ? 0f : 0.2f);
            bipedLeftArm.rotateAngleX = -1.2F + (this.isSneak ? 0f : 0.2f);
            bipedRightArm.rotateAngleZ = -0.15f;
            bipedLeftArm.rotateAngleZ = 0.15f;
            bipedRightArm.rotateAngleY = 0f;
            bipedLeftArm.rotateAngleY = 0f;
        }
    }


}