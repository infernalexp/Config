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
import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.NumberConfigElement;
import org.infernalstudios.config.util.annotation.Nullable;

public final class DoubleElementHandler implements IConfigElementHandler<Double, Number> {
    public static final DoubleElementHandler INSTANCE = new DoubleElementHandler();
    private DoubleElementHandler() {}

    @Override
    public IConfigElement<Double> create(Field field) {
        DoubleRange range = field.getAnnotation(DoubleRange.class);
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;
        if (range != null) {
            min = range.min();
            max = range.max();
        }
        return new NumberConfigElement<>(field, min, max, this);
    }

    @Override
    public IConfigElement<Double> update(IConfigElement<Double> element, @Nullable Number obj) {
        if (obj != null) {
            element.set(obj.doubleValue());
        }
        return element;
    }

    @Override
    public Number serialize(IConfigElement<Double> element) {
        Double value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz.equals(Double.class) || clazz.equals(Double.TYPE) || clazz.isAssignableFrom(Double.class) || clazz.isAssignableFrom(Double.TYPE);
    }
}
