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
package org.infernalstudios.config.element;

import java.lang.reflect.Field;

import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.util.Util;

public class PrimitiveConfigElement<T> extends ConfigElement<T> {
    private final Class<?> primitiveType;

    public PrimitiveConfigElement(Field field, IConfigElementHandler<T, ?> handler) {
        super(field, handler);
        this.primitiveType = Util.getPrimitive(this.getType());
        if (this.primitiveType == null) {
            throw new IllegalArgumentException(String.format("defaultValue must be a primitive type, got %s", this.getType()));
        }
    }

    @Override
    public boolean extendsFrom(Class<?> clazz) {
        return this.primitiveType.isAssignableFrom(clazz) || super.extendsFrom(clazz);
    }
}
