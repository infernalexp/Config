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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileWatcher;

import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.element.handler.BooleanElementHandler;
import org.infernalstudios.config.element.handler.DoubleElementHandler;
import org.infernalstudios.config.element.handler.FloatElementHandler;
import org.infernalstudios.config.element.handler.IConfigElementHandler;
import org.infernalstudios.config.element.handler.IntegerElementHandler;
import org.infernalstudios.config.element.handler.ListElementHandler;
import org.infernalstudios.config.element.handler.NumberElementHandler;
import org.infernalstudios.config.element.handler.StringElementHandler;

public final class Config {
    private final CommentedFileConfig config;
    public final List<IConfigElement<?>> elements;

    protected Config(CommentedFileConfig config, List<IConfigElement<?>> elements) {
        this.config = config;
        this.elements = new CopyOnWriteArrayList<>(elements);

        this.reload();

        synchronized (this.config) {
            this.save();
        }

        try {
            FileWatcher.defaultInstance().addWatch(config.getNioPath(), new Thread() {
                @Override
                public void run() {
                    Config.this.reload();
                }
            });
        } catch (IOException e) {
            System.err.println(String.format("Couldn't watch file \"%s\" for changes.",
                    config.getNioPath().toAbsolutePath().toString()));
        }
    }

    /**
     * Saves all config values to the config file.
     */
    @SuppressWarnings("unchecked")
    public void save() {
        for (IConfigElement<?> element : this.elements) {
            this.config.set(element.getName(), ((IConfigElement<Object>) element).getTypeHandler().serialize((IConfigElement<Object>) element));
            this.config.setComment(element.getName(), element.getComment());
        }
        this.config.save();
    }

    /**
     * Reloads the config elements, sets their values from the config file.
     */
    @SuppressWarnings("unchecked")
    public void reload() {
        this.config.load();
        boolean shouldSave = false;
        for (IConfigElement<?> element : this.elements) {
            Object obj = this.config.get(element.getName());
            IConfigElementHandler<Object, Object> handler = (IConfigElementHandler<Object, Object>) element.getTypeHandler();
            if (obj != null && handler.canHandle(obj.getClass())) {
                handler.update(((IConfigElement<Object>) element), obj);
            } else {
                this.config.set(element.getName(), handler.serialize((IConfigElement<Object>) element));
                shouldSave = true;
            }
        }
        if (shouldSave) {
            this.save();
        }
        this.config.save();
    }

    /**
     * Creates a config builder.
     * @throws IOException
     */
    public static ConfigBuilder builder(String path) throws IOException {
        return Config.builder(Paths.get(path));
    }

    /**
     * Creates a config builder.
     * @throws IOException
     */
    public static ConfigBuilder builder(File file) throws IOException {
        return Config.builder(file.toPath());
    }

    /**
     * Creates a config builder.
     * @throws IOException
     */
    public static ConfigBuilder builder(Path path) throws IOException {
        return new ConfigBuilder(path);
    }

    protected static final Map<Class<?>, IConfigElementHandler<?, ?>> HANDLERS = new ConcurrentHashMap<>();

    public static <T> void registerHandler(Class<T> clazz, IConfigElementHandler<T, ?> handler) {
        Config.HANDLERS.put(clazz, handler);
    }

    @SuppressWarnings("unchecked")
    public static <T> IConfigElementHandler<T, ?> getHandler(Class<T> clazz) {
        IConfigElementHandler<?, ?> handler = Config.HANDLERS.get(clazz);
        if (handler == null) {
            for (IConfigElementHandler<?, ?> h : Config.HANDLERS.values()) {
                if (h.canHandle(clazz)) {
                    handler = h;
                    break;
                }
            }
        }
        return (IConfigElementHandler<T, ?>) handler;
    }

    static {
        registerHandler(Boolean.class, BooleanElementHandler.INSTANCE);
        registerHandler(Boolean.TYPE, BooleanElementHandler.INSTANCE);
        registerHandler(Double.class, DoubleElementHandler.INSTANCE);
        registerHandler(Double.TYPE, DoubleElementHandler.INSTANCE);
        registerHandler(Float.class, FloatElementHandler.INSTANCE);
        registerHandler(Float.TYPE, FloatElementHandler.INSTANCE);
        registerHandler(Integer.class, IntegerElementHandler.INSTANCE);
        registerHandler(Integer.TYPE, IntegerElementHandler.INSTANCE);
        registerHandler(String.class, StringElementHandler.INSTANCE);
        registerHandler(Number.class, NumberElementHandler.INSTANCE);
        registerHandler(List.class, ListElementHandler.INSTANCE);
    }
}
