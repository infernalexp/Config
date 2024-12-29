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
import java.util.List;
import java.util.Objects;

import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.ListConfigElement;
import org.infernalstudios.config.util.annotation.Nullable;

@SuppressWarnings("rawtypes")
public final class ListElementHandler implements IConfigElementHandler<List, List> {
    public static final ListElementHandler INSTANCE = new ListElementHandler();
    private ListElementHandler() {}

    @Override
    public IConfigElement<List> create(Field field) {
        return new ListConfigElement(field, this);
    }

    @Override
    public IConfigElement<List> update(IConfigElement<List> element, @Nullable List obj) {
        if (obj != null) {
            element.set(obj);
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List serialize(IConfigElement<List> element) {
        List value = element.getFromField();
        if (value == null) {
            value = element.getDefault();
        }

        if (element instanceof ListConfigElement listConfigElement && listConfigElement.serializeHandler != null) {
            value = value.stream().map(listConfigElement.serializeHandler).filter(Objects::nonNull).toList();
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List deserialize(IConfigElement<List> element, List obj) {
        if (obj == null) {
            obj = element.getDefault();
        }

        if (element instanceof ListConfigElement listConfigElement && listConfigElement.deserializeHandler != null) {
            obj = obj.stream().map(listConfigElement.deserializeHandler).filter(Objects::nonNull).toList();
        }

        return obj;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return (List.class.equals(clazz) || List.class.isAssignableFrom(clazz));
    }
}
