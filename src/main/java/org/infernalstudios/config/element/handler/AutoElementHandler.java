package org.infernalstudios.config.element.handler;

import com.electronwill.nightconfig.core.CommentedConfig;
import org.infernalstudios.config.Config;
import org.infernalstudios.config.annotation.Configurable;
import org.infernalstudios.config.element.ConfigElement;
import org.infernalstudios.config.element.IConfigElement;
import org.infernalstudios.config.util.annotation.Nullable;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoElementHandler<T> implements IConfigElementHandler<T, com.electronwill.nightconfig.core.Config> {
    private final Class<T> clazz;
    private final Constructor<T> constructor;
    private final List<String> fields = new ArrayList<>();

    public AutoElementHandler(Class<T> clazz) {
        this.clazz = clazz;

        try {
            this.constructor = clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No default constructor found for class: " + clazz.getName(), e);
        }

        if (!Modifier.isPublic(constructor.getModifiers())) {
            throw new IllegalStateException("Default constructor is not public");
        }

        if (Modifier.isAbstract(clazz.getModifiers())) {
            throw new IllegalStateException("Cannot generate auto-serializer for abstract class");
        }

        Field[] fields = Arrays.stream(clazz.getDeclaredFields()).filter(
            field ->
                    field.isAnnotationPresent(Configurable.class) &&
                    Modifier.isPublic(field.getModifiers()) &&
                    !Modifier.isStatic(field.getModifiers()) &&
                    !Modifier.isFinal(field.getModifiers())
        ).toArray(Field[]::new);

        if (fields.length == 0) {
            throw new IllegalStateException("No fields found to serialize");
        }

        for (Field field : fields) {
            this.fields.add(field.getName());
        }
    }

    public T createDefault() {
        try {
            return this.constructor.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Could not instantiate class", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access constructor", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Could not invoke constructor", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public IConfigElement<T> create(Field field) {
        T defaultValue;
        try {
            defaultValue = (T) field.get(field.getDeclaringClass());
        } catch (Throwable e) {
            defaultValue = this.createDefault();
        }

        return new ConfigElement<>(field, this, defaultValue);
    }

    @Override
    public IConfigElement<T> update(IConfigElement<T> element, @Nullable T obj) {
        if (obj != null) {
            element.set(obj);
        }
        return element;
    }

    @SuppressWarnings("unchecked")
    @Override
    public com.electronwill.nightconfig.core.Config serialize(IConfigElement<T> element) {
        CommentedConfig config = CommentedConfig.inMemory();
        T value = element.getFromField();
        if (value == null) {
            value = element.getDefault();
        }

        for (String fieldName : this.fields) {
            Field field;

            try {
                field = this.clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Could not find field: " + fieldName, e);
            }

            IConfigElementHandler<Object, ?> handler = (IConfigElementHandler<Object, ?>) Config.getHandler(field.getType());
            if (handler == null) {
                throw new IllegalStateException("No handler found for field type");
            }

            IConfigElement<Object> virtualElement;
            try {
                virtualElement = new VirtualAutoConfigElement<>(value, field, field.get(element.getDefault()));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Field is not public", e);
            }

            Object serialized = handler.serialize(virtualElement);

            config.set(virtualElement.getName(), serialized);
            config.setComment(virtualElement.getName(), element.getComment());
        }

        return config;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(IConfigElement<T> element, com.electronwill.nightconfig.core.Config obj) {
        if (obj == null) {
            return element.getDefault();
        }

        T value = element.getDefault();

        for (String fieldName : this.fields) {
            Field field;

            try {
                field = this.clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                throw new IllegalStateException("Could not find field: " + fieldName, e);
            }

            IConfigElementHandler<Object, Object> handler = (IConfigElementHandler<Object, Object>) Config.getHandler(field.getType());
            if (handler == null) {
                throw new IllegalStateException("No handler found for field type");
            }

            IConfigElement<Object> virtualElement;
            try {
                virtualElement = new VirtualAutoConfigElement<>(value, field, field.get(element.getDefault()));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Field is not public", e);
            }

            if (handler.canHandle(obj.get(fieldName).getClass())) {
                handler.update(virtualElement, handler.deserialize(virtualElement, obj.get(fieldName)));
            } else {
                try {
                    field.set(value, handler.deserialize(virtualElement, handler.serialize(virtualElement)));
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Field is not public", e);
                }
            }
        }

        return value;
    }

    @Override
    public boolean canHandle(Class<?> clazz) {
        return (this.clazz.equals(clazz) || this.clazz.isAssignableFrom(clazz));
    }

    public static class VirtualAutoConfigElement<T> extends ConfigElement<T> {
        private final Object parent;

        public VirtualAutoConfigElement(Object parent, Field field, T defaultValue) {
            super(field, null, defaultValue);
            this.parent = parent;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T getFromField() {
            try {
                return (T) this.getField().get(this.parent);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(String.format("Field is not static\n\tat: %s",
                        this.getField().toGenericString()), e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(String.format("Field is not public\n\tat: %s",
                        this.getField().toGenericString()), e);
            } catch (ClassCastException e) {
                throw new IllegalStateException(String.format("Field is not of type \"%s\"\n\tat: %s",
                        this.getField().getType().getName(), this.getField().toGenericString()), e);
            }
        }


        @Override
        public void set(@Nullable T value) {
            try {
                this.getField().set(this.parent, value == null ? this.getDefault() : value);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(String.format("Field is not public\n\tat: %s",
                        this.getField().toGenericString()), e);
            }
        }
    }
}
