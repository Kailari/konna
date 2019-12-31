module roguelite.engine.lwjgl {
    requires static lombok;
    requires org.joml;
    requires org.lwjgl;
    requires org.lwjgl.stb;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.slf4j;
    requires jsr305;

    requires java.desktop;

    requires roguelite.engine;
    requires roguelite.engine.ecs;
    requires roguelite.engine.view;
    requires roguelite.engine.utilities;

    exports fi.jakojaannos.roguelite.engine.lwjgl;
    exports fi.jakojaannos.roguelite.engine.lwjgl.input;
    exports fi.jakojaannos.roguelite.engine.lwjgl.view.rendering;
    exports fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader;
    exports fi.jakojaannos.roguelite.engine.lwjgl.view;
    exports fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text;
}