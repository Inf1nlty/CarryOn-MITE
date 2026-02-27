package tschipp.carryon.scripting;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.minecraft.NBTTagCompound;

/**
 * MITE 1.6.4 port of ScriptReader.
 *
 * Reads CarryOn override scripts from a configration directory.
 * In MITE 1.6.4, the config directory is determined at mod initialization
 * instead of FML's PreInit event.
 *
 * Features retained: block/entity overrides, render overrides, command effects.
 * Features NOT supported: advancements, gamestages (no system available in MITE 1.6.4).
 */
public class ScriptReader {

    private static ArrayList<File> scripts = new ArrayList<>();
    public static HashMap<Integer, CarryOnOverride> OVERRIDES = new HashMap<>();

    private static File configDir;

    public static void init(File configDirectory) {
        configDir = new File(configDirectory, "carryon-scripts/");
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        scripts.clear();
        for (File file : configDir.listFiles()) {
            if (file.getName().endsWith(".json")) {
                scripts.add(file);
            }
        }
        try {
            parseScripts();
        } catch (Exception e) {
            System.err.println("[CarryOn] Error parsing scripts: " + e.getMessage());
        }
    }

    public static void parseScripts() throws JsonIOException, JsonSyntaxException, Exception {
        for (File file : scripts) {
            boolean errored = false;
            int hash = file.getAbsolutePath().hashCode();

            JsonParser parser = new JsonParser();
            JsonObject json = (JsonObject) parser.parse(new FileReader(file.getAbsolutePath()));

            JsonObject object = (JsonObject) json.get("object");
            JsonObject conditions = (JsonObject) json.get("conditions");
            JsonObject render = (JsonObject) json.get("render");
            JsonObject effects = (JsonObject) json.get("effects");

            if ((object != null && conditions != null) || (object != null && render != null)
                    || (object != null && effects != null)) {

                JsonObject block = (JsonObject) object.get("block");
                JsonObject entity = (JsonObject) object.get("entity");

                if ((block == null && entity == null) || (block != null && entity != null)) {
                    errored = true;
                }

                if (!errored) {
                    CarryOnOverride override = new CarryOnOverride(file.getAbsolutePath());

                    if (block != null) {
                        override.setBlock(true);
                        setIfPresent(override::setTypeNameBlock, block.get("name"));
                        setIfPresent(override::setTypeMeta, block.get("meta"));
                        setIfPresent(override::setTypeMaterial, block.get("material"));
                        setIfPresent(override::setTypeHardness, block.get("hardness"));
                        setIfPresent(override::setTypeResistance, block.get("resistance"));
                        JsonObject nbt = (JsonObject) block.get("nbt");
                        if (nbt != null) {
                            override.setTypeBlockTag(jsonToNBT(nbt));
                        }
                    } else {
                        override.setEntity(true);
                        setIfPresent(override::setTypeNameEntity, entity.get("name"));
                        setIfPresent(override::setTypeHealth, entity.get("health"));
                        setIfPresent(override::setTypeHeight, entity.get("height"));
                        setIfPresent(override::setTypeWidth, entity.get("width"));
                        JsonObject nbt = (JsonObject) entity.get("nbt");
                        if (nbt != null) {
                            override.setTypeEntityTag(jsonToNBT(nbt));
                        }
                    }

                    if (conditions != null) {
                        setIfPresent(override::setConditionXp, conditions.get("xp"));
                        // Unsupported conditions are silently ignored:
                        // gamestage, advancement, gamemode, scoreboard, position, effects
                    }

                    if (render != null) {
                        setIfPresent(override::setRenderNameBlock, render.get("name_block"));
                        setIfPresent(override::setRenderNameEntity, render.get("name_entity"));
                        setIfPresent(override::setRenderTranslation, render.get("translation"));
                        setIfPresent(override::setRenderRotation, render.get("rotation"));
                        setIfPresent(override::setRenderScale, render.get("scale"));
                        setIfPresent(override::setRenderRotationLeftArm, render.get("rotation_left_arm"));
                        setIfPresent(override::setRenderRotationRightArm, render.get("rotation_right_arm"));
                        JsonElement meta = render.get("meta");
                        if (meta != null) override.setRenderMeta(meta.getAsInt());
                        JsonElement renderLeftArm = render.get("render_left_arm");
                        if (renderLeftArm != null) override.setRenderLeftArm(renderLeftArm.getAsBoolean());
                        JsonElement renderRightArm = render.get("render_right_arm");
                        if (renderRightArm != null) override.setRenderRightArm(renderRightArm.getAsBoolean());
                        JsonObject nbt = (JsonObject) render.get("nbt");
                        if (nbt != null) override.setRenderNBT(jsonToNBT(nbt));
                    }

                    if (effects != null) {
                        setIfPresent(override::setCommandInit, effects.get("commandPickup"));
                        setIfPresent(override::setCommandLoop, effects.get("commandLoop"));
                        setIfPresent(override::setCommandPlace, effects.get("commandPlace"));
                    }

                    OVERRIDES.put(hash, override);
                }
            }
        }
    }

    private static void setIfPresent(java.util.function.Consumer<String> setter, JsonElement element) {
        if (element != null) setter.accept(element.getAsString());
    }

    /**
     * Simple JSON to NBTTagCompound converter for script NBT conditions.
     * MITE 1.6.4 doesn't have JsonToNBT, so we implement a simple version.
     */
    private static NBTTagCompound jsonToNBT(JsonObject json) {
        NBTTagCompound tag = new NBTTagCompound();
        for (java.util.Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            if (value.isJsonPrimitive()) {
                if (value.getAsJsonPrimitive().isBoolean()) {
                    tag.setBoolean(key, value.getAsBoolean());
                } else if (value.getAsJsonPrimitive().isNumber()) {
                    double d = value.getAsDouble();
                    if (d == Math.floor(d)) {
                        tag.setLong(key, value.getAsLong());
                    } else {
                        tag.setDouble(key, d);
                    }
                } else {
                    tag.setString(key, value.getAsString());
                }
            } else if (value.isJsonObject()) {
                tag.setCompoundTag(key, jsonToNBT(value.getAsJsonObject()));
            }
        }
        return tag;
    }
}
