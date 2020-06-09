package fi.jakojaannos.roguelite.engine.ecs.annotation;

import java.lang.annotation.*;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;

/**
 * Registers the system to listen for the given event via an <i>"automatic disable"</i> -handler. This causes the system
 * to be automatically enabled once the event is received.
 *
 * @see EcsSystem
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface DisableOn {
}
