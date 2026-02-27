package tschipp.carryon.render;

import net.minecraft.*;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import tschipp.carryon.CarryOnEvents;
import tschipp.carryon.items.ItemTile;

public class BlockRendererLayer {

    public static void renderThirdPerson(AbstractClientPlayer player, float partialTicks)
    {
        ItemStack stack = player.getHeldItemStack();

        if (stack == null || stack.getItem() != CarryOnEvents.TILE_ITEM) return;

        if (!ItemTile.hasTileData(stack)) return;

        Block block = ItemTile.getBlock(stack);

        if (block == null || block.blockID == 0) return;

        int meta = ItemTile.getMeta(stack);

        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null) return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);

        RenderHelper.enableStandardItemLighting();
        setLightCoords(player);
        GL11.glPushMatrix();
        GL11.glRotated(180, 1, 0, 0);
        GL11.glRotated(180, 0, 1, 0);
        GL11.glScaled(0.6, 0.6, 0.6);
        GL11.glTranslated(0, -0.75, -0.65);

        if (player.isSneaking()) GL11.glTranslated(0, -0.15, -0.15);

        if (isChest(block))
        {
            GL11.glRotated(180, 0, 1, 0);
            renderChestWithMeta(block, meta, 1.0f);
        }
        else
        {
            applyDirectionRotation(block, meta);
            new RenderBlocks().renderBlockAsItem(block, meta, 1.0f);
        }

        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopAttrib();
    }

    public static void renderFirstPerson(EntityLivingBase entity, ItemStack stack, float partialTicks)
    {
        if (stack == null || stack.getItem() != CarryOnEvents.TILE_ITEM) return;

        if (!ItemTile.hasTileData(stack)) return;

        Block block = ItemTile.getBlock(stack);

        if (block == null || block.blockID == 0) return;

        int meta = ItemTile.getMeta(stack);

        Minecraft mc = Minecraft.getMinecraft();

        if (mc == null) return;

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
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
        GL11.glScaled(1.6, 1.6, 1.6);
        GL11.glTranslated(0, -0.55, -1.4);

        if (isChest(block))
        {
            GL11.glRotated(180, 0, 1, 0);
            renderChestWithMeta(block, meta, 1.0f);
        }
        else
        {
            applyDirectionRotation(block, meta);
            new RenderBlocks().renderBlockAsItem(block, meta, 1.0f);
        }

        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopAttrib();
    }

    public static boolean isChest(Block block)
    {
        return block == Block.chest        || block == Block.enderChest     || block == Block.chestTrapped
            || block == Block.chestCopper  || block == Block.chestSilver    || block == Block.chestGold
            || block == Block.chestIron    || block == Block.chestMithril   || block == Block.chestAdamantium
            || block == Block.chestAncientMetal;
    }

    private static void renderChestWithMeta(Block block, int meta, float brightness)
    {
        float extra;
        if      (meta == 2) extra = 180f;
        else if (meta == 3) extra =   0f;
        else if (meta == 4) extra =  90f;
        else if (meta == 5) extra = -90f;
        else                extra =   0f;

        if (extra != 0f) GL11.glRotatef(extra, 0f, 1f, 0f);
        new RenderBlocks().renderBlockAsItem(block, meta, brightness);
    }

    private static void applyDirectionRotation(Block block, int meta)
    {
        EnumDirection dir = block.getDirectionFacing(meta);
        if (dir == null) return;

        double yRot = 0;
        if      (dir == EnumDirection.NORTH) yRot = 180;
        else if (dir == EnumDirection.WEST)  yRot = 270;
        else if (dir == EnumDirection.EAST)  yRot =  90;

        if (yRot != 0) GL11.glRotated(yRot, 0, 1, 0);
    }

    private static void setLightCoords(EntityLivingBase player)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.theWorld == null) return;

        int x = (int) player.posX;
        int y = (int) (player.posY + player.getEyeHeight());
        int z = (int) player.posZ;
        int lightValue = mc.theWorld.getLightBrightnessForSkyBlocks(x, y, z, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
                (float)(lightValue & 0xFFFF), (float)(lightValue >> 16));
    }
}