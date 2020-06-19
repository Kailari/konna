package fi.jakojaannos.riista.ecs.annotation;

import java.lang.annotation.*;

import fi.jakojaannos.riista.ecs.EcsSystem;

/**
 * Inverts a input entity data requirement of a system. Mark record component with this annotation to make the system
 * exclude all entities with the marked component. Any component marked with this annotation will always be passed to
 * system as <code>null</code>
 *
 * @see EcsSystem
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Without {
}
