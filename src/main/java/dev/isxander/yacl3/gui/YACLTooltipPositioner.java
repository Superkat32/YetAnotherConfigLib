package dev.isxander.yacl3.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.util.Mth;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.function.Supplier;

public class YACLTooltipPositioner implements ClientTooltipPositioner {
    private final Supplier<ScreenRectangle> buttonDimensions;

    public YACLTooltipPositioner(net.minecraft.client.gui.components.AbstractWidget widget) {
        this.buttonDimensions = widget::getRectangle;
    }

    public YACLTooltipPositioner(dev.isxander.yacl3.gui.AbstractWidget widget) {
        this.buttonDimensions = () -> {
            var dim = widget.getDimension();
            return new ScreenRectangle(dim.x(), dim.y(), dim.width(), dim.height());
        };
    }

    public YACLTooltipPositioner(Supplier<ScreenRectangle> buttonDimensions) {
        this.buttonDimensions = buttonDimensions;
    }

    @Override
    public Vector2ic positionTooltip(int guiWidth, int guiHeight, int x, int y, int width, int height) {
        ScreenRectangle buttonDimensions = this.buttonDimensions.get();

        int centerX = buttonDimensions.left() + buttonDimensions.width() / 2;
        int aboveY = buttonDimensions.top() - height - 4;
        int belowY = buttonDimensions.top() + buttonDimensions.height() + 4;

        int maxBelow = guiHeight - (belowY + height);
        int minAbove = aboveY - height;

        int yResult = aboveY;
        if (minAbove < 8)
            yResult = maxBelow > minAbove ? belowY : aboveY;

        int xResult = Mth.clamp(centerX - width / 2, -4, guiWidth - width - 4);

        return new Vector2i(xResult, yResult);
    }
}
