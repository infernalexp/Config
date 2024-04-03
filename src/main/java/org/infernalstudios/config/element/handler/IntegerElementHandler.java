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

public final class IntegerElementHandler implements IConfigElementHandler<Integer, Number> {
    public static final IntegerElementHandler INSTANCE = new IntegerElementHandler();
    private IntegerElementHandler() {}

    @Override
    public IConfigElement<Integer> create(Field field) {
        DoubleRange rangeD = field.getAnnotation(DoubleRange.class);
        FloatRange rangeF = field.getAnnotation(FloatRange.class);
        IntegerRange rangeI = field.getAnnotation(IntegerRange.class);
        if (rangeD != null) {
            System.err.println(String.format("WARNING: %s has a %s annotation, but is an int.",
                    field.toGenericString(), DoubleRange.class.getSimpleName()));
        }
        if (rangeF != null) {
            System.err.println(String.format("WARNING: %s has an %s annotation, but is an int.",
                    field.toGenericString(), FloatRange.class.getSimpleName()));
        }
        int min = Integer.MIN_VALUE;
        int max = Integer.MAX_VALUE;
        if (rangeI != null) {
            min = rangeI.min();
            max = rangeI.max();
        }
        return new NumberConfigElement<>(field, min, max, this);
    }

    @Override
    public IConfigElement<Integer> update(IConfigElement<Integer> element, @Nullable Integer value) {
        if (value != null) {
            element.set(value.intValue());
        }
        return element;
    }

    @Override
    public Number serialize(IConfigElement<Integer> element) {
        Integer value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public Integer deserialize(IConfigElement<Integer> element, Number obj) {
        return obj.intValue();
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return Integer.class.equals(clazz) || Integer.TYPE.equals(clazz) || Integer.class.isAssignableFrom(clazz) || Integer.TYPE.isAssignableFrom(clazz);
    }
}
