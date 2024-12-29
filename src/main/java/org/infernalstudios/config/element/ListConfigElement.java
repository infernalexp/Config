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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.infernalstudios.config.Config;
import org.infernalstudios.config.annotation.AutoSerializable;
import org.infernalstudios.config.annotation.ListValue;
import org.infernalstudios.config.element.handler.AutoElementHandler;
import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.util.Util;
import org.infernalstudios.config.util.annotation.Nullable;

@SuppressWarnings("rawtypes")
public class ListConfigElement extends ConfigElement<List> { 
    @Nullable
    public final Function<Object, Object> deserializeHandler;
    @Nullable
    public final Function<Object, Object> serializeHandler;

    @SuppressWarnings("unchecked")
    public ListConfigElement(Field field, IConfigElementHandler<List, ?> handler) {
        super(field, handler);
        ListValue valueAnnotation = field.getAnnotation(ListValue.class);

        if (valueAnnotation != null) {
            String deserializeHandlerPath = valueAnnotation.deserialize();
            String serializeHandlerPath = valueAnnotation.serialize();

            String deserializeClassPath = deserializeHandlerPath.substring(0, deserializeHandlerPath.lastIndexOf("::"));
            String deserializeMethodName = deserializeHandlerPath.substring(deserializeHandlerPath.lastIndexOf("::") + 2);

            String serializeClassPath = serializeHandlerPath.substring(0, serializeHandlerPath.lastIndexOf("::"));
            String serializeMethodName = serializeHandlerPath.substring(serializeHandlerPath.lastIndexOf("::") + 2);

            Class<?> deserializeClass;
            try {
                deserializeClass = Class.forName(deserializeClassPath);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Could not find class for deserialization handler", e);
            }
            Class<?> serializeClass;
            try {
                serializeClass = Class.forName(serializeClassPath);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Could not find class for serialization handler", e);
            }

            try {
                Method deserializeMethod = deserializeClass.getDeclaredMethod(deserializeMethodName, Object.class);
                this.deserializeHandler = obj -> {
                    try {
                        return deserializeMethod.invoke(null, obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new IllegalStateException("Could not deserialize element in list", e);
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Could not find method for deserialization handler", e);
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not access method for deserialization handler", e);
            }
            try {
                Class<?> genericType = Util.getGenericClass(field);
                Method serializeMethod = serializeClass.getDeclaredMethod(serializeMethodName, genericType);
                this.serializeHandler = obj -> {
                    try {
                        return serializeMethod.invoke(null, obj);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        throw new IllegalStateException("Could not serialize element in list", e);
                    }
                };
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Could not find method for serialization handler", e);
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not access method for serialization handler", e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Could not find class for generic type", e);
            }
        } else {
            try {
                Class<?> genericType = Util.getGenericClass(field);
                if (genericType == null) {
                    throw new IllegalStateException("List declared without generic type: " + field.toGenericString());
                }

                if (genericType.isAnnotationPresent(AutoSerializable.class)) {
                    AutoElementHandler typeHandler = (AutoElementHandler) Config.getHandler(genericType);
                    if (typeHandler == null) {
                        throw new IllegalStateException("No handler found for generic type: " + genericType.getName());
                    }

                    this.deserializeHandler = obj -> {
                        if (obj instanceof com.electronwill.nightconfig.core.Config config) {
                            return typeHandler.deserialize(new VirtualListConfigElement<>(this, obj, typeHandler.createDefault()), config);
                        } else {
                            return null;
                        }
                    };
                    this.serializeHandler = obj -> typeHandler.serialize(new VirtualListConfigElement<>(this, typeHandler.createDefault(), typeHandler.createDefault()));
                } else {
                    this.deserializeHandler = null;
                    this.serializeHandler = null;
                }
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not access method for serialization handler", e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Could not find class for generic type", e);
            }
        }
    }

    public static class VirtualListConfigElement<T> implements IConfigElement<T> {
        private final ListConfigElement parentElement;
        private final T value;
        private final T defaultValue;

        public VirtualListConfigElement(ListConfigElement parentElement, T value, T defaultValue) {
            this.parentElement = parentElement;
            this.value = value;
            this.defaultValue = defaultValue;
        }

        @Override
        public String getCategory() {
            return this.parentElement.getCategory();
        }

        @Override
        public String getTranslationKey() {
            return this.parentElement.getTranslationKey();
        }

        @Override
        public T get() {
            return this.value;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T getFromField() {
            return this.value;
        }

        @Override
        public void set(@Nullable T value) {
            throw new UnsupportedOperationException("Cannot set value of virtual list element");
        }

        @Override
        @Nullable
        public T getDefault() {
            return this.defaultValue;
        }

        @Override
        public String getComment() {
            return this.parentElement.getComment();
        }

        @Override
        public boolean hasTag(String tag) {
            return this.parentElement.hasTag(tag);
        }

        @Override
        public Field getField() {
            return null;
        }

        @Override
        public IConfigElementHandler<T, ?> getTypeHandler() {
            return null;
        }
    }
}
