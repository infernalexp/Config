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

public final class FloatElementHandler implements IConfigElementHandler<Float, Number> {
    public static final FloatElementHandler INSTANCE = new FloatElementHandler();
    private FloatElementHandler() {}

    @Override
    public IConfigElement<Float> create(Field field) {
        DoubleRange rangeD = field.getAnnotation(DoubleRange.class);
        FloatRange rangeF = field.getAnnotation(FloatRange.class);
        IntegerRange rangeI = field.getAnnotation(IntegerRange.class);
        if (rangeD != null) {
            System.err.println(String.format("WARNING: %s has a %s annotation, but is a float.", 
                    field.toGenericString(), DoubleRange.class.getSimpleName()));
        }
        if (rangeI != null) {
            System.err.println(String.format("WARNING: %s has an %s annotation, but is a float.", 
                    field.toGenericString(), IntegerRange.class.getSimpleName()));
        }
        float min = Float.MIN_VALUE;
        float max = Float.MAX_VALUE;
        if (rangeF != null) {
            min = rangeF.min();
            max = rangeF.max();
        }
        return new NumberConfigElement<>(field, min, max, this);
    }

    @Override
    public IConfigElement<Float> update(IConfigElement<Float> element, @Nullable Float value) {
        if (value != null) {
            element.set(value.floatValue());
        }
        return element;
    }

    @Override
    public Number serialize(IConfigElement<Float> element) {
        Float value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public Float deserialize(IConfigElement<Float> element, Number obj) {
        return obj.floatValue();
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return Float.class.equals(clazz) || Float.TYPE.equals(clazz) || Float.class.isAssignableFrom(clazz) || Float.TYPE.isAssignableFrom(clazz);
    }
}
