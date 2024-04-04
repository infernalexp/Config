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
package org.infernalstudios.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the field is a {@link java.util.List} field.
 * Must specify `deserialize` and `serialize` handlers.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ListValue {
    /**
     * The handler for deserializing the list.
     * Handler must have a return type of the element in list, must accept an {@link Object} type, and must be static.
     */
    String deserialize();
    
    /**
     * The handler for serializing the list.
     * Handler must have a return type of any TOML serializable value, must accept the element in list as a parameter, and must be static.
     */
    String serialize();
}
