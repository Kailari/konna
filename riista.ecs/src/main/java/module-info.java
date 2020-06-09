module riista.ecs {
    requires org.slf4j;
    requires jsr305;

    requires riista.utilities;

    exports fi.jakojaannos.roguelite.engine.ecs;
    exports fi.jakojaannos.roguelite.engine.ecs.data.resources;
    exports fi.jakojaannos.roguelite.engine.ecs.annotation;
    exports fi.jakojaannos.roguelite.engine.ecs.legacy;

    exports fi.jakojaannos.roguelite.engine.ecs.systemdata to riista.view;
}
