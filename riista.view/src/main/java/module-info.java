module riista.view {
    requires org.slf4j;
    requires org.lwjgl;
    requires com.google.gson;
    requires org.joml;
    requires jsr305;

    requires java.desktop;

    requires riista;
    requires riista.ecs;
    requires riista.utilities;

    exports fi.jakojaannos.riista.view;
    exports fi.jakojaannos.riista.view.ui;
    exports fi.jakojaannos.riista.view.assets;
    exports fi.jakojaannos.riista.view.assets.ui to riista.vulkan;

    exports fi.jakojaannos.roguelite.engine.view;
    exports fi.jakojaannos.roguelite.engine.view.audio;
    exports fi.jakojaannos.roguelite.engine.view.content;
    exports fi.jakojaannos.roguelite.engine.view.ui;
    exports fi.jakojaannos.roguelite.engine.view.ui.query;
    exports fi.jakojaannos.roguelite.engine.view.rendering;
    exports fi.jakojaannos.roguelite.engine.view.rendering.text;
    exports fi.jakojaannos.roguelite.engine.view.rendering.mesh;
    exports fi.jakojaannos.roguelite.engine.view.rendering.shader;
    exports fi.jakojaannos.roguelite.engine.view.rendering.sprite;
    exports fi.jakojaannos.roguelite.engine.view.rendering.ui;

    opens fi.jakojaannos.riista.view.ui to com.google.gson;
    opens fi.jakojaannos.riista.view.ui.impl to com.google.gson;
}