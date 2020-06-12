package fi.jakojaannos.roguelite.game.test.stepdefs.gameplay;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joml.Vector2d;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.archetypes.ProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.resources.Players;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectileKnockbackSteps {
    public static final double DELTA = 0.01;
    private Transform targetTransform;
    private Velocity targetVelocity;

    @Given("the world is blank")
    public void theWorldIsBlank() {
        final var world = simulation.state().world();
        world.clearAllEntities();
        world.fetchResource(Players.class).setLocalPlayer(null);
    }

    @Given("there is an enemy and a projectile heading towards it")
    public void thereIsAnEnemyAndAProjectileHeadingTowardsIt() {
        final var world = simulation.state().world();
        world.createEntity(targetTransform = new Transform(25.0, 25.0),
                           new Velocity(),
                           Physics.builder()
                                  .mass(1.0)
                                  .friction(50.0)
                                  .build(),
                           new Collider(CollisionLayer.ENEMY));

        ProjectileArchetype.createWeaponProjectile(world,
                                                   new Vector2d(22.0, 22.0),
                                                   new Vector2d(6.0, 6.0),
                                                   DamageSource.Generic.UNDEFINED,
                                                   CollisionLayer.PLAYER_PROJECTILE,
                                                   0,
                                                   -1,
                                                   25,
                                                   1.0);

        world.commitEntityModifications();
    }

    @When("the projectile hits the enemy")
    public void theProjectileHitsTheEnemy() {
        simulation.runsForSeconds(3);
    }

    @Then("the enemy should have moved slightly")
    public void theEnemyShouldHaveMovedSlightly() {
        assertTrue(targetTransform.position.x > 25.0);
        assertTrue(targetTransform.position.y > 25.0);
    }

    @Given("there is an enemy flying in straight line and a projectile heading towards it")
    public void thereIsAnEnemyFlyingInStraightLineAndAProjectileHeadingTowardsIt() {
        final var world = simulation.state().world();
        world.createEntity(targetTransform = new Transform(0.0, 0.0),
                           targetVelocity = new Velocity(15.0, 0.0),
                           Physics.builder().mass(1.0).friction(50.0).build(),
                           new Collider(CollisionLayer.ENEMY),
                           new InAir(0, 69420666));

        ProjectileArchetype.createWeaponProjectile(world,
                                                   new Vector2d(30.0, -12),
                                                   new Vector2d(0.0, 6.0),
                                                   DamageSource.Generic.UNDEFINED,
                                                   CollisionLayer.PLAYER_PROJECTILE,
                                                   0,
                                                   -1,
                                                   25,
                                                   1.0);

        world.commitEntityModifications();
    }

    @Then("the trajectory of said enemy changes")
    public void theTrajectoryOfSaidEnemyChanges() {
        assertEquals(targetVelocity.x, 15.0, DELTA);
        assertTrue(targetVelocity.y > 15);
    }
}
