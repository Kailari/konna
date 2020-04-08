module roguelite.engine.assertions {
    requires jsr305;
    requires org.junit.jupiter.api;
    requires roguelite.engine.view;
    requires roguelite.engine.utilities;
    requires roguelite.engine.ecs;
    requires roguelite.engine;

    exports fi.jakojaannos.roguelite.engine.utilities.assertions.ui;
    exports fi.jakojaannos.roguelite.engine.utilities.assertions.world;
}