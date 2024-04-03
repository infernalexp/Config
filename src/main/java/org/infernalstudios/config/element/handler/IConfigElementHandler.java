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
     * Updates {@code element} with a new value {@code obj}.
     * @param element The element to update.
     * @param obj The new value deserialized from {@link IConfigElementHandler#deserialize(S) deserialize}. Can be null.
     */
    IConfigElement<T> update(IConfigElement<T> element, @Nullable T obj);

    /**
     * Serializes {@code element} to a TOML-serializable value.
     * @param element The element to serialize.
     */
    S serialize(IConfigElement<T> element);

    /**
     * Deserializes {@code obj} from a TOML-serializable value to the handler type.
     * @param obj The value to deserialize.
     */
    @Nullable
    T deserialize(IConfigElement<T> element, S obj);

    /**
     * Determines whether the provided type can be handled by this handler.
     * @param clazz The type to check.
     */
    boolean canHandle(Class<?> clazz);
}
