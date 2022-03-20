package org.infernalstudios.config.util.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An element annotated with {@link Nullable} claims {@code null} value is
 * perfectly valid to return (for methods), pass to (parameters) or
 * hold in (local variables and fields).
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE })
public @interface Nullable {
    /**
     * Reason when the annotated value could be null for documentation purposes.
     */
    String value() default "";
}