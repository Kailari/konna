module roguelite.game {
    requires transitive org.joml;
    requires org.slf4j;
    requires jsr305;

    requires transitive riista;
    requires transitive riista.ecs;
    requires transitive riista.utilities;

    opens fi.jakojaannos.roguelite.game.systems to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.characters to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.cleanup to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.collision to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.menu to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.physics to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.characters.ai to riista.ecs;
    opens fi.jakojaannos.roguelite.game.systems.characters.movement to riista.ecs;
    opens fi.jakojaannos.roguelite.game.data.resources to riista.ecs;
    opens fi.jakojaannos.roguelite.game.data.resources.collision to riista.ecs;

    exports fi.jakojaannos.roguelite.game;
    exports fi.jakojaannos.roguelite.game.weapons;
    exports fi.jakojaannos.roguelite.game.weapons.events;
    exports fi.jakojaannos.roguelite.game.weapons.modules;
    exports fi.jakojaannos.roguelite.game.gamemode;
    exports fi.jakojaannos.roguelite.game.data;
    exports fi.jakojaannos.roguelite.game.data.events;
    exports fi.jakojaannos.roguelite.game.data.events.render;
    exports fi.jakojaannos.roguelite.game.data.archetypes;
    exports fi.jakojaannos.roguelite.game.data.resources;
    exports fi.jakojaannos.roguelite.game.data.components;
    exports fi.jakojaannos.roguelite.game.data.components.weapon;
    exports fi.jakojaannos.roguelite.game.data.components.character;
    exports fi.jakojaannos.roguelite.game.data.components.character.enemy;
}