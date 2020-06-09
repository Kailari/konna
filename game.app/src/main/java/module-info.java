module roguelite.game.app {
    requires org.slf4j;
    requires jsr305;

    requires riista;

    requires riista.view;
    requires riista.vulkan;

    requires roguelite.game;

    requires roguelite.game.view;

    exports fi.jakojaannos.roguelite.game.app;
}