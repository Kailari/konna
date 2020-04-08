module roguelite.game {
    requires transitive org.joml;
    requires org.slf4j;
    requires org.lwjgl.opengl;
    requires jsr305;

    requires transitive roguelite.engine;
    requires transitive roguelite.engine.utilities;
    requires transitive roguelite.engine.ecs;
    requires transitive roguelite.engine.lwjgl;

    opens fi.jakojaannos.roguelite.game.systems to roguelite.engine.ecs;
    opens fi.jakojaannos.roguelite.game.data.resources to roguelite.engine.ecs;
    opens fi.jakojaannos.roguelite.game.data.resources.collision to roguelite.engine.ecs;

    exports fi.jakojaannos.roguelite.game;
    exports fi.jakojaannos.roguelite.game.weapons;
    exports fi.jakojaannos.roguelite.game.gamemode;
    exports fi.jakojaannos.roguelite.game.data;
    exports fi.jakojaannos.roguelite.game.data.archetypes;
    exports fi.jakojaannos.roguelite.game.data.resources;
    exports fi.jakojaannos.roguelite.game.data.components;
    exports fi.jakojaannos.roguelite.game.data.components.weapon;
    exports fi.jakojaannos.roguelite.game.data.components.character;
    exports fi.jakojaannos.roguelite.game.data.components.character.enemy;
}