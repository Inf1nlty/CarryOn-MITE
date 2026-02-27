package tschipp.carryon.scripting;

import net.minecraft.*;

/**
 * MITE 1.6.4 port of ScriptParseHelper.
 *
 * Provides static utility methods for matching script override conditions
 * against block/entity properties.
 */
public class ScriptParseHelper {

    /**
     * Matches a float value against a range/value string like "0.5", ">1.0", "<2.0", "1.0-2.0"
     */
    public static boolean matches(float value, String condition) {
        if (condition == null || condition.isEmpty()) return true;
        condition = condition.trim();
        try {
            if (condition.startsWith(">")) {
                float threshold = Float.parseFloat(condition.substring(1).trim());
                return value > threshold;
            } else if (condition.startsWith("<")) {
                float threshold = Float.parseFloat(condition.substring(1).trim());
                return value < threshold;
            } else if (condition.contains("-")) {
                String[] parts = condition.split("-");
                float min = Float.parseFloat(parts[0].trim());
                float max = Float.parseFloat(parts[1].trim());
                return value >= min && value <= max;
            } else {
                float expected = Float.parseFloat(condition);
                return Math.abs(value - expected) < 0.001f;
            }
        } catch (NumberFormatException e) {
            return true; // If parse fails, allow through
        }
    }

    /**
     * Matches an int value against a range/value string.
     */
    public static boolean matches(int value, String condition) {
        return matches((float) value, condition);
    }

    /**
     * Matches NBT tags - checks if all keys in the condition tag exist and match in the entity/block tag.
     */
    public static boolean matches(NBTTagCompound tag, NBTTagCompound condition) {
        if (condition == null || condition.hasNoTags()) return true;
        if (tag == null) return false;
        for (Object key : condition.getTags()) {
            NBTBase conditionValue = (NBTBase) key;
            String k = conditionValue.getName();
            if (!tag.hasKey(k)) return false;
            if (!tag.getTag(k).toString().equals(condition.getTag(k).toString())) return false;
        }
        return true;
    }

    /**
     * Matches a block against a name condition string.
     */
    public static boolean matchesBlock(Block block, String blockName, String condition) {
        if (condition == null || condition.isEmpty()) return true;
        return blockName != null && (blockName.equalsIgnoreCase(condition) || blockName.contains(condition));
    }

    /**
     * Matches a Material against a material name string.
     * In MITE 1.6.4, Material names are matched by class name.
     */
    public static boolean matchesMaterial(Material material, String condition) {
        if (condition == null || condition.isEmpty()) return true;
        if (material == null) return false;
        // Get material name from its class name
        String matName = material.getClass().getSimpleName().toLowerCase().replace("material", "");
        return matName.equalsIgnoreCase(condition);
    }

}
