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

    requires roguelite.game;

    opens fi.jakojaannos.roguelite.game.view.adapters to riista.ecs;
    opens fi.jakojaannos.roguelite.game.view.systems to riista.ecs;
    opens fi.jakojaannos.roguelite.game.view.systems.audio to riista.ecs;

    exports fi.jakojaannos.roguelite.game.view;
}