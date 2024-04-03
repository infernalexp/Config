/*
 * Copyright 2022 Infernal Studios
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infernalstudios.config.element.handler;

import java.lang.reflect.Field;

import org.infernalstudios.config.annotation.DoubleRange;
import org.infernalstudios.config.annotation.FloatRange;
import org.infernalstudios.config.annotation.IntegerRange;
import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.NumberConfigElement;
import org.infernalstudios.config.util.annotation.Nullable;

public final class DoubleElementHandler implements IConfigElementHandler<Double, Number> {
    public static final DoubleElementHandler INSTANCE = new DoubleElementHandler();
    private DoubleElementHandler() {}

    @Override
    public IConfigElement<Double> create(Field field) {
        DoubleRange rangeD = field.getAnnotation(DoubleRange.class);
        FloatRange rangeF = field.getAnnotation(FloatRange.class);
        IntegerRange rangeI = field.getAnnotation(IntegerRange.class);
        if (rangeF != null) {
            System.err.println(String.format("WARNING: %s has a %s annotation, but is a double.",
                    field.toGenericString(), FloatRange.class.getSimpleName()));
        }
        if (rangeI != null) {
            System.err.println(String.format("WARNING: %s has an %s annotation, but is a double.",
                    field.toGenericString(), IntegerRange.class.getSimpleName()));
        }
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        if (rangeD != null) {
            min = rangeD.min();
            max = rangeD.max();
        }
        return new NumberConfigElement<>(field, min, max, this);
    }

    @Override
    public IConfigElement<Double> update(IConfigElement<Double> element, @Nullable Double value) {
        if (value != null) {
            element.set(value.doubleValue());
        }
        return element;
    }

    @Override
    public Number serialize(IConfigElement<Double> element) {
        Double value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public Double deserialize(IConfigElement<Double> element, Number obj) {
        return obj.doubleValue();
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return Double.class.equals(clazz) || Double.TYPE.equals(clazz) || Double.class.isAssignableFrom(clazz) || Double.TYPE.isAssignableFrom(clazz);
    }
}
