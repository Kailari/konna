package fi.jakojaannos.roguelite.engine.ecs.annotation;

import java.lang.annotation.*;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;

/**
 * Marks the system to be disabled by default. The system must then be explicitly enabled before it is ticked.
 *
 * @see EcsSystem
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DisabledByDefault {
}
