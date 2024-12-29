package org.infernalstudios.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Tells the config builder to attempt to automatically serialize and deserialize all the classes public fields.
 * These fields can be annotated with {@link org.infernalstudios.config.annotation.Configurable @Configurable}, {@link org.infernalstudios.config.annotation.Category @Category}, etc. to specify additional information.
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoSerializable {
}
