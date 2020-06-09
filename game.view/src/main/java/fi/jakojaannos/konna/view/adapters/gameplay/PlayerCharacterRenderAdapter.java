package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class PlayerCharacterRenderAdapter implements EcsSystem<PlayerCharacterRenderAdapter.Resources, PlayerCharacterRenderAdapter.EntityData, EcsSystem.NoEvents> {
    private final SkeletalMesh mesh;

    public PlayerCharacterRenderAdapter(final AssetManager assetManager) {
        this.mesh = assetManager.getStorage(SkeletalMesh.class)
                                .getOrDefault("models/humanoid.fbx");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var renderer = resources.renderer;
        final var timeManager = resources.timeManager;

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;

            renderer.mesh().drawSkeletal(transform,
                                         this.mesh,
                                         "Armature|idle",
                                         (int) (timeManager.getCurrentGameTime()));
        });
    }

    public static record EntityData(
            Transform transform,
            PlayerTag player,
            @Without NoDrawTag noDraw
    ) {}

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager
    ) {}
}
