
package net.specialattack.settling.client.gui;

public class GuiScreenMainMenu extends GuiScreen {

    private GuiButton buttonTest;

    @Override
    public void onResize(int newWidth, int newHeight) {
        this.buttonTest.posX = newWidth / 2 - 200;
    }

    @Override
    public void onInit() {
        buttonTest = new GuiButton("Test", this.width / 2 - 200, 10, 400, 50, this);

        this.elements.add(buttonTest);
    }

    @Override
    public void drawBackground() {
        // TODO: create fancy background, low priority
    }

    @Override
    public void onRender(int mouseX, int mouseY) {}

    @Override
    protected void onAction(GuiElement element, int mouseX, int mouseZ, int mouseButton) {
        if (element == buttonTest) {
            buttonTest.enabled = false;
            buttonTest.label = "Button: " + mouseButton;
        }
    }

}
