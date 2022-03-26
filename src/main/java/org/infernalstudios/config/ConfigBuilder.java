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
package org.infernalstudios.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;

import org.infernalstudios.config.annotation.Configurable;
import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.util.Pair;
import org.infernalstudios.config.util.Util;

public final class ConfigBuilder {
    private final Path path;
    private final List<Pair<Field, IConfigElementHandler<?, ?>>> elements = new LinkedList<>();
    private final Set<String> loadedElementNames = new HashSet<>();

    protected ConfigBuilder(Path path) throws IOException {
        this.path = path;
        File file = path.toFile();
        if (file.exists()) {
            throwIfInvalidFile(file);
        }
    }

    /**
     * Adds a field to the config only if it has the {@link org.infernalstudios.config.annotation.Configurable Configurable} annotation.
     * 
     * @throws IllegalArgumentException If the field has the same name as another
     *                                  already added field
     * @throws IllegalStateException    If the handler for the type of the field
     *                                  cannot be found
     */
    public ConfigBuilder loadField(Field field) throws IllegalStateException, IllegalArgumentException {
        if (field.isAnnotationPresent(Configurable.class)) {
            String category = Util.getCategory(field) + "." + field.getName();
            if (this.loadedElementNames.contains(category)) {
                throw new IllegalArgumentException(String.format("Field with name \"%s\" is already loaded", category));
            }
            this.loadedElementNames.add(category);
            
            Configurable configurable = field.getAnnotation(Configurable.class);
            IConfigElementHandler<?, ?> handler = null;
            if (!configurable.handler().equals("")) {
                try {
                    String fieldPath = configurable.handler();
                    String classPath = fieldPath.substring(0, fieldPath.lastIndexOf('.'));
                    String fieldName = fieldPath.substring(fieldPath.lastIndexOf('.') + 1);
                    Field INSTANCE = Class.forName(classPath).getDeclaredField(fieldName);
                    Class<?> clazz = INSTANCE.getType();
                    Object h = INSTANCE.get(null);
                    if (IConfigElementHandler.class.isInstance(h)) {
                        handler = (IConfigElementHandler<?, ?>) h;
                    } else {
                        throw new IllegalStateException(
                                String.format("%s is not an instance of %s\n\tat: %s", clazz.toGenericString(),
                                        IConfigElementHandler.class.toGenericString(), field.toGenericString()));
                    }
                    if (!handler.canHandle(field.getType())) {
                        throw new IllegalStateException(
                                String.format("%s cannot handle %s\n\tat: %s", handler.getClass().toGenericString(),
                                        field.getType().toGenericString(), field.toGenericString()));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    // Fail silently
                }
            }
            if (handler == null) {
                Class<?> fieldType = field.getType();
                handler = Config.getHandler(fieldType);
                if (handler == null) {
                    throw new IllegalStateException(String.format("No handler of type %s\n\tat: %s",
                            fieldType.getName(), field.toGenericString()));
                }
            }
            
            this.elements.add(Pair.of(field, handler));
        }

        return this;
    }

    /**
     * Adds all public static fields from the given class with the
     * {@link org.infernalstudios.config.annotation.Configurable Configurable}
     * annotation to the config.
     * This method will not add inherited fields.
     * 
     * @param clazz The class to add
     * 
     * @throws IllegalArgumentException If a field has the same name as another
     *                                  already added field
     * @throws IllegalStateException    If the handler for the type of a field
     *                                  cannot be found
     */
    public ConfigBuilder loadClass(Class<?> clazz) throws IllegalStateException, IllegalArgumentException {
        return this.loadClass(clazz, false);
    }

    /**
     * Adds all public static fields from the given class with the
     * {@link org.infernalstudios.config.annotation.Configurable Configurable}
     * annotation to the config.
     * 
     * @param clazz         The class to add
     * @param addInherited Whether to add inherited fields
     * 
     * @throws IllegalArgumentException If a field has the same name as another
     *                                  already added field
     * @throws IllegalStateException    If the handler for the type of a field
     *                                  cannot be found
     */
    public ConfigBuilder loadClass(Class<?> clazz, boolean addInherited) throws IllegalStateException, IllegalArgumentException {
        for (Field field : addInherited ? clazz.getFields() : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && field.isAnnotationPresent(Configurable.class)) {
                this.loadField(field);
            }
        }
        return this;
    }

    /**
     * Creates an instance of the config class
     * 
     * @throws IOException
     */
    public Config build() throws IOException {
        List<IConfigElement<?>> elements = new ArrayList<>(this.elements.size());
        for (Pair<Field, IConfigElementHandler<?, ?>> pair : this.elements) {
            pair.getLeft().setAccessible(true);
            elements.add(pair.getRight().create(pair.getLeft()));
        }

        CommentedFileConfig config = CommentedFileConfig
            .builder(this.path, TomlFormat.instance())
            .concurrent()
            .build();

        File file = this.path.toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IOException(String.format("Could not create file \"%s\"", this.path), e);
            }
        }
        throwIfInvalidFile(file);
        return new Config(config, elements);
    }

    private static void throwIfInvalidFile(File file) throws IOException {
        if (!file.isFile()) {
            throw new IOException(String.format("\"%s\" is not a file!", file.getAbsolutePath().toString()));
        }
        if (!file.canRead()) {
            throw new IOException(String.format("File \"%s\" is not readable!", file.getAbsolutePath().toString()));
        }
        if (!file.canWrite()) {
            throw new IOException(String.format("File \"%s\" is not writable!", file.getAbsolutePath().toString()));
        }
        if (file.isHidden()) {
            System.out.println(String.format("WARNING: File \"%s\" is hidden", file.getAbsolutePath().toString()));
        }
    }
}