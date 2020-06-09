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
    requires org.lwjgl.shaderc;
    requires org.lwjgl.vulkan;
    requires org.lwjgl.stb;
    requires org.lwjgl.glfw;
    requires org.lwjgl.assimp;

    requires org.lwjgl.natives;
    requires org.lwjgl.shaderc.natives;
    requires org.lwjgl.stb.natives;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.assimp.natives;

    exports fi.jakojaannos.riista.vulkan.application;
    exports fi.jakojaannos.riista.vulkan.assets.storage;
    exports fi.jakojaannos.riista.vulkan.input;
}
