package fi.jakojaannos.roguelite.game.test.stepdefs.gameplay;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.archetypes.ProjectileArchetype;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.resources.Players;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulateSeconds;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.state;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProjectileKnockbackSteps {
    public static final double DELTA = 0.01;
    private Entity target;
    private Transform targetTransform;
    private Velocity targetVelocity;

    @Given("the world is blank")
    public void theWorldIsBlank() {
        state.world().clearAllEntities();
        state.world().fetchResource(Players.class).setLocalPlayer(null);
        state.world().fetchResource(CameraProperties.class).cameraEntity = null;
    }

    @Given("there is an enemy and a projectile heading towards it")
    public void thereIsAnEnemyAndAProjectileHeadingTowardsIt() {
        EntityManager entityManager = state.world().getEntityManager();
        target = entityManager.createEntity();
        state.world().createEntity(targetTransform = new Transform(25.0, 25.0),
                                   new Velocity(),
                                   Physics.builder()
                                             .mass(1.0)
                                             .friction(50.0)
                                             .build(),
                                   new Collider(CollisionLayer.ENEMY));

        ProjectileArchetype.createWeaponProjectile(entityManager,
                                                   new Vector2d(22.0, 22.0),
                                                   new Vector2d(6.0, 6.0),
                                                   DamageSource.Generic.UNDEFINED,
                                                   CollisionLayer.PLAYER_PROJECTILE,
                                                   0,
                                                   -1,
                                                   25);

        entityManager.applyModifications();
    }

    @When("the projectile hits the enemy")
    public void theProjectileHitsTheEnemy() {
        simulateSeconds(3);
    }

    @Then("the enemy should have moved slightly")
    public void theEnemyShouldHaveMovedSlightly() {
        assertTrue(targetTransform.position.x > 25.0);
        assertTrue(targetTransform.position.y > 25.0);
    }

    @Given("there is an enemy flying in straight line and a projectile heading towards it")
    public void thereIsAnEnemyFlyingInStraightLineAndAProjectileHeadingTowardsIt() {
        EntityManager entityManager = state.world().getEntityManager();
        target = entityManager.createEntity();
        entityManager.addComponentTo(target, targetTransform = new Transform(0.0, 0.0));
        entityManager.addComponentTo(target, targetVelocity = new Velocity(15.0, 0.0));
        entityManager.addComponentTo(target, Physics.builder().mass(1.0).friction(50.0).build());
        entityManager.addComponentTo(target, new Collider(CollisionLayer.ENEMY));
        entityManager.addComponentTo(target, new InAir(0, 69420666));

        ProjectileArchetype.createWeaponProjectile(entityManager,
                                                   new Vector2d(30.0, -12),
                                                   new Vector2d(0.0, 6.0),
                                                   DamageSource.Generic.UNDEFINED,
                                                   CollisionLayer.PLAYER_PROJECTILE,
                                                   0,
                                                   -1,
                                                   25);

        entityManager.applyModifications();
    }

    @Then("the trajectory of said enemy changes")
    public void theTrajectoryOfSaidEnemyChanges() {
        assertEquals(targetVelocity.x, 15.0, DELTA);
        assertTrue(targetVelocity.y > 15);
    }
}
