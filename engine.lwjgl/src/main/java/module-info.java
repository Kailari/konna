module roguelite.engine.lwjgl {
    requires org.joml;
    requires org.lwjgl;
    requires org.lwjgl.stb;
    requires org.lwjgl.glfw;
    requires org.lwjgl.opengl;
    requires org.lwjgl.openal;
    requires org.slf4j;
    requires jsr305;

    requires java.desktop;

    requires roguelite.engine.ecs;
    requires roguelite.engine.view;

    exports fi.jakojaannos.roguelite.engine.lwjgl;
    exports fi.jakojaannos.roguelite.engine.lwjgl.input;
}