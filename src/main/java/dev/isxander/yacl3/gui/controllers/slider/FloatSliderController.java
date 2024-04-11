package dev.isxander.yacl3.gui.controllers.slider;

import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.controller.ValueFormatter;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.Validate;

import java.util.function.Function;

/**
 * {@link ISliderController} for floats.
 */
public class FloatSliderController implements ISliderController<Float> {
    /**
     * Formats floats to one decimal place
     */
    public static final Function<Float, Component> DEFAULT_FORMATTER = value -> Component.literal(String.format("%,.1f", value).replaceAll("[\u00a0\u202F]", " "));

    private final Option<Float> option;

    private final float min, max, interval;

    private final ValueFormatter<Float> valueFormatter;

    /**
     * Constructs a {@link ISliderController} for floats
     * using the default value formatter {@link FloatSliderController#DEFAULT_FORMATTER}.
     *
     * @param option bound option
     * @param min minimum slider value
     * @param max maximum slider value
     * @param interval step size (or increments) for the slider
     */
    public FloatSliderController(Option<Float> option, float min, float max, float interval) {
        this(option, min, max, interval, DEFAULT_FORMATTER);
    }

    /**
     * Constructs a {@link ISliderController} for floats.
     *
     * @param option bound option
     * @param min minimum slider value
     * @param max maximum slider value
     * @param interval step size (or increments) for the slider
     * @param valueFormatter format the value into any {@link Component}
     */
    public FloatSliderController(Option<Float> option, float min, float max, float interval, Function<Float, Component> valueFormatter) {
        Validate.isTrue(max > min, "`max` cannot be smaller than `min`");
        Validate.isTrue(interval > 0, "`interval` must be more than 0");
        Validate.notNull(valueFormatter, "`valueFormatter` must not be null");

        this.option = option;
        this.min = min;
        this.max = max;
        this.interval = interval;
        this.valueFormatter = valueFormatter::apply;
    }

    public static FloatSliderController createInternal(Option<Float> option, float min, float max, float interval, ValueFormatter<Float> formatter) {
        return new FloatSliderController(option, min, max, interval, formatter::format);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Option<Float> option() {
        return option;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component formatValue() {
        return valueFormatter.format(option().pendingValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double min() {
        return min;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double max() {
        return max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double interval() {
        return interval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPendingValue(double value) {
        option().requestSet((float) value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double pendingValue() {
        return option().pendingValue();
    }

}
