module riista {
    requires jsr305;
    requires org.slf4j;
    requires org.joml;
    requires com.google.gson;

    requires riista.ecs;

    requires riista.utilities;

    requires transitive java.desktop;

    opens fi.jakojaannos.riista.data.resources to riista.ecs;

    exports fi.jakojaannos.riista.assets;
    exports fi.jakojaannos.riista.application;

    exports fi.jakojaannos.roguelite.engine;
    exports fi.jakojaannos.roguelite.engine.event;
    exports fi.jakojaannos.roguelite.engine.ui;
    exports fi.jakojaannos.riista.data.resources;
    exports fi.jakojaannos.roguelite.engine.state;
    exports fi.jakojaannos.roguelite.engine.tilemap;
    exports fi.jakojaannos.roguelite.engine.input;
    exports fi.jakojaannos.roguelite.engine.content;
    exports fi.jakojaannos.riista.data.components;
    exports fi.jakojaannos.roguelite.engine.network;
    exports fi.jakojaannos.roguelite.engine.network.client;
    exports fi.jakojaannos.roguelite.engine.network.message;
}
