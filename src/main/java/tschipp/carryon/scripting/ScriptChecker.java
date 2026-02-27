package tschipp.carryon.scripting;

import javax.annotation.Nullable;

import net.minecraft.*;
import tschipp.carryon.interfaces.ICarryOnData;

/**
 * MITE 1.6.4 port of ScriptChecker.
 *
 * Features NOT available in MITE 1.6.4 (always pass/ignored):
 * - Advancement/Achievement conditions (no advancement system)
 * - GameStages (mod not available)
 * - Gamemode conditions (different system)
 * - Scoreboard conditions (different)
 * - Position conditions (BlockPos doesn't exist)
 * - Potion effect conditions (simplified)
 *
 * Features retained:
 * - Block name/meta/material/hardness/NBT matching
 * - Entity name/height/width/health/NBT matching
 * - XP conditions
 * - Script-driven commands (run via /say equivalent)
 */
public class ScriptChecker {

    @Nullable
    public static CarryOnOverride inspectBlock(Block block, int meta, World world, int x, int y, int z,
            @Nullable NBTTagCompound nbt) {
        if (ScriptReader.OVERRIDES.isEmpty())
            return null;

        String blockName = block.getUnlocalizedName();
        float hardness = block.getBlockHardness(meta);
        float resistance = block.getExplosionResistance(null);
        Material material = block.blockMaterial;

        for (CarryOnOverride override : ScriptReader.OVERRIDES.values()) {
            if (override.isBlock()) {
                if (matchesAll(override, block, blockName, meta, material, hardness, resistance, nbt)) {
                    return override;
                }
            }
        }
        return null;
    }

    @Nullable
    public static CarryOnOverride inspectEntity(Entity entity) {
        if (ScriptReader.OVERRIDES.isEmpty())
            return null;

        String name = EntityList.getEntityString(entity);
        if (name == null) return null;
        float height = entity.height;
        float width = entity.width;
        float health = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).getHealth() : 0.0f;
        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);

        for (CarryOnOverride override : ScriptReader.OVERRIDES.values()) {
            if (override.isEntity()) {
                if (matchesAll(override, name, height, width, health, tag))
                    return override;
            }
        }
        return null;
    }

    public static boolean matchesAll(CarryOnOverride override, String name, float height, float width, float health,
            NBTTagCompound tag) {
        boolean matchname = override.getTypeNameEntity() == null || name.equals(override.getTypeNameEntity());
        boolean matchheight = ScriptParseHelper.matches(height, override.getTypeHeight());
        boolean matchwidth = ScriptParseHelper.matches(width, override.getTypeWidth());
        boolean matchhealth = ScriptParseHelper.matches(health, override.getTypeHealth());
        boolean matchnbt = ScriptParseHelper.matches(tag, override.getTypeEntityTag());
        return matchname && matchheight && matchwidth && matchhealth && matchnbt;
    }

    public static boolean matchesAll(CarryOnOverride override, Block block, String blockName, int meta,
            Material material, float hardness, float resistance, NBTTagCompound nbt) {
        boolean matchblock = ScriptParseHelper.matchesBlock(block, blockName, override.getTypeNameBlock());
        boolean matchmeta = ScriptParseHelper.matches(meta, override.getTypeMeta());
        boolean matchmaterial = ScriptParseHelper.matchesMaterial(material, override.getTypeMaterial());
        boolean matchhardness = ScriptParseHelper.matches(hardness, override.getTypeHardness());
        boolean matchresistance = ScriptParseHelper.matches(resistance, override.getTypeResistance());
        boolean matchnbt = ScriptParseHelper.matches(nbt, override.getTypeBlockTag());
        return matchblock && matchmeta && matchmaterial && matchhardness && matchresistance && matchnbt;
    }

    /**
     * Checks condition fields on the override against the player.
     * In MITE 1.6.4: only XP is supported; other conditions are ignored (always true).
     */
    public static boolean fulfillsConditions(CarryOnOverride override, EntityPlayer player) {
        // XP check (MITE 1.6.4 uses player.experience)
        boolean xp = ScriptParseHelper.matches(player.experience, override.getConditionXp());
        // All unsupported conditions default to true
        return xp;
    }

    @Nullable
    public static CarryOnOverride getOverride(EntityPlayer player) {
        // Store override key in our CarryOnData NBT
        ICarryOnData data = (ICarryOnData) player;
        NBTTagCompound tag = data.getCarryOnData();
        if (tag != null && tag.hasKey("overrideKey")) {
            int key = tag.getInteger("overrideKey");
            return ScriptReader.OVERRIDES.get(key);
        }
        return null;
    }

    public static void setCarryOnOverride(EntityPlayer player, int key) {
        ICarryOnData data = (ICarryOnData) player;
        NBTTagCompound tag = data.getCarryOnData();
        if (tag == null) tag = new NBTTagCompound();
        tag.setInteger("overrideKey", key);
        data.setCarryOnData(tag);
    }

}
