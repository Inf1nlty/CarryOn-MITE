package tschipp.carryon.scripting;

import net.minecraft.NBTTagCompound;

/**
 * MITE 1.6.4 port of CarryOnOverride.
 *
 * The original used 1.14 Forge script overrides with advancements, gamestages, etc.
 * Those features don't exist in MITE 1.6.4, so they are preserved as stubs.
 *
 * Features retained: block/entity name, meta, NBT matching, basic conditions.
 * Features not reproducible: advancements, gamestages, scoreboard, gamemode.
 */
public class CarryOnOverride {

    // BLOCKS
    private NBTTagCompound typeBlockTag;
    private String typeMeta;
    private String typeNameBlock;
    private String typeMaterial;
    private String typeHardness;
    private String typeResistance;

    // ENTITIES
    private NBTTagCompound typeEntityTag;
    private String typeNameEntity;
    private String typeHeight;
    private String typeWidth;
    private String typeHealth;

    // CONDITIONS (partially supported in MITE 1.6.4)
    private String conditionXp;
    // Not supported in MITE 1.6.4:
    // private String conditionGamestage;
    // private String conditionAchievement;
    // private String conditionGamemode;
    // private String conditionScoreboard;
    // private String conditionPosition;
    // private String conditionEffects;

    // RENDER
    private String renderNameBlock;
    private String renderNameEntity;
    private int renderMeta;
    private NBTTagCompound renderNBT;
    private String renderTranslation;
    private String renderRotation;
    private String renderScale;
    private String renderRotationLeftArm;
    private String renderRotationRightArm;
    private boolean renderLeftArm = true;
    private boolean renderRightArm = true;

    // EFFECTS
    private String commandInit;
    private String commandLoop;
    private String commandPlace;

    private boolean isBlock;
    private boolean isEntity;

    private final String origin;

    public CarryOnOverride(String origin) {
        this.origin = origin;
    }

    public boolean isBlock() { return isBlock; }
    public boolean isEntity() { return isEntity; }
    public void setBlock(boolean v) { this.isBlock = v; }
    public void setEntity(boolean v) { this.isEntity = v; }

    public NBTTagCompound getTypeBlockTag() { return typeBlockTag; }
    public void setTypeBlockTag(NBTTagCompound v) { this.typeBlockTag = v; }

    public String getTypeMeta() { return typeMeta; }
    public void setTypeMeta(String v) { this.typeMeta = v; }

    public String getTypeNameBlock() { return typeNameBlock; }
    public void setTypeNameBlock(String v) { this.typeNameBlock = v; }

    public String getTypeMaterial() { return typeMaterial; }
    public void setTypeMaterial(String v) { this.typeMaterial = v; }

    public String getTypeHardness() { return typeHardness; }
    public void setTypeHardness(String v) { this.typeHardness = v; }

    public String getTypeResistance() { return typeResistance; }
    public void setTypeResistance(String v) { this.typeResistance = v; }

    public NBTTagCompound getTypeEntityTag() { return typeEntityTag; }
    public void setTypeEntityTag(NBTTagCompound v) { this.typeEntityTag = v; }

    public String getTypeNameEntity() { return typeNameEntity; }
    public void setTypeNameEntity(String v) { this.typeNameEntity = v; }

    public String getTypeHeight() { return typeHeight; }
    public void setTypeHeight(String v) { this.typeHeight = v; }

    public String getTypeWidth() { return typeWidth; }
    public void setTypeWidth(String v) { this.typeWidth = v; }

    public String getTypeHealth() { return typeHealth; }
    public void setTypeHealth(String v) { this.typeHealth = v; }

    public String getConditionXp() { return conditionXp; }
    public void setConditionXp(String v) { this.conditionXp = v; }

    // Stub for unsupported conditions (always returns null/empty)
    public String getConditionGamestage() { return null; }
    public String getConditionAchievement() { return null; }
    public String getConditionGamemode() { return null; }
    public String getConditionScoreboard() { return null; }
    public String getConditionPosition() { return null; }
    public String getConditionEffects() { return null; }

    public String getRenderNameBlock() { return renderNameBlock; }
    public void setRenderNameBlock(String v) { this.renderNameBlock = v; }

    public String getRenderNameEntity() { return renderNameEntity; }
    public void setRenderNameEntity(String v) { this.renderNameEntity = v; }

    public int getRenderMeta() { return renderMeta; }
    public void setRenderMeta(int v) { this.renderMeta = v; }

    public NBTTagCompound getRenderNBT() { return renderNBT; }
    public void setRenderNBT(NBTTagCompound v) { this.renderNBT = v; }

    public String getRenderTranslation() { return renderTranslation; }
    public void setRenderTranslation(String v) { this.renderTranslation = v; }

    public String getRenderRotation() { return renderRotation; }
    public void setRenderRotation(String v) { this.renderRotation = v; }

    public String getRenderScale() { return renderScale; }
    public void setRenderScale(String v) { this.renderScale = v; }

    public String getRenderRotationLeftArm() { return renderRotationLeftArm; }
    public void setRenderRotationLeftArm(String v) { this.renderRotationLeftArm = v; }

    public String getRenderRotationRightArm() { return renderRotationRightArm; }
    public void setRenderRotationRightArm(String v) { this.renderRotationRightArm = v; }

    public boolean isRenderLeftArm() { return renderLeftArm; }
    public void setRenderLeftArm(boolean v) { this.renderLeftArm = v; }

    public boolean isRenderRightArm() { return renderRightArm; }
    public void setRenderRightArm(boolean v) { this.renderRightArm = v; }

    public String getCommandInit() { return commandInit; }
    public void setCommandInit(String v) { this.commandInit = v; }

    public String getCommandLoop() { return commandLoop; }
    public void setCommandLoop(String v) { this.commandLoop = v; }

    public String getCommandPlace() { return commandPlace; }
    public void setCommandPlace(String v) { this.commandPlace = v; }

    public String getOrigin() { return origin; }

    @Override
    public int hashCode() {
        return origin != null ? origin.hashCode() : 0;
    }
}
