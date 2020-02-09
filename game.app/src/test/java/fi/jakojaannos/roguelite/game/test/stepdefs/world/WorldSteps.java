package fi.jakojaannos.roguelite.game.test.stepdefs.world;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joml.Vector2d;

import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.getLocalPlayer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldSteps {
    @Given("the world is blank with {int} enemies with {int} hp each scattered about")
    public void theWorldIsBlankWithEnemiesScatteredAbout(int numberOfEnemies, int initialHealth) {
        state.getWorld()
             .getEntityManager()
             .clearEntities();

        final var camera = state.getWorld().getEntityManager().createEntity();
        state.getWorld().getEntityManager().addComponentTo(camera, new Transform());
        state.getWorld().getEntityManager().addComponentTo(camera, new NoDrawTag());
        state.getWorld().getOrCreateResource(CameraProperties.class).cameraEntity = camera;

        final var areaWidth = 20;
        final var areaHeight = 20;
        IntStream.range(0, numberOfEnemies)
                 .mapToObj(ignored -> new Vector2d((random.nextDouble() * 2.0 - 1.0) * areaWidth,
                                                   (random.nextDouble() * 2.0 - 1.0) * areaHeight))
                 .forEach(enemyPosition -> {
                     final var entity = FollowerArchetype.create(state.getWorld().getEntityManager(),
                                                                 new Transform(enemyPosition.x, enemyPosition.y));
                     final var health = state.getWorld()
                                             .getEntityManager()
                                             .getComponentOf(entity, Health.class)
                                             .orElseThrow();
                     health.maxHealth = initialHealth;
                     health.currentHealth = initialHealth;
                 });
        state.getWorld().getEntityManager().applyModifications();
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
        getComponentOf(getLocalPlayer().orElseThrow(), Health.class)
                .orElseThrow()
                .currentHealth = health;
    }

    @Given("the player is surrounded by follower enemies")
    public void the_player_is_surrounded_by_follower_enemies() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(PlayerTag.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .map(player -> getComponentOf(player, Transform.class).orElseThrow().position)
             .flatMap(playerPosition -> Stream.of(playerPosition.add(2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(-2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 2.0, new Vector2d()),
                                                  playerPosition.add(0.0, -2.0, new Vector2d()),
                                                  playerPosition.add(1.5, 0.0, new Vector2d()),
                                                  playerPosition.add(-1.5, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 1.5, new Vector2d()),
                                                  playerPosition.add(0.0, -1.5, new Vector2d())))
             .forEach(enemyPosition -> FollowerArchetype.create(state.getWorld().getEntityManager(),
                                                                new Transform(enemyPosition.x,
                                                                              enemyPosition.y)));
        state.getWorld().getEntityManager().applyModifications();
    }

    @Given("there are no obstacles")
    public void there_are_no_obstacles() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(ObstacleTag.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
        state.getWorld().getEntityManager().applyModifications();
    }

    @And("there are no turrets")
    public void thereAreNoTurrets() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(SpriteInfo.class)
             .filter(pair -> pair.getComponent().spriteName.equals("sprites/turret"))
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
        state.getWorld().getEntityManager().applyModifications();
    }

    @Given("there are no spawners")
    public void there_are_no_spawners() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(SpawnerComponent.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
        state.getWorld().getEntityManager().applyModifications();
    }

    @Then("the player should still be alive.")
    public void the_player_should_still_be_alive() {
        Optional<Entity> player = getLocalPlayer();

        assertTrue(player.isPresent());

        Health health = getComponentOf(player.get(), Health.class).orElseThrow();
        assertTrue(health.currentHealth > 0);
    }

    @Then("the player should be dead.")
    public void the_player_should_be_dead() {
        Optional<Entity> player = getLocalPlayer();

        if (player.isPresent()) {
            Health health = state.getWorld().getEntityManager().getComponentOf(player.get(), Health.class).orElseThrow();
            assertFalse(health.currentHealth > 0);
        }
    }

    private static void setPlayerKills(final int amount) {
        final var localPlayerDamageSource = getComponentOf(getLocalPlayer().orElseThrow(), AttackAbility.class)
                .orElseThrow()
                .damageSource;
        state.getWorld()
             .getOrCreateResource(SessionStats.class)
             .setKillsOf(localPlayerDamageSource, amount);
    }
}
