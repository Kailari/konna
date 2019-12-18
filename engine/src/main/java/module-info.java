module roguelite.engine {
    requires static lombok;
    requires org.slf4j;
    requires org.joml;
    requires com.google.gson;

    requires roguelite.engine.ecs;
    requires roguelite.engine.utilities;

    requires transitive java.desktop;

    opens fi.jakojaannos.roguelite.engine.data.resources to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.engine;
    exports fi.jakojaannos.roguelite.engine.data.resources;
    exports fi.jakojaannos.roguelite.engine.state;
    exports fi.jakojaannos.roguelite.engine.tilemap;
    exports fi.jakojaannos.roguelite.engine.input;
    exports fi.jakojaannos.roguelite.engine.content;
}
