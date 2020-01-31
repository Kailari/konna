package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.BasicWeaponStats;
import fi.jakojaannos.roguelite.game.data.components.weapon.ProjectileStats;
import fi.jakojaannos.roguelite.game.systems.characters.CharacterAttackSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CharacterAttackSystemTest {
    private CharacterAttackSystem system;
    private World world;
    private Entity entity;
    private CharacterInput characterInput;
    private CharacterAbilities characterAbilities;
    private BasicWeaponStats weaponStats;

    @BeforeEach
    void beforeEach() {
        this.system = new CharacterAttackSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.provideResource(Time.class, time);

        entity = entityManager.createEntity();
        this.characterInput = new CharacterInput();
        this.characterInput.move.set(0.0);
        this.characterInput.attack = false;
        final WalkingMovementAbility movementStats = new WalkingMovementAbility();
        this.characterAbilities = new CharacterAbilities(new DamageSource.Entity(entity));
        this.weaponStats = new BasicWeaponStats();
        entityManager.addComponentTo(entity, new Transform(0.0, 0.0));
        entityManager.addComponentTo(entity, new Velocity());
        entityManager.addComponentTo(entity, this.characterInput);
        entityManager.addComponentTo(entity, this.characterAbilities);
        entityManager.addComponentTo(entity, movementStats);
        entityManager.addComponentTo(entity, this.weaponStats);

        entityManager.applyModifications();

        // Warm-up
        for (int i = 0; i < 100; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
        }
    }

    @Test
    void characterDoesNotShootWhenInputIsFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = false;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(0, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterShootsWhenInputIsTrue() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = true;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void attackRateLimitsWhenCharacterCanShoot() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = true;
        for (int i = 0; i < 175; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
        }

        assertEquals(4, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }

    @Test
    void characterStopsAttackingWhenInputIsSetToFalse() {
        characterAbilities.attackTarget.set(10.0, 10.0);
        weaponStats.attackRate = 1.0;

        characterInput.attack = true;
        this.system.tick(Stream.of(entity), this.world);
        this.world.getEntityManager().applyModifications();

        characterInput.attack = false;
        for (int i = 0; i < 200; ++i) {
            this.system.tick(Stream.of(entity), this.world);
            this.world.getEntityManager().applyModifications();
        }

        assertEquals(1, this.world.getEntityManager().getEntitiesWith(ProjectileStats.class).count());
    }
}
