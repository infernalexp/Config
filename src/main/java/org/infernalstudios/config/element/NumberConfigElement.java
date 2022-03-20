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

import com.electronwill.nightconfig.core.conversion.InvalidValueException;

import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.util.annotation.Nullable;

public class NumberConfigElement<T extends Number> extends PrimitiveConfigElement<T> {
    private final T min;
    private final T max; 

    public NumberConfigElement(Field field, T min, T max, IConfigElementHandler<T, ?> handler) {
        super(field, handler);
        this.min = min;
        this.max = max;
        
        if (!this.isValid(this.getDefault())) {
            throw new IllegalStateException(String.format("Default value \"%s\" is not in range [%s, %s]\n\tat: %s",
                    this.getDefault(), this.min, this.max, this.getField().toGenericString()));
        }
    }

    @Override
    public String getComment() {
        StringBuilder s = new StringBuilder();
        String comment = super.getComment();
        if (comment != null) {
            s.append(comment);
        }
        if (min != null && min.doubleValue() != Double.MIN_VALUE && min.intValue() != Integer.MIN_VALUE && min.floatValue() != Float.MIN_VALUE) {
            if (s.length() > 0) {
                s.append('\n');
            }
            s.append(' ').append("Minimum: ").append(min);
        }
        if (max != null && max.doubleValue() != Double.MAX_VALUE && max.intValue() != Integer.MAX_VALUE && max.floatValue() != Float.MAX_VALUE) {
            if (s.length() > 0) {
                s.append('\n');
            }
            s.append(' ').append("Maximum: ").append(max);
        }
        return s.toString();
    }

    @Override
    public void set(@Nullable T value) {
        if (value != null) {
            if (this.isValid(value)) {
                super.set(value);
            } else {
                throw new InvalidValueException(String.format("Value \"%s\" is not valid for field \"%s.%s\"",
                        value, this.getField().getDeclaringClass().getName(), this.getField().getName()));
            }
        }
    }

    public boolean isValid(Object value) {
        if (value == null) {
            return true;
        } else {
            return value instanceof Number && this.isValid((Number) value);
        }
    }

    public boolean isValid(Number value) {
        return value.doubleValue() >= this.min.doubleValue() && value.doubleValue() <= this.max.doubleValue();
    }
}
