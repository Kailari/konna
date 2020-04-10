module roguelite.engine.utilities {
    requires org.joml;
    requires com.google.gson;
    requires jsr305;
    requires org.slf4j;

    exports fi.jakojaannos.roguelite.engine.utilities;
    exports fi.jakojaannos.roguelite.engine.utilities.json;
    exports fi.jakojaannos.roguelite.engine.utilities.logging;
    exports fi.jakojaannos.roguelite.engine.utilities.math;
    exports fi.jakojaannos.roguelite.engine.utilities.annotation;
}