package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerEnemyAI;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CharacterAIControllerSystemTest {
    private CharacterAIControllerSystem system;
    private World world;
    private Entity player, follower;
    private Transform playerPos, followerPos;
    private CharacterInput followerInput;

    @BeforeEach
    void beforeEach() {
        system = new CharacterAIControllerSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        this.world = World.createNew(entityManager);

        this.player = entityManager.createEntity();
        this.playerPos = new Transform();
        entityManager.addComponentTo(this.player, playerPos);
        entityManager.addComponentTo(this.player, new PlayerTag());
        this.world.getOrCreateResource(Players.class).setLocalPlayer(player);

        this.follower = entityManager.createEntity();
        this.followerInput = new CharacterInput();
        this.followerPos = new Transform();
        entityManager.addComponentTo(this.follower, followerInput);
        entityManager.addComponentTo(this.follower, followerPos);
        entityManager.addComponentTo(this.follower, new FollowerEnemyAI(100.0f, 0.0f));

        entityManager.applyModifications();
    }


    @ParameterizedTest
    @CsvSource({
                       "1.0f,1.0f,1.0f,1.0f,0.0f,0.0f",
                       "10.0f,10.0f,10.0f,10.0f,0.0f,0.0f",
                       "1.0f,1.0f,1.0f,0.0f,0f,1f",
                       "1.0f,1.0f,0.0f,1.0f,1f,0f",
                       "1.0f,1.0f,0.0f,0.0f,0.7071f,0.7071f",
                       "50.0f,50.0f,0.0f,0.0f,0.7071f,0.7071f",
                       "50.0f,50.0f,100.0f,100.0f,-0.7071f,-0.7071f",
                       "41.0f,24.0f,7.0f,-32.0f,0.5189f,0.8547f"

               })
    void aiControllerSystemModifiesCharacterInputCorrectly(
            double playerX,
            double playerY,
            double followerX,
            double followerY,
            double expectedDirectionX,
            double expectedDirectionY
    ) {
        this.playerPos.position.set(playerX, playerY);
        this.followerPos.position.set(followerX, followerY);

        this.system.tick(Stream.of(follower), this.world);

        if (followerInput.move.length() != 0)
            followerInput.move.normalize();

        assertEquals(expectedDirectionX, followerInput.move.x, 0.0001f);
        assertEquals(expectedDirectionY, followerInput.move.y, 0.0001f);
    }

}
