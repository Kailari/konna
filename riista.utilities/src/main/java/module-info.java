module riista.utilities {
    requires org.joml;
    requires com.google.gson;
    requires jsr305;
    requires org.slf4j;

    exports fi.jakojaannos.riista.utilities;

    exports fi.jakojaannos.roguelite.engine.utilities;
    exports fi.jakojaannos.riista.utilities.json;
    exports fi.jakojaannos.riista.utilities.logging;
    exports fi.jakojaannos.roguelite.engine.utilities.math;
    exports fi.jakojaannos.riista.utilities.annotation;
}