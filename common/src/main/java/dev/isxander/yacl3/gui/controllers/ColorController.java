package dev.isxander.yacl3.gui.controllers;

import com.google.common.collect.ImmutableList;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.api.utils.MutableDimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import dev.isxander.yacl3.platform.YACLConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.awt.*;
import java.util.List;

/**
 * A color controller that uses a hex color field as input.
 */
public class ColorController implements IStringController<Color> {
    private final Option<Color> option;
    private final boolean allowAlpha;

    /**
     * Constructs a color controller with {@link ColorController#allowAlpha()} defaulting to false
     *
     * @param option bound option
     */
    public ColorController(Option<Color> option) {
        this(option, false);
    }

    /**
     * Constructs a color controller
     *
     * @param option bound option
     * @param allowAlpha allows the color input to accept alpha values
     */
    public ColorController(Option<Color> option, boolean allowAlpha) {
        this.option = option;
        this.allowAlpha = allowAlpha;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option<Color> option() {
        return option;
    }

    public boolean allowAlpha() {
        return allowAlpha;
    }

    @Override
    public String getString() {
        return formatValue().getString();
    }

    @Override
    public Component formatValue() {
        MutableComponent text = Component.literal("#");
        text.append(Component.literal(toHex(option().pendingValue().getRed())).withStyle(ChatFormatting.RED));
        text.append(Component.literal(toHex(option().pendingValue().getGreen())).withStyle(ChatFormatting.GREEN));
        text.append(Component.literal(toHex(option().pendingValue().getBlue())).withStyle(ChatFormatting.BLUE));
        if (allowAlpha()) text.append(toHex(option().pendingValue().getAlpha()));
        return text;
    }

    private String toHex(int value) {
        String hex = Integer.toString(value, 16).toUpperCase();
        if (hex.length() == 1)
            hex = "0" + hex;
        return hex;
    }

    @Override
    public void setFromString(String value) {
        if (value.startsWith("#"))
            value = value.substring(1);

        int red = Integer.parseInt(value.substring(0, 2), 16);
        int green = Integer.parseInt(value.substring(2, 4), 16);
        int blue = Integer.parseInt(value.substring(4, 6), 16);

        if (allowAlpha()) {
            int alpha = Integer.parseInt(value.substring(6, 8), 16);
            option().requestSet(new Color(red, green, blue, alpha));
        } else {
            option().requestSet(new Color(red, green, blue));
        }
    }

    @Override
    public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> widgetDimension) {
        return new ColorControllerElement(this, screen, widgetDimension);
    }

    public static class ColorControllerElement extends StringControllerElement {
        private final ColorController colorController;
        private ColorPickerWidget colorPickerWidget;

        protected MutableDimension<Integer> colorPreviewDim;
        private final List<Character> allowedChars;
        public boolean hoveredOverColorPreview = false;
        private boolean colorPickerVisible = false;
        private int previewOutlineFadeTicks = 0;

        public ColorControllerElement(ColorController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim, true);
            this.colorController = control;
            this.allowedChars = ImmutableList.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f');
        }

        @Override
        protected void drawValueText(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            hovered = isMouseOver(mouseX, mouseY);

            if (isHovered()) {
                colorPreviewDim.move(-inputFieldBounds.width() - 8, -2);
                colorPreviewDim.expand(4, 4);
                previewOutlineFadeTicks++;
                super.drawValueText(graphics, mouseX, mouseY, delta);
            }

            graphics.fill(colorPreviewDim.x(), colorPreviewDim.y(), colorPreviewDim.xLimit(), colorPreviewDim.yLimit(), colorController.option().pendingValue().getRGB());
            Color outlineColor = getPreviewOutlineColor(hoveredOverColorPreview || isMouseOverColorPreview(mouseX, mouseY));
            drawOutline(graphics, colorPreviewDim.x(), colorPreviewDim.y(), colorPreviewDim.xLimit(), colorPreviewDim.yLimit(), 1, outlineColor.getRGB());
        }

        @Override
        public void write(String string) {
            if (string.startsWith("0x")) string = string.substring(2);
            for (char chr : string.toCharArray()) {
                if (!allowedChars.contains(Character.toLowerCase(chr))) {
                    return;
                }
            }

            if (caretPos == 0)
                return;

            String trimmed = string.substring(0, Math.min(inputField.length() - caretPos, string.length()));

            if (modifyInput(builder -> builder.replace(caretPos, caretPos + trimmed.length(), trimmed))) {
                caretPos += trimmed.length();
                setSelectionLength();
                updateControl();
            }
        }

        @Override
        protected void doBackspace() {
            if (caretPos > 1) {
                if (modifyInput(builder -> builder.setCharAt(caretPos - 1, '0'))) {
                    caretPos--;
                    updateControl();
                }
            }
        }

        @Override
        protected void doDelete() {
            if (caretPos >= 1) {
                if (modifyInput(builder -> builder.setCharAt(caretPos, '0'))) {
                    updateControl();
                }
            }
        }

        @Override
        protected boolean doCut() {
            return false;
        }

        @Override
        protected boolean doCopy() {
            return false;
        }

        @Override
        protected boolean doSelectAll() {
            return false;
        }

        protected void setSelectionLength() {
            selectionLength = caretPos < inputField.length() && caretPos > 0 ? 1 : 0;
        }

