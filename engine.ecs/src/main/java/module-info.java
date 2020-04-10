module roguelite.engine.ecs {
    requires org.slf4j;
    requires jsr305;

    requires roguelite.engine.utilities;

    exports fi.jakojaannos.roguelite.engine.ecs;
    exports fi.jakojaannos.roguelite.engine.ecs.annotation;
    exports fi.jakojaannos.roguelite.engine.ecs.legacy;
}
