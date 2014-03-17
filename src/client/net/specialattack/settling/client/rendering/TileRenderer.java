
package net.specialattack.settling.client.rendering;

import net.specialattack.settling.client.texture.SubTexture;
import net.specialattack.settling.client.texture.TextureRegistry;
import net.specialattack.settling.common.world.World;

import org.lwjgl.opengl.GL11;

public class TileRenderer {

    public static void renderTile(World world, int posX, int posZ) {
        SubTexture texture = TextureRegistry.getSubTexture("missingno");

        GL11.glBegin(GL11.GL_QUADS);

        float startU = texture.getStartU();
        float startV = texture.getStartV();
        float endU = texture.getEndU();
        float endV = texture.getEndV();

        float startX = (float) posX;
        float startZ = (float) posZ;
        float endX = startX + 1.0F;
        float endZ = startZ + 1.0F;

        // if (false) {
        // float temp = startY;
        // startY = startZ;
        // startZ = temp;
        // 
        // temp = endY;
        // endY = endZ;
        // endZ = temp;
        // }

        GL11.glTexCoord2f(startU, startV);
        // GL11.glVertex3f(25.0F, 0.0F, 0.0F);
        GL11.glVertex3f(startX, 0.0F, startZ);

        GL11.glTexCoord2f(endU, startV);
        // GL11.glVertex3f(50.0F, 12.5F, 0.0F);
        GL11.glVertex3f(endX, 0.0F, startZ);

        GL11.glTexCoord2f(endU, endV);
        // GL11.glVertex3f(25.5F, 25.2F, 0.0F); // 25.2F to prevent borders
        GL11.glVertex3f(endX, 0.0F, endZ);

        GL11.glTexCoord2f(startU, endV);
        // GL11.glVertex3f(0.0F, 12.5F, 0.0F);
        GL11.glVertex3f(startX, 0.0F, endZ);

        GL11.glEnd();
    }

}