        @Override
        protected int getDefaultCaretPos() {
            return colorController.allowAlpha() ? 3 : 1;
        }

        @Override
        public void setDimension(Dimension<Integer> dim) {
            super.setDimension(dim);

            int previewSize = (dim.height() - getYPadding() * 2) / 2;
            colorPreviewDim = Dimension.ofInt(dim.xLimit() - getXPadding() - previewSize, dim.centerY() - previewSize / 2, previewSize, previewSize);

            if(colorPickerWidget != null) {
                colorPickerWidget.setDimension(colorPickerWidget.getDimension().withY(this.getDimension().y()));
                //checks if the color controller is being partially rendered offscreen
                if(this.getDimension().y() < screen.tabArea.top() || this.getDimension().yLimit() > screen.tabArea.bottom()) {
                    removeColorPicker();
                }
            }
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            int prevSelectionLength = selectionLength;
            selectionLength = 0;
            if (super.keyPressed(keyCode, scanCode, modifiers)) {
                caretPos = Math.max(1, caretPos);
                setSelectionLength();
                return true;
            } else selectionLength = prevSelectionLength;
            return false;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                //Detects if the user has clicked the color preview
                if(isMouseOverColorPreview(mouseX, mouseY)) {
                        playDownSound();
                        createOrRemoveColorPicker();
                        if(YACLConfig.HANDLER.instance().showColorPickerIndicator) {
                            YACLConfig.HANDLER.instance().showColorPickerIndicator = false;
                            YACLConfig.HANDLER.save();
                        }
                }
                caretPos = Math.max(1, caretPos);
                setSelectionLength();
                return true;
            }

            return false;
        }

        public boolean isMouseOverColorPreview(double mouseX, double mouseY) {
            return colorPreviewDim.isPointInside((int) mouseX, (int) mouseY);
        }

        public void createOrRemoveColorPicker() {
            colorPickerVisible = !colorPickerVisible;
            if(colorPickerVisible) {
                colorPickerWidget = createColorPicker();
                screen.addPopupControllerWidget(colorPickerWidget);
            } else {
                removeColorPicker();
            }
        }

        @Override
        public void unfocus() {
            if(colorPickerVisible) {
                removeColorPicker();
            }
            previewOutlineFadeTicks = 0;
            super.unfocus();
        }

        public Color getPreviewOutlineColor(boolean colorPreviewHovered) {
            Color outlineColor = new Color(0xFF000000);
            Color highlightedColor = getHighlightedOutlineColor();

            if(!hovered && !colorPreviewHovered) {
                previewOutlineFadeTicks = 0;
                return outlineColor;
            }

            int fadeInTicks = 80;
            int fadeOutTicks = fadeInTicks + 120;

            if(colorPreviewHovered) {
                //white/light grey if the color preview is being hovered
                previewOutlineFadeTicks = 0;
                return highlightedColor;
            } else if(YACLConfig.HANDLER.instance().showColorPickerIndicator) {
                if(previewOutlineFadeTicks <= fadeInTicks) {
                    //fade to white
                    return getFadedColor(outlineColor, highlightedColor, previewOutlineFadeTicks, fadeInTicks);
                } else if (previewOutlineFadeTicks <= fadeOutTicks) {
                    //fade to black
                    return getFadedColor(highlightedColor, outlineColor, previewOutlineFadeTicks - fadeInTicks, fadeOutTicks - fadeInTicks);
                }

                if(previewOutlineFadeTicks >= fadeInTicks + fadeOutTicks + 10) {
                    //reset fade
                    previewOutlineFadeTicks = 0;
                }
            }

            return outlineColor;
        }

        private Color getFadedColor(Color original, Color fadeToColor, int fadeTick, int maxFadeTicks) {
            int red = fadeToColor.getRed() - original.getRed();
            int green = fadeToColor.getGreen() - original.getGreen();
            int blue = fadeToColor.getBlue() - original.getBlue();
            return new Color(
                    original.getRed() + ((red * fadeTick) / maxFadeTicks),
                    original.getGreen() + ((green * fadeTick) / maxFadeTicks),
                    original.getBlue() + ((blue * fadeTick) / maxFadeTicks)
            );
        }

        private Color getHighlightedOutlineColor() {
            //Brightness detector in case a developer has their starting color bright
            //Makes the outline indicating to a user that the mini color preview can be clicked a light grey rather than white
            //For reference, there is about a 10 digit moving room in saturation and light
            Color pendingValue = colorController.option().pendingValue();
            float[] HSL = Color.RGBtoHSB(pendingValue.getRed(), pendingValue.getGreen(), pendingValue.getBlue(), null);
            Color highlightedColor = new Color(0xFFFFFFFF);
            if(HSL[1] < 0.1f && HSL[2] > 0.9f) {
                highlightedColor = new Color(0xFFC6C6C6);
            }
            return highlightedColor;
        }

        public ColorPickerWidget colorPickerWidget() {
            return colorPickerWidget;
        }

        public boolean colorPickerVisible() {
            return colorPickerVisible;
        }

        public ColorPickerWidget createColorPicker() {
            return new ColorPickerWidget(colorController, screen, getDimension(), this);
        }

        public void removeColorPicker() {
            screen.clearPopupControllerWidget();
            this.colorPickerVisible = false;
            this.colorPickerWidget = null;
            this.hoveredOverColorPreview = false; //set to false in favor of the manual checking here to be done
        }
    }
}
