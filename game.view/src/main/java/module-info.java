module roguelite.game.view {
    requires org.slf4j;
    requires org.lwjgl;
    requires org.lwjgl.openal;
    requires org.lwjgl.stb;
    requires org.joml;
    requires jsr305;

    requires roguelite.engine;
    requires roguelite.engine.utilities;
    requires roguelite.engine.ecs;

    requires transitive roguelite.engine.lwjgl;
    requires roguelite.engine.view;

    requires roguelite.game;

    opens fi.jakojaannos.roguelite.game.view.systems.audio to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.game.view;

    opens fi.jakojaannos.roguelite.game.view.systems to roguelite.engine.ecs;
}