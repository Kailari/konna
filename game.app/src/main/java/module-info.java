module roguelite.game.app {
    requires static lombok;
    requires org.slf4j;
    requires jsr305;

    requires roguelite.engine;
    requires roguelite.engine.lwjgl;

    requires roguelite.game;
    requires roguelite.game.view;

    exports fi.jakojaannos.roguelite.game.app;
}