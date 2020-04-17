package fi.jakojaannos.roguelite.game.view.systems.audio;

import java.nio.file.Path;
import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.RenderEvents;
import fi.jakojaannos.roguelite.game.data.events.render.GunshotEvent;
import fi.jakojaannos.roguelite.game.view.data.AudioContext;
import fi.jakojaannos.roguelite.game.view.data.SoundEffect;

public class HandleRenderEventsSystem implements EcsSystem<HandleRenderEventsSystem.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents>, AutoCloseable {
    private final SoundEffect[] shotgun;
    private final SoundEffect melee;
    private final SoundEffect gatling;

    private final Random random = new Random();

    public SoundEffect getRandomShotgunBlast() {
        return this.shotgun[this.random.nextInt(this.shotgun.length)];
    }

    public HandleRenderEventsSystem(
            final Path assetRoot,
            final AudioContext context
    ) {
        this.shotgun = new SoundEffect[]{
                new SoundEffect(assetRoot, "shotgun/Blast1.ogg", context),
                new SoundEffect(assetRoot, "shotgun/Blast2.ogg", context),
                new SoundEffect(assetRoot, "shotgun/Blast3.ogg", context),
        };
        this.melee = new SoundEffect(assetRoot, "shotgun/Pump3.ogg", context);
        this.gatling = new SoundEffect(assetRoot, "shotgun/Blast2.ogg", context);
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        resources.events.events().forEach(event -> {
            if (event instanceof GunshotEvent gunshot) {
                switch (gunshot.variant()) {
                    case SHOTGUN -> getRandomShotgunBlast().play(2, 0.75f, 1.5f);
                    case MELEE -> this.melee.play(1, 1.0f, 0.5f);
                    case GATLING -> this.gatling.play(0, 0.3f, 2.0f);
                }
            }
        });
    }

    @Override
    public void close() {
        for (final var soundEffect : this.shotgun) {
            soundEffect.close();
        }
        this.melee.close();
        this.gatling.close();
    }

    public static record Resources(RenderEvents events) {}
}
