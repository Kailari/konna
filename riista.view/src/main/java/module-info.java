module riista.view {
    requires org.slf4j;
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
    exports fi.jakojaannos.riista.view.audio;

    opens fi.jakojaannos.riista.view.ui to com.google.gson;
    opens fi.jakojaannos.riista.view.ui.impl to com.google.gson;
}