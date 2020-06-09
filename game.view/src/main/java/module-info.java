module roguelite.game.view {
    requires org.slf4j;
    requires org.lwjgl;
    requires org.lwjgl.stb;
    requires org.joml;
    requires jsr305;

    requires riista;
    requires riista.ecs;
    requires riista.utilities;

    requires riista.view;
    requires riista.vulkan;

    requires roguelite.game;

    opens fi.jakojaannos.konna.view.adapters.menu to riista.ecs;
    opens fi.jakojaannos.konna.view.adapters.gameplay to riista.ecs;

    exports fi.jakojaannos.konna.view;
}