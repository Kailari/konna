package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.characters.ai.FollowerAIControllerSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FollowerAIControllerSystemTest {
    private FollowerAIControllerSystem system;
    private World world;
    private Entity follower;
    private Transform playerTransform, followerPos;
    private MovementInput followerInput;

    @BeforeEach
    void beforeEach() {
        system = new FollowerAIControllerSystem();
        this.world = World.createNew();

        this.playerTransform = new Transform();
        final var player = world.createEntity(playerTransform,
                                              new PlayerTag());

        final var players = new Players(player);
        this.world.registerResource(players);

        this.followerInput = new MovementInput();
        this.followerPos = new Transform();
        this.follower = world.createEntity(followerInput,
                                           followerPos,
                                           new FollowerAI(100.0f, 0.0f))
                             .asLegacyEntity();

        world.commitEntityModifications();
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
        this.playerTransform.position.set(playerX, playerY);
        this.followerPos.position.set(followerX, followerY);

        this.system.tick(Stream.of(follower), this.world);

        if (followerInput.move.length() != 0) {
            followerInput.move.normalize();
        }

        assertEquals(expectedDirectionX, followerInput.move.x, 0.0001f);
        assertEquals(expectedDirectionY, followerInput.move.y, 0.0001f);
    }

}
