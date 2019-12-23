module roguelite.engine.view {
    requires static lombok;
    requires org.slf4j;
    requires com.google.gson;
    requires org.joml;
    requires jsr305;

    requires java.desktop;

    requires roguelite.engine;
    requires roguelite.engine.utilities;

    exports fi.jakojaannos.roguelite.engine.view;
    exports fi.jakojaannos.roguelite.engine.view.text;
    exports fi.jakojaannos.roguelite.engine.view.ui;
    exports fi.jakojaannos.roguelite.engine.view.ui.builder;
    exports fi.jakojaannos.roguelite.engine.view.sprite;
    exports fi.jakojaannos.roguelite.engine.view.content;
    exports fi.jakojaannos.roguelite.engine.view.rendering;
}