package fi.jakojaannos.roguelite.game.systems;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.characters.ai.FollowerAIControllerSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FollowerAIControllerSystemTest {
    private Transform playerTransform;
    private Transform followerTransform;
    private MovementInput followerInput;

    void initialState(final World world) {
        playerTransform = new Transform();
        final var player = world.createEntity(playerTransform,
                                              new PlayerTag());

        world.registerResource(new Players(player));

        followerInput = new MovementInput();
        followerTransform = new Transform();
        world.createEntity(followerInput,
                           followerTransform,
                           new FollowerAI(100.0f, 0.0f));
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
        whenGame().withSystems(new FollowerAIControllerSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      this.playerTransform.position.set(playerX, playerY);
                      this.followerTransform.position.set(followerX, followerY);
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      if (followerInput.move.length() != 0) {
                          followerInput.move.normalize();
                      }

                      assertEquals(expectedDirectionX, followerInput.move.x, 0.0001f);
                      assertEquals(expectedDirectionY, followerInput.move.y, 0.0001f);
                  });
    }
}
