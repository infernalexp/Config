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
import org.infernalstudios.config.util.annotation.Nullable;

/**
 * Represents an element of the config.
 * Contains the field, the handler, modifiers attributed to it, and everything else related to the element.
 */
public interface IConfigElement<T> {
    /**
     * Returns the name of this element. Defaults to the field name + any parent category, and shouldn't be overriden.
     */
    default String getName() {
        if (getCategory().isEmpty()) {
            return getField().getName();
        } else {
            return getCategory() + "." + getField().getName();
        }
    }

    /**
     * Returns the category this element is in.
     */
    String getCategory();

    /**
     * Returns the translation key of this element.
     */
    String getTranslationKey();
    
    /**
     * Returns the cached value of this element. If the cached value is not set, will return null, and users should always check for null.
     */
    @Nullable
    T get();

    /**
     * Returns the field value of this element. This may be null.
     */
    @Nullable
    T getFromField();

    /**
     * Sets the value of this element. If the given value is null, the value will be cleared.
     * Can throw an {@link IllegalArgumentException} if the given value is not valid.
     */
    void set(@Nullable T value);
    
    /**
     * Returns the default value of this element. Should never be null, and should be set within the class constructor.
     */
    T getDefault();
    
    /**
     * Returns comments regarding this element. Can be null.
     */
    @Nullable
    String getComment();

    /**
     * Checks if the given tag is present on the element.
     */
    boolean hasTag(String tag);

    /**
     * Returns the field this element is associated with.
     */
    Field getField();

    /**
     * Returns the type handler for this element.
     */
    IConfigElementHandler<T, ?> getTypeHandler();

    /**
     * Returns the type of this element, defaults to the field's type.
     */
    default Class<?> getType() {
        return getField().getType();
    }

    /**
     * Tests if the config element's type extends the given class
     */
    default boolean extendsFrom(Class<?> clazz) {
        return getType().isAssignableFrom(clazz);
    }
}
