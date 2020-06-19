module roguelite.launcher {
    requires org.slf4j;
    requires org.apache.logging.log4j;

    requires roguelite.game.app;

    requires org.lwjgl.natives;
    requires org.lwjgl.glfw.natives;
    requires org.lwjgl.shaderc.natives;
    requires org.lwjgl.stb.natives;
    requires org.lwjgl.openal.natives;
    requires org.lwjgl.assimp.natives;
}
