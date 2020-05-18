package fi.jakojaannos.konna.view.adapters;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.assets.AssetManager;
import fi.jakojaannos.konna.engine.assets.mesh.SkeletalMesh;
import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class PlayerCharacterRenderAdapter implements EcsRenderAdapter<PlayerCharacterRenderAdapter.Resources, PlayerCharacterRenderAdapter.EntityData> {
    private final SkeletalMesh mesh;

    public PlayerCharacterRenderAdapter(final AssetManager assetManager) {
//        this.mesh = assetManager.getStorage(SkeletalMesh.class)
//                                .get("models/humanoid.fbx");
        this.mesh = null;
    }

    @Override
    public void draw(
            final Renderer renderer,
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final long accumulator
    ) {
        final var timeManager = resources.timeManager;

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;

            renderer.mesh().drawSkeletal(transform,
                                         this.mesh,
                                         "Armature|idle",
                                         (int) (timeManager.getCurrentGameTime() / 10));
        });
    }

    public static record EntityData(
            Transform transform,
            PlayerTag player,
            @Without NoDrawTag noDraw
    ) {}

    public static record Resources(
            TimeManager timeManager
    ) {}
}
