package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.Without;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;

public class FollowerRenderAdapter implements EcsSystem<FollowerRenderAdapter.Resources, FollowerRenderAdapter.EntityData, EcsSystem.NoEvents> {
    private final SkeletalMesh mesh;

    public FollowerRenderAdapter(final AssetManager assetManager) {
        this.mesh = assetManager.getStorage(SkeletalMesh.class)
                                .getOrDefault("models/bug.fbx");
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
                                         "Armature|Idle",
                                         (int) (timeManager.getCurrentGameTime()));
        });
    }

    public static record EntityData(
            Transform transform,
            EnemyTag player,
            // Differentiate between stalkers and followers by AI components
            FollowerAI ai,
            @Without StalkerAI stalkerAi,
            @Without JumpingMovementAbility jumpingAbility,
            @Without NoDrawTag noDraw
    ) {}

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager
    ) {}
}
