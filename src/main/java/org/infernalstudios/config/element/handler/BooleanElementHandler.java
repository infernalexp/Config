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

// import org.infernalstudios.config.annotation.Configurable;
import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.PrimitiveConfigElement;
import org.infernalstudios.config.util.annotation.Nullable;

public final class BooleanElementHandler implements IConfigElementHandler<Boolean, Boolean> {
    public static final BooleanElementHandler INSTANCE = new BooleanElementHandler();
    private BooleanElementHandler() {}

    @Override
    public IConfigElement<Boolean> create(Field field) {
        return new PrimitiveConfigElement<>(field, this);
    }

    @Override
    public IConfigElement<Boolean> update(IConfigElement<Boolean> element, @Nullable Boolean bool) {
        if (bool != null) {
            element.set(bool.booleanValue());
        }
        return element;
    }

    @Override
    public Boolean serialize(IConfigElement<Boolean> element) {
        Boolean value = element.getFromField();
        return value == null ? element.getDefault() : value;
    }

    @Override
    public Boolean deserialize(Boolean obj) {
        return obj;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return clazz.equals(Boolean.class) || clazz.equals(Boolean.TYPE) || clazz.isAssignableFrom(Boolean.class) || clazz.isAssignableFrom(Boolean.TYPE);
    }
}
