module roguelite.engine.view {
    requires org.slf4j;
    requires org.lwjgl;
    requires com.google.gson;
    requires org.joml;
    requires jsr305;

    requires java.base; // HACK: Allows casting to ParameterizedType
    requires java.desktop;

    requires roguelite.engine;
    requires roguelite.engine.ecs;
    requires roguelite.engine.utilities;

    opens fi.jakojaannos.roguelite.engine.view.systems.ui to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.engine.view;
    exports fi.jakojaannos.roguelite.engine.view.audio;
    exports fi.jakojaannos.roguelite.engine.view.data.components.ui;
    exports fi.jakojaannos.roguelite.engine.view.data.resources.ui;
    exports fi.jakojaannos.roguelite.engine.view.content;
    exports fi.jakojaannos.roguelite.engine.view.ui;
    exports fi.jakojaannos.roguelite.engine.view.ui.query;
    exports fi.jakojaannos.roguelite.engine.view.ui.builder;
    exports fi.jakojaannos.roguelite.engine.view.rendering;
    exports fi.jakojaannos.roguelite.engine.view.rendering.text;
    exports fi.jakojaannos.roguelite.engine.view.rendering.mesh;
    exports fi.jakojaannos.roguelite.engine.view.rendering.shader;
    exports fi.jakojaannos.roguelite.engine.view.rendering.sprite;
    exports fi.jakojaannos.roguelite.engine.view.rendering.ui;
}