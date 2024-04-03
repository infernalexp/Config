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
import java.lang.reflect.Method;
import java.util.List;

import org.infernalstudios.config.annotation.ListValue;
import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.util.annotation.Nullable;

import com.electronwill.nightconfig.core.Config;

@SuppressWarnings("rawtypes")
public class ListConfigElement extends ConfigElement<List> { 
    @Nullable
    public final Method deserializeHandler;
    @Nullable
    public final Method serializeHandler;

    public ListConfigElement(Field field, IConfigElementHandler<List, ?> handler) {
        super(field, handler);
        ListValue valueAnnotation = field.getAnnotation(ListValue.class);

        if (valueAnnotation != null) {
            String deserializeHandlerPath = valueAnnotation.deserialize();
            String serializeHandlerPath = valueAnnotation.serialize();

            String deserializeClassPath = deserializeHandlerPath.substring(0, deserializeHandlerPath.lastIndexOf("::"));
            String deserializeMethod = deserializeHandlerPath.substring(deserializeHandlerPath.lastIndexOf("::") + 2);

            String serializeClassPath = serializeHandlerPath.substring(0, serializeHandlerPath.lastIndexOf("::"));
            String serializeMethod = serializeHandlerPath.substring(serializeHandlerPath.lastIndexOf("::") + 2);

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
                this.deserializeHandler = deserializeClass.getDeclaredMethod(deserializeMethod, Config.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Could not find method for deserialization handler", e);
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not access method for deserialization handler", e);
            }
            try {
                this.serializeHandler = serializeClass.getDeclaredMethod(serializeMethod);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Could not find method for serialization handler", e);
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not access method for serialization handler", e);
            }
        } else {
            this.deserializeHandler = null;
            this.serializeHandler = null;
        }
    }
}
