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
        } else if (Boolean.class.equals(clazz)) {
            return Boolean.TYPE;
        } else if (Byte.class.equals(clazz)) {
            return Byte.TYPE;
        } else if (Character.class.equals(clazz)) {
            return Character.TYPE;
        } else if (Short.class.equals(clazz)) {
            return Short.TYPE;
        } else if (Integer.class.equals(clazz)) {
            return Integer.TYPE;
        } else if (Long.class.equals(clazz)) {
            return Long.TYPE;
        } else if (Float.class.equals(clazz)) {
            return Float.TYPE;
        } else if (Double.class.equals(clazz)) {
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
            Number.class.isAssignableFrom(clazz) ||
            Byte.TYPE.equals(clazz) ||
            Short.TYPE.equals(clazz) ||
            Integer.TYPE.equals(clazz) ||
            Long.TYPE.equals(clazz) ||
            Float.TYPE.equals(clazz) ||
            Double.TYPE.equals(clazz)
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
