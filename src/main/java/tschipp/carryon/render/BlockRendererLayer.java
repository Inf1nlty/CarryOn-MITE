package tschipp.carryon.render;

import net.minecraft.*;
import org.lwjgl.opengl.GL11;
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
        }

        setLightCoords(player);
        RenderBlocks renderBlocks = new RenderBlocks();
        renderBlocks.renderBlockAsItem(block, meta, 1.0f);

        GL11.glPopMatrix();
    }

    public static void renderFirstPerson(EntityLivingBase entity, ItemStack stack, float partialTicks) {
        if (stack == null || stack.getItem() != CarryOnEvents.TILE_ITEM) return;
        if (!ItemTile.hasTileData(stack)) return;

        Block block = ItemTile.getBlock(stack);
        if (block == null || block.blockID == 0) return;

        int meta = ItemTile.getMeta(stack);

        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null) return;

        RenderHelper.enableStandardItemLighting();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

        GL11.glPushMatrix();
        // Scale down and push further away for a natural carry look
        GL11.glScaled(1.6, 1.6, 1.6);
        GL11.glTranslated(0, -0.55, -1.4);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        if (isChest(block)) {
            GL11.glRotated(180, 0, 1, 0);
        }

        setLightCoords(entity);
        RenderBlocks renderBlocks = new RenderBlocks();
        renderBlocks.renderBlockAsItem(block, meta, 1.0f);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    public static boolean isChest(Block block) {
        return block == Block.chest || block == Block.enderChest || block == Block.chestTrapped;
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