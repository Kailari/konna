package fi.jakojaannos.roguelite.engine.ecs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

/**
 * Inverts a input entity data requirement of a system. Mark record component with this annotation to make the system
 * exclude all entities with the marked component. Any component marked with this annotation will always be passed to
 * system as <code>null</code>
 *
 * @see EcsSystem
 */
@Nullable
@CheckForNull
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface Without {
}
