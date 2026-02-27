package tschipp.carryon.render;

import net.minecraft.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.items.ItemTile;

/**
 * Renders the carried block on the player's head area (third-person view).
 * In MITE 1.6.4 there's no "layer" system like 1.14+; rendering is done
 * from RenderPlayer.renderSpecials instead.
 *
 * This class provides static methods called from the PlayerRendererMixin.
 */
public class BlockRendererLayer {

    public static void renderThirdPerson(AbstractClientPlayer player, float partialTicks) {
        ItemStack stack = player.getHeldItemStack();
        if (stack == null || stack.getItem() != CarryOnEvents.TILE_ITEM) return;
        if (!ItemTile.hasTileData(stack)) return;

        Block block = ItemTile.getBlock(stack);
        if (block == null || block.blockID == 0) return;

        int meta = ItemTile.getMeta(stack);

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        // Save the entire GL state so we don't contaminate the surrounding render pipeline
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Must bind the block texture atlas BEFORE any rendering - this is what
        // prevents stray item/entity textures from bleeding into the block render
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        // Reset color to white to avoid tinting from previous renders
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();
        setLightCoords(player);
        GL11.glPushMatrix();
        GL11.glRotated(180, 1, 0, 0);
        GL11.glRotated(180, 0, 1, 0);
        GL11.glScaled(0.6, 0.6, 0.6);
        GL11.glTranslated(0, -0.75, -0.65);

        if (player.isSneaking()) {
            GL11.glTranslated(0, -0.15, -0.15);
        }

        if (isChest(block)) {
            GL11.glRotated(180, 0, 1, 0);
            renderChestWithMeta(block, meta, 1.0f);
        } else {
            // Compensate for direction facing encoded in meta.
            // renderBlockAsItem always renders as if facing SOUTH; rotate Y to match actual facing.
            applyDirectionRotation(block, meta);
            RenderBlocks renderBlocks = new RenderBlocks();
            renderBlocks.renderBlockAsItem(block, meta, 1.0f);
        }

        GL11.glPopMatrix();

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        // Restore all saved GL state - this undoes texture bindings, alpha test, blend, etc.
        GL11.glPopAttrib();
    }

    public static void renderFirstPerson(EntityLivingBase entity, ItemStack stack, float partialTicks) {
        if (stack == null || stack.getItem() != CarryOnEvents.TILE_ITEM) return;
        if (!ItemTile.hasTileData(stack)) return;

        Block block = ItemTile.getBlock(stack);
        if (block == null || block.blockID == 0) return;

        int meta = ItemTile.getMeta(stack);

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // Bind block texture atlas before rendering
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();
        setLightCoords(entity);

        GL11.glPushMatrix();
        // Scale down and push further away for a natural carry look
        GL11.glScaled(1.6, 1.6, 1.6);
        GL11.glTranslated(0, -0.55, -1.4);

        if (isChest(block)) {
            GL11.glRotated(180, 0, 1, 0);
            renderChestWithMeta(block, meta, 1.0f);
        } else {
            // Compensate for direction facing encoded in meta.
            // renderBlockAsItem always renders as if facing SOUTH; rotate Y to match actual facing.
            applyDirectionRotation(block, meta);
            RenderBlocks renderBlocks = new RenderBlocks();
            renderBlocks.renderBlockAsItem(block, meta, 1.0f);
        }

        GL11.glPopMatrix();

        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopAttrib();
    }

    public static boolean isChest(Block block) {
        return block == Block.chest || block == Block.enderChest || block == Block.chestTrapped
                || block == Block.chestCopper || block == Block.chestSilver || block == Block.chestGold
                || block == Block.chestIron || block == Block.chestMithril || block == Block.chestAdamantium
                || block == Block.chestAncientMetal;
    }

    /**
     * Renders a chest block with the correct facing from meta.
     *
     * renderBlockAsItem hard-codes glRotate(90, Y) for renderType 22 and then
     * calls ChestItemRenderHelper without any world context, so the TileEntity
     * renderer always falls back to meta=0 (facing south).
     *
     * We replicate the transform from TileEntityChestRenderer but substitute
     * the correct meta-derived angle:
     *   meta 2 = NORTH → 180°
     *   meta 3 = SOUTH →   0°
     *   meta 4 = WEST  →  90°
     *   meta 5 = EAST  → -90°
     */
    private static void renderChestWithMeta(Block block, int meta, float brightness) {
        // renderBlockAsItem for renderType 22 (chest) hard-codes glRotatef(90, Y) before
        // calling ChestItemRenderHelper, so the net angle we need to add is:
        //   meta 2 (NORTH) → target 180° - hardcoded 90° = +90°
        //   meta 3 (SOUTH) → target   0° - hardcoded 90° = -90°
        //   meta 4 (WEST)  → target  90° - hardcoded 90° =   0°
        //   meta 5 (EAST)  → target -90° - hardcoded 90° = -180°
        float extra;
        if      (meta == 2) extra =  90f;
        else if (meta == 3) extra = -90f;
        else if (meta == 4) extra =   0f;
        else if (meta == 5) extra = 180f;
        else                extra = -90f; // fallback: treat as SOUTH

        if (extra != 0f) {
            GL11.glRotatef(extra, 0f, 1f, 0f);
        }

        RenderBlocks renderBlocks = new RenderBlocks();
        renderBlocks.renderBlockAsItem(block, meta, brightness);
    }

    /**
     * Rotates the GL matrix around Y to compensate for the direction facing stored in meta.
     * renderBlockAsItem always renders as if the block faces SOUTH (+Z).
     * We rotate to match the actual facing direction so the carried block looks correct.
     *
     * EnumDirection ordinals: SOUTH=0, NORTH=1, EAST=2, WEST=3
     * Y rotation needed (to make SOUTH-rendered mesh appear as the correct face):
     *   SOUTH →   0°  (no change)
     *   WEST  →  90°
     *   NORTH → 180°
     *   EAST  → 270°
     */
    private static void applyDirectionRotation(Block block, int meta) {
        EnumDirection dir = block.getDirectionFacing(meta);
        if (dir == null) return;
        double yRot = 0;
        if (dir == EnumDirection.WEST)  yRot =  90;
        else if (dir == EnumDirection.NORTH) yRot = 180;
        else if (dir == EnumDirection.EAST)  yRot = 270;
        if (yRot != 0) {
            GL11.glRotated(yRot, 0, 1, 0);
        }
    }

    private static void setLightCoords(EntityLivingBase player) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null) return;
        int x = (int) player.posX;
        int y = (int) (player.posY + player.getEyeHeight());
        int z = (int) player.posZ;
        int lightValue = mc.theWorld.getLightBrightnessForSkyBlocks(x, y, z, 0);
        float var3 = (float) (lightValue & 0xFFFF);
        float var4 = (float) (lightValue >> 16);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var3, var4);
    }
}