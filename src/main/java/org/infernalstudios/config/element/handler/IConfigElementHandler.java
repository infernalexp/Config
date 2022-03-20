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

import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.util.annotation.Nullable;

public interface IConfigElementHandler<T, S> {
    /**
     * Creates a new config element using `field`.
     * @param field The field to create the element for.
     */
    IConfigElement<T> create(Field field);

    /**
     * Updates `element` with a new TOML-serialized value `obj`.
     * @param element The element to update.
     * @param obj The TOML-serialized value to use as the new value. Can be null.
     */
    IConfigElement<T> update(IConfigElement<T> element, @Nullable S obj);

    /**
     * Serializes `element` to a TOML-serializable value.
     * @param element The element to serialize.
     */
    S serialize(IConfigElement<T> element);

    /**
     * Determines whether the provided type can be handled by this handler.
     * @param clazz The type to check.
     */
    boolean canHandle(Class<?> clazz);
}
