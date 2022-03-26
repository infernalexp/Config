package org.infernalstudios.config.util;

import java.lang.reflect.Field;
import java.util.Objects;

import org.infernalstudios.config.annotation.Category;
import org.infernalstudios.config.annotation.Configurable;
import org.infernalstudios.config.util.annotation.Nullable;

public final class Util {
    private Util() {}

    @Nullable
    public static Class<?> getPrimitive(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        if (clazz.isPrimitive()) {
            return clazz;
        } else if (clazz.equals(Boolean.class)) {
            return Boolean.TYPE;
        } else if (clazz.equals(Byte.class)) {
            return Byte.TYPE;
        } else if (clazz.equals(Character.class)) {
            return Character.TYPE;
        } else if (clazz.equals(Short.class)) {
            return Short.TYPE;
        } else if (clazz.equals(Integer.class)) {
            return Integer.TYPE;
        } else if (clazz.equals(Long.class)) {
            return Long.TYPE;
        } else if (clazz.equals(Float.class)) {
            return Float.TYPE;
        } else if (clazz.equals(Double.class)) {
            return Double.TYPE;
        } else {
            return null;
        }
    }

    public static boolean isNumber(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        if (clazz.isPrimitive()) {
            clazz = getPrimitive(clazz);
        }
        return (
            clazz.isAssignableFrom(Number.class) ||
            clazz.equals(Byte.TYPE) ||
            clazz.equals(Short.TYPE) ||
            clazz.equals(Integer.TYPE) ||
            clazz.equals(Long.TYPE) ||
            clazz.equals(Float.TYPE) ||
            clazz.equals(Double.TYPE)
        );
    }

    public static String getCategory(Field field) {
        Objects.requireNonNull(field, "field must not be null");
        Configurable configurable = field.getAnnotation(Configurable.class);
        String category;
        if (configurable != null) {
            category = configurable.category();
        } else {
            category = "";
        }
        if ("".equals(category)) {
            category = getCategory(field.getDeclaringClass());
        } else {
            String superCategory = getCategory(field.getDeclaringClass());
            if (!"".equals(superCategory)) {
                category = superCategory + "." + category;
            }
        }
        return category;
    }

    public static String getCategory(Class<?> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        Category configurable = clazz.getAnnotation(Category.class);
        String category;
        if (configurable != null) {
            category = configurable.value();
        } else {
            category = "";
        }
        Class<?> clazz2 = clazz.getDeclaringClass();
        if (clazz2 != null) {
            if ("".equals(category)) {
                category = getCategory(clazz2);
            } else {
                String superCategory = getCategory(clazz2);
                if (!"".equals(superCategory)) {
                    category = superCategory + "." + category;
                }
            }
        }
        return category;
    }
}
