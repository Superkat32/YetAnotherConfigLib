package dev.isxander.yacl3.gui.controllers;

import dev.isxander.yacl3.gui.YACLScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;

public class PopupControllerScreen extends Screen {
    private final YACLScreen backgroundYaclScreen;
    private final ControllerPopupWidget<?> controllerPopup;
    public PopupControllerScreen(YACLScreen backgroundYaclScreen, ControllerPopupWidget<?> controllerPopup) {
        super(controllerPopup.popupTitle()); //Gets narrated by the narrator
        this.backgroundYaclScreen = backgroundYaclScreen;
        this.controllerPopup = controllerPopup;
    }


    @Override
    protected void init() {
        this.addRenderableWidget(this.controllerPopup);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        minecraft.setScreen(backgroundYaclScreen);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        controllerPopup.renderBackground(graphics, mouseX, mouseY, delta);
        this.backgroundYaclScreen.render(graphics, -1, -1, delta); //mouseX/Y set to -1 to prevent hovering outlines

        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(
            GuiGraphics guiGraphics
            /*? if >1.20.1 {*/,
            int mouseX,
            int mouseY,
            float partialTick
            /*?}*/
    ) {

    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, /*? if >1.20.1 {*/ double scrollX, /*?}*/ double scrollY) {
        backgroundYaclScreen.mouseScrolled(mouseX, mouseY, /*? if >1.20.1 {*/ scrollX, /*?}*/ scrollY); //mouseX & mouseY are needed here
        return super.mouseScrolled(mouseX, mouseY, /*? if >1.20.1 {*/ scrollX, /*?}*/ scrollY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return controllerPopup.charTyped(codePoint, modifiers);
    }


    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return controllerPopup.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.screen = backgroundYaclScreen;
    }

}
