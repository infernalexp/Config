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
import java.util.Objects;

import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.util.annotation.Nullable;

public class ConfigElement<T> implements IConfigElement<T> {
    private final Field field;
    private final IConfigElementHandler<T, ?> handler;
    private final T defaultValue;
    private String translationKey;
    private String comment;
    private T value;
    private String category = "";

    @SuppressWarnings("unchecked")
    public ConfigElement(Field field, IConfigElementHandler<T, ?> handler) {
        this.field = field;
        this.handler = handler;
        this.translationKey = null;
        this.value = null;
        this.comment = null;

        try {
            this.defaultValue = (T) this.field.get(field.getDeclaringClass());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Field is not static\n\tat: %s",
                    field.toGenericString()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format("Field is not public\n\tat: %s",
                    field.toGenericString()), e);
        } catch (ClassCastException e) {
            throw new IllegalStateException(String.format("Field is not of type \"%s\"\n\tat: %s",
                    field.getType().getName(), field.toGenericString()), e);
        }
        if (this.defaultValue == null) {
            throw new IllegalStateException(String.format("Default value is null for field \"%s\"\n\tat: %s",
                    this.field.getName(), this.field.toGenericString()));
        }
    }


    @Override
    @Nullable
    public String getTranslationKey() {
        return this.translationKey;
    }

    @Override
    public void setTranslationKey(@Nullable String translationKey) {
        Objects.requireNonNull(translationKey, "translationKey mustn't be null");
        this.translationKey = translationKey;
    }

    @Override
    @Nullable
    public T get() {
        return this.value;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public T getFromField() {
        try {
            this.set((T) this.field.get(field.getDeclaringClass()));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Field is not static\n\tat: %s",
                    field.toGenericString()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format("Field is not public\n\tat: %s",
                    field.toGenericString()), e);
        } catch (ClassCastException e) {
            throw new IllegalStateException(String.format("Field is not of type \"%s\"\n\tat: %s",
                    field.getType().getName(), field.toGenericString()), e);
        }
        return this.get();
    }

    @Override
    public void set(@Nullable T value) {
        this.value = value;
        try {
            this.field.set(this, value == null ? this.getDefault() : this.get());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Field is not static\n\tat: %s",
                    this.field.toGenericString()), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(String.format("Field is not public\n\tat: %s",
                    this.field.toGenericString()), e);
        }
    }

    @Override
    public T getDefault() {
        return this.defaultValue;
    }

    @Override
    public String getComment() {
        StringBuilder s = new StringBuilder();
        if (this.comment != null) {
            s.append(' ').append(this.comment).append('\n');
        }
        s.append(' ').append("Default: ").append(this.defaultValue.toString());
        return s.toString();
    }

    @Override
    public void setComment(@Nullable String comment) {
        this.comment = comment;
    }

    @Override
    public Field getField() {
        return this.field;
    }

    @Override
    public IConfigElementHandler<T, ?> getTypeHandler() {
        return this.handler;
    }


    @Override
    public String getCategory() {
        return this.category;
    }


    @Override
    public void setCategory(String category) {
        this.category = category;
    }
}
