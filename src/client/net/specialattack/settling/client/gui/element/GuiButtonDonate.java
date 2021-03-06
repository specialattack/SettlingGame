
package net.specialattack.settling.client.gui.element;

import net.specialattack.settling.client.gui.GuiHelper;
import net.specialattack.settling.client.gui.GuiScreen;
import net.specialattack.settling.client.texture.TextureRegistry;

import org.lwjgl.opengl.GL11;

public class GuiButtonDonate extends GuiButton {

    public GuiButtonDonate(String label, int posX, int posY, int width, int height, GuiScreen screen) {
        super(label, posX, posY, width, height, screen);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        TextureRegistry.getTexture("/textures/gui/controls.png").bindTexture();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        float vAdd = 0.0F;
        int color = 0xFFFF00FF;

        if (mouseX >= this.posX && mouseX <= this.posX + this.width && mouseY >= this.posY && mouseY <= this.posY + this.height) {
            vAdd = 0.0625F;
        }

        GuiHelper.drawTexturedRectangle(this.posX, this.posY, this.width, this.height, 0.5F, 0.0F + vAdd, 0.75F, 0.0625F + vAdd);

        float textTop = (float) this.posY + (float) this.height / 2.0F - this.screen.font.fontSize / 2.0F;
        float textLeft = (float) this.posX + (float) this.width / 2.0F - (float) this.screen.font.getStringWidth(this.label) / 2.0F;

        this.screen.font.renderStringWithShadow(this.label, (int) textLeft, (int) textTop, color);

    }

    @Override
    public boolean mouseClicked(int mouseButton, int mouseX, int mouseY) {
        if (!this.enabled || mouseButton != 0) {
            return false;
        }

        if (mouseX >= this.posX && mouseX <= this.posX + this.width && mouseY >= this.posY && mouseY <= this.posY + this.height) {
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, char character) {
        return false;
    }

    @Override
    public boolean mouseScrolled(int dir) {
        return false;
    }

}
