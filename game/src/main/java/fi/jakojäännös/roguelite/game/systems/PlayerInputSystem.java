package fi.jakojäännös.roguelite.game.systems;

import fi.jakojäännös.roguelite.engine.ecs.Cluster;
import fi.jakojäännös.roguelite.engine.ecs.Component;
import fi.jakojäännös.roguelite.engine.ecs.ECSSystem;
import fi.jakojäännös.roguelite.engine.ecs.Entity;
import fi.jakojäännös.roguelite.game.data.GameState;
import fi.jakojäännös.roguelite.game.data.components.CharacterInput;
import fi.jakojäännös.roguelite.game.data.components.PlayerTag;
import fi.jakojäännös.roguelite.game.data.components.Position;
import lombok.val;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class PlayerInputSystem implements ECSSystem<GameState> {
    @Override
    public Collection<Class<? extends Component>> getRequiredComponents() {
        return List.of(CharacterInput.class, PlayerTag.class);
    }

    @Override
    public void tick(
            Stream<Entity> entities,
            GameState state,
            double delta,
            Cluster cluster
    ) {
        val inputHorizontal = (state.inputRight ? 1 : 0) - (state.inputLeft ? 1 : 0);
        val inputVertical = (state.inputDown ? 1 : 0) - (state.inputUp ? 1 : 0);

        entities.forEach(entity -> state.world.getComponentOf(entity, CharacterInput.class)
                                              .ifPresent(input -> {
                                                  input.move.set(inputHorizontal,
                                                                 inputVertical);
                                              }));
    }
}
