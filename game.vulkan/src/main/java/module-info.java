module konna.vulkan {
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.joml;
    requires com.google.gson;
    requires jsr305;

    requires riista;
    requires riista.ecs;
    requires riista.utilities;

    requires riista.view;
    requires riista.vulkan;

    requires roguelite.game;

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

    opens fi.jakojaannos.konna.view.adapters.menu to riista.ecs;
    opens fi.jakojaannos.konna.view.adapters.gameplay to riista.ecs;

    exports fi.jakojaannos.konna.view;
}
