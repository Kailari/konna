module roguelite.game.view {
    requires org.slf4j;
    requires org.lwjgl;
    requires org.lwjgl.stb;
    requires org.joml;
    requires jsr305;

    requires riista;
    requires riista.utilities;
    requires roguelite.engine.ecs;

    requires roguelite.engine.view;

    requires roguelite.game;

    opens fi.jakojaannos.roguelite.game.view.adapters to roguelite.engine.ecs;
    opens fi.jakojaannos.roguelite.game.view.systems to roguelite.engine.ecs;
    opens fi.jakojaannos.roguelite.game.view.systems.audio to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.game.view;
}