module riista {
    requires jsr305;
    requires org.slf4j;
    requires org.joml;
    requires com.google.gson;

    requires riista.ecs;

    requires riista.utilities;

    requires transitive java.desktop;

    opens fi.jakojaannos.riista.data.resources to riista.ecs;

    exports fi.jakojaannos.riista;
    exports fi.jakojaannos.riista.input;
    exports fi.jakojaannos.riista.assets;
    exports fi.jakojaannos.riista.application;
    exports fi.jakojaannos.riista.data.events;
    exports fi.jakojaannos.riista.data.resources;
    exports fi.jakojaannos.riista.data.components;

    exports fi.jakojaannos.roguelite.engine;
    exports fi.jakojaannos.roguelite.engine.tilemap;
    exports fi.jakojaannos.roguelite.engine.network;
    exports fi.jakojaannos.roguelite.engine.network.client;
    exports fi.jakojaannos.roguelite.engine.network.message;
}
