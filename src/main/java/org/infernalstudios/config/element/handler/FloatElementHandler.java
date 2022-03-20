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

import org.infernalstudios.config.annotation.Configurable;
import org.infernalstudios.config.annotation.FloatRange;
import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.NumberConfigElement;
import org.infernalstudios.config.util.Util;
import org.infernalstudios.config.util.annotation.Nullable;

public final class FloatElementHandler implements IConfigElementHandler<Float, Number> {
    public static final FloatElementHandler INSTANCE = new FloatElementHandler();
    private FloatElementHandler() {}

    @Override
    public IConfigElement<Float> create(Field field) {
        FloatRange range = field.getAnnotation(FloatRange.class);
        float min = Float.MIN_VALUE;
        float max = Float.MAX_VALUE;
        if (range != null) {
            min = range.min();
            max = range.max();
        }
        IConfigElement<Float> element = new NumberConfigElement<Float>(field, min, max, this);
        Configurable configurable = field.getAnnotation(Configurable.class);
        String description = configurable.description();
        String translationKey = configurable.translationKey();
        if (!description.isEmpty()) {
            element.setComment(description);
        }
        element.setTranslationKey(translationKey.isEmpty() ? field.getName() : translationKey);
        element.setCategory(Util.getCategory(field));
        return element;
    }

    @Override
    public IConfigElement<Float> update(IConfigElement<Float> element, @Nullable Number obj) {
        if (obj != null) {
            element.set(obj.floatValue());
        }
        return element;
    }

    @Override
    public Number serialize(IConfigElement<Float> element) {
        Float value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz.equals(Float.class) || clazz.equals(Float.TYPE) || clazz.isAssignableFrom(Float.class) || clazz.isAssignableFrom(Float.TYPE);
    }
}
