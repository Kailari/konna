module koetus.assertions {
    requires jsr305;
    requires org.joml;
    requires org.junit.jupiter.api;

    requires riista;
    requires riista.ecs;
    requires riista.utilities;
    requires riista.view;

    exports fi.jakojaannos.roguelite.engine.utilities.assertions.world;
    exports fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension;
}