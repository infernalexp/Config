package org.infernalstudios.config.element.handler;

import java.lang.reflect.Field;

import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.NumberConfigElement;
import org.infernalstudios.config.util.Util;
import org.infernalstudios.config.util.annotation.Nullable;

public class NumberElementHandler implements IConfigElementHandler<Number, Number> {
    public static final NumberElementHandler INSTANCE = new NumberElementHandler();

    private NumberElementHandler() {}

    @Override
    public IConfigElement<Number> create(Field field) {
        return new NumberConfigElement<>(field, Double.MIN_VALUE, Double.MAX_VALUE, this);
    }

    @Override
    public IConfigElement<Number> update(IConfigElement<Number> element, @Nullable Number obj) {
        if (obj != null) {
            element.set(obj.doubleValue());
        }
        return element;
    }

    @Override
    public Number serialize(IConfigElement<Number> element) {
        Number value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public Number deserialize(Number obj) {
        return obj;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return Util.isNumber(clazz);
    }
}
