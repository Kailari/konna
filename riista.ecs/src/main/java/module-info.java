module riista.ecs {
    requires org.slf4j;
    requires jsr305;

    requires riista.utilities;

    exports fi.jakojaannos.riista.ecs;
    exports fi.jakojaannos.riista.ecs.resources;
    exports fi.jakojaannos.riista.ecs.annotation;
    exports fi.jakojaannos.riista.ecs.legacy;

    exports fi.jakojaannos.riista.ecs.systemdata to riista.view;
}
