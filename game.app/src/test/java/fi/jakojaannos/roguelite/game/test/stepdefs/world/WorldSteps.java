package fi.jakojaannos.roguelite.game.test.stepdefs.world;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joml.Vector2d;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.getLocalPlayer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldSteps {
    private static void setPlayerKills(final int amount) {
        final var localPlayerDamageSource = getLocalPlayer()
                .flatMap(player -> player.getComponent(AttackAbility.class))
                .orElseThrow().damageSource;
        state.world()
             .fetchResource(SessionStats.class)
             .setKillsOf(localPlayerDamageSource, amount);
    }

    @Given("the world is blank with {int} enemies with {int} hp each scattered about")
    public void theWorldIsBlankWithEnemiesScatteredAbout(int numberOfEnemies, int initialHealth) {
        state.world()
             .clearAllEntities();
        state.world()
             .fetchResource(Players.class)
             .setLocalPlayer(null);

        final var cameraProperties = state.world().fetchResource(CameraProperties.class);
        cameraProperties.cameraEntity = state.world().createEntity(new Transform(),
                                                                   new NoDrawTag());

        final var areaWidth = 20;
        final var areaHeight = 20;
        IntStream.range(0, numberOfEnemies)
                 .mapToObj(ignored -> new Vector2d((random.nextDouble() * 2.0 - 1.0) * areaWidth,
                                                   (random.nextDouble() * 2.0 - 1.0) * areaHeight))
                 .forEach(enemyPosition -> {
                     final var entity = FollowerArchetype.create(state.world()::createEntity,
                                                                 new Transform(enemyPosition.x, enemyPosition.y));
                     final var health = entity.getComponent(Health.class).orElseThrow();
                     health.maxHealth = initialHealth;
                     health.currentHealth = initialHealth;
                 });
        state.world().commitEntityModifications();
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
        state.world()
             .getEntityManager()
             .getEntitiesWith(PlayerTag.class)
             .map(EntityManager.EntityComponentPair::entity)
             .map(player -> getComponentOf(player, Transform.class).orElseThrow().position)
             .flatMap(playerPosition -> Stream.of(playerPosition.add(2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(-2.0, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 2.0, new Vector2d()),
                                                  playerPosition.add(0.0, -2.0, new Vector2d()),
                                                  playerPosition.add(1.5, 0.0, new Vector2d()),
                                                  playerPosition.add(-1.5, 0.0, new Vector2d()),
                                                  playerPosition.add(0.0, 1.5, new Vector2d()),
                                                  playerPosition.add(0.0, -1.5, new Vector2d())))
             .forEach(enemyPosition -> FollowerArchetype.create(state.world().getEntityManager(),
                                                                new Transform(enemyPosition.x,
                                                                              enemyPosition.y)));
        state.world().getEntityManager().applyModifications();
    }

    @Given("there are no obstacles")
    public void there_are_no_obstacles() {
        state.world()
             .getEntityManager()
             .getEntitiesWith(ObstacleTag.class)
             .map(EntityManager.EntityComponentPair::entity)
             .forEach(state.world().getEntityManager()::destroyEntity);
        state.world().getEntityManager().applyModifications();
    }

    @And("there are no turrets")
    public void thereAreNoTurrets() {
        state.world()
             .getEntityManager()
             .getEntitiesWith(SpriteInfo.class)
             .filter(pair -> pair.component().spriteName.equals("sprites/turret"))
             .map(EntityManager.EntityComponentPair::entity)
             .forEach(state.world().getEntityManager()::destroyEntity);
        state.world().getEntityManager().applyModifications();
    }

    @Given("there are no spawners")
    public void there_are_no_spawners() {
        state.world()
             .getEntityManager()
             .getEntitiesWith(SpawnerComponent.class)
             .map(EntityManager.EntityComponentPair::entity)
             .forEach(state.world().getEntityManager()::destroyEntity);
        state.world().getEntityManager().applyModifications();
    }

    @Then("the player should still be alive.")
    public void the_player_should_still_be_alive() {
        final var player = getLocalPlayer().map(EntityHandle::asLegacyEntity);

        assertTrue(player.isPresent());

        Health health = getComponentOf(player.get(), Health.class).orElseThrow();
        assertTrue(health.currentHealth > 0);
    }

    @Then("the player should be dead.")
    public void the_player_should_be_dead() {
        final var player = getLocalPlayer().map(EntityHandle::asLegacyEntity);

        if (player.isPresent()) {
            Health health = state.world().getEntityManager().getComponentOf(player.get(), Health.class).orElseThrow();
            assertFalse(health.currentHealth > 0);
        }
    }
}
