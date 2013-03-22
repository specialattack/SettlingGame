
package net.specialattack.settling.client.gui;

import net.specialattack.settling.client.SettlingClient;
import net.specialattack.settling.client.texture.TextureRegistry;
import net.specialattack.settling.client.world.WorldDemo;
import net.specialattack.settling.common.Settling;

public class GuiScreenMainMenu extends GuiScreen {

    private GuiButton buttonPlay;
    private GuiButton buttonExit;

    @Override
    public void onResize(int newWidth, int newHeight) {
        this.buttonPlay.posX = newWidth / 2 - 200;
        this.buttonExit.posX = newWidth / 2 - 200;
    }

    @Override
    public void onInit() {
        buttonPlay = new GuiButton("Play, demo world only", this.width / 2 - 200, 350, 400, 50, this);
        buttonExit = new GuiButton("Exit", this.width / 2 - 200, 410, 400, 50, this);

        this.elements.add(buttonPlay);
        this.elements.add(buttonExit);
    }

    @Override
    public void drawBackground() {
        // TODO: create fancy background, low priority
    }

    @Override
    public void onRender(int mouseX, int mouseY) {
        TextureRegistry.getTexture("/textures/settling.png").bindTexture();

        GuiHelper.drawTexturedRectangle((float) width / 2.0F - 300, 40.0F, 600.0F, 300.0F, 0.0F, 0.0F, 1.0F, 1.0F);
    }

    @Override
    protected void onAction(GuiElement element, int mouseX, int mouseZ, int mouseButton) {
        if (element == buttonPlay) {
            SettlingClient.instance.currentWorld = new WorldDemo(null); // XXX: Not the way this will be done
            SettlingClient.instance.markChunksDirty();
        }
        if (element == buttonExit) {
            Settling.getInstance().attemptShutdown();
        }
    }

}