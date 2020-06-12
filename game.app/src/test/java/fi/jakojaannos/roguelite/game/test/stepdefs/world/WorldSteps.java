package fi.jakojaannos.roguelite.game.test.stepdefs.world;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joml.Vector2d;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.TurretTag;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.getLocalPlayer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.random;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulation;
import static org.junit.jupiter.api.Assertions.*;

public class WorldSteps {
    private static void setPlayerKills(final int amount) {
        final var localPlayerDamageSource = getLocalPlayer()
                .flatMap(player -> player.getComponent(AttackAbility.class))
                .orElseThrow().damageSource;
        simulation.state()
                  .world()
                  .fetchResource(SessionStats.class)
                  .setKillsOf(localPlayerDamageSource, amount);
    }

    @Given("the world is blank with {int} enemies with {int} hp each scattered about")
    public void theWorldIsBlankWithEnemiesScatteredAbout(int numberOfEnemies, int initialHealth) {
        final var world = simulation.state().world();

        world.clearAllEntities();
        world.fetchResource(Players.class)
             .setLocalPlayer(null);

        final var areaWidth = 20;
        final var areaHeight = 20;
        IntStream.range(0, numberOfEnemies)
                 .mapToObj(ignored -> new Vector2d((random.nextDouble() * 2.0 - 1.0) * areaWidth,
                                                   (random.nextDouble() * 2.0 - 1.0) * areaHeight))
                 .forEach(enemyPosition -> {
                     final var entity = FollowerArchetype.create(world, new Transform(enemyPosition));
                     final var health = entity.getComponent(Health.class).orElseThrow();
                     health.maxHealth = initialHealth;
                     health.currentHealth = initialHealth;
                 });

        world.commitEntityModifications();
    }

    @Given("the player has no kills")
    public void thePlayerHasNoKills() {
        setPlayerKills(0);
    }

    @Given("the player has {int} kills")
    public void thePlayerHasKills(int amount) {
        setPlayerKills(amount);
    }

    @Given("the player has {int} health")
    public void thePlayerHasHealth(int health) {
        getLocalPlayer().flatMap(player -> player.getComponent(Health.class))
                        .orElseThrow().currentHealth = health;
    }

    @Given("the player is surrounded by follower enemies")
    public void the_player_is_surrounded_by_follower_enemies() {
        final var world = simulation.state().world();
        world.iterateEntities(new Class[]{Transform.class, PlayerTag.class},
                              new boolean[]{false},
                              new boolean[]{false},
                              objects -> null,
                              false)
             .map(dataHandle -> dataHandle.getComponent(Transform.class).orElseThrow().position)
             .flatMap(playerPosition -> Stream.of(playerPosition.add(2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(-2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 2.0, new Vector2d()),
                                                  playerPosition.add(0.0, -2.0, new Vector2d()),
                                                  playerPosition.add(1.5, 0.0, new Vector2d()),
                                                  playerPosition.add(-1.5, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 1.5, new Vector2d()),
                                                  playerPosition.add(0.0, -1.5, new Vector2d())))
             .forEach(enemyPosition -> FollowerArchetype.create(world, new Transform(enemyPosition)));

        world.commitEntityModifications();
    }

    @Given("there are no obstacles")
    public void there_are_no_obstacles() {
        clearAllEntitiesWith(ObstacleTag.class);
    }

    @And("there are no turrets")
    public void thereAreNoTurrets() {
        clearAllEntitiesWith(TurretTag.class);
    }

    @Given("there are no spawners")
    public void there_are_no_spawners() {
        clearAllEntitiesWith(SpawnerComponent.class);
    }

    @Then("the player should still be alive.")
    public void the_player_should_still_be_alive() {
        getLocalPlayer().ifPresentOrElse(
                player -> {
                    final var health = player.getComponent(Health.class).orElseThrow();
                    assertTrue(health.currentHealth > 0, "Expected player to be alive, but their health is zero!");
                },
                () -> fail("Expected player to be alive, but player entity has been destroyed!"));
    }

    @Then("the player should be dead.")
    public void the_player_should_be_dead() {
        getLocalPlayer().ifPresent(player -> {
            final var health = player.getComponent(Health.class).orElseThrow();
            assertFalse(health.currentHealth > 0);
        });
    }

    private static void clearAllEntitiesWith(final Class<?> componentClass) {
        simulation.state()
                  .world()
                  .iterateEntities(new Class[]{componentClass},
                                   new boolean[]{false},
                                   new boolean[]{false},
                                   objects -> null,
                                   false)
                  .forEach(EntityDataHandle::destroy);
        simulation.state()
                  .world().commitEntityModifications();
    }
}
