package fi.jakojaannos.roguelite.game.test.stepdefs.world;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joml.Vector2d;

import java.util.Optional;
import java.util.stream.Stream;

import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.getLocalPlayer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.getComponentOf;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.state;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorldSteps {
    @Given("the world is blank with {int} enemies scattered about")
    public void theWorldIsBlankWithEnemiesScatteredAbout(int numberOfEnemies) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
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
                                                  playerPosition.add(0.0, -2.0, new Vector2d())))
             .forEach(enemyPosition -> FollowerArchetype.create(state.getWorld().getEntityManager(),
                                                                new Transform(enemyPosition.x,
                                                                              enemyPosition.y)));
    }

    @Given("there are no obstacles")
    public void there_are_no_obstacles() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(ObstacleTag.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
    }

    @Given("there are no spawners")
    public void there_are_no_spawners() {
        state.getWorld()
             .getEntityManager()
             .getEntitiesWith(SpawnerComponent.class)
             .map(EntityManager.EntityComponentPair::getEntity)
             .forEach(state.getWorld().getEntityManager()::destroyEntity);
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
}
