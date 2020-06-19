module riista.vulkan {
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.joml;
    requires com.google.gson;
    requires jsr305;

    requires riista;
    requires riista.ecs;
    requires riista.utilities;

    requires riista.view;

    requires org.lwjgl;
    requires org.lwjgl.openal;
    requires org.lwjgl.shaderc;
    requires org.lwjgl.vulkan;
    requires org.lwjgl.stb;
    requires org.lwjgl.glfw;
    requires org.lwjgl.assimp;

    // Allows gson to construct UI elements
    exports fi.jakojaannos.riista.vulkan.assets.ui to com.google.gson;
    opens fi.jakojaannos.riista.vulkan.assets.ui to com.google.gson;

    exports fi.jakojaannos.riista.vulkan.application;
    exports fi.jakojaannos.riista.vulkan.assets.storage;
    exports fi.jakojaannos.riista.vulkan.input;
    exports fi.jakojaannos.riista.vulkan.renderer;
}
