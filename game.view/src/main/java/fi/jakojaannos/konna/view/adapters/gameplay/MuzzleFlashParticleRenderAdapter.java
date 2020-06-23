package fi.jakojaannos.konna.view.adapters.gameplay;

import org.joml.Vector3d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.Renderer;

public class MuzzleFlashParticleRenderAdapter implements EcsSystem<MuzzleFlashParticleRenderAdapter.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    public MuzzleFlashParticleRenderAdapter(final AssetManager assetManager) {
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        final var timeManager = resources.timeManager;

        final var maxTranslation = 4.0;
        final var duration = 40;
        final var spread = 2.0;

        final var random = new Random(1337L);
        for (int i = 0; i < 10000; i++) {
            final var x = random.nextDouble() * (timeManager.getCurrentGameTime() % duration) * maxTranslation;
            resources.renderer.particles()
                              .drawParticleSystem(new Vector3d(x,
                                                               ((random.nextDouble() * 2.0) - 1.0) * spread,
                                                               1.0));
        }
    }

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager
    ) {}
}
