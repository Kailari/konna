package fi.jakojaannos.roguelite.game.view.systems.audio;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.event.RenderEvents;
import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.audio.SoundEffect;
import fi.jakojaannos.roguelite.game.data.events.render.GunshotEvent;

public class HandleRenderEventsSystem implements EcsSystem<HandleRenderEventsSystem.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents>, AutoCloseable {
    private final SoundEffect shotgun;
    private final SoundEffect melee;
    private final SoundEffect gatling;
    private final SoundEffect rifle;

    private final SoundEffect click;
    private final SoundEffect pump;
    private final SoundEffect shotgunReload;

    public HandleRenderEventsSystem(
            final Path assetRoot,
            final AudioContext context
    ) {
        this.shotgun = context.createEffect(assetRoot, "shotgun/Blast1.ogg", context);
        this.rifle = context.createEffect(assetRoot, "shotgun/Blast3.ogg", context);
        this.melee = context.createEffect(assetRoot, "shotgun/Pump3.ogg", context);
        this.gatling = context.createEffect(assetRoot, "shotgun/Blast2.ogg", context);

        this.click = context.createEffect(assetRoot, "shotgun/Load1.ogg", context);
        this.pump = context.createEffect(assetRoot, "shotgun/Pump1.ogg", context);
        this.shotgunReload = context.createEffect(assetRoot, "shotgun/Load2.ogg", context);
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
                    case SHOTGUN -> this.rifle.play(2, 0.75f, 1.0f);
                    case RIFLE -> this.rifle.play(2, 0.75f, 1.5f);
                    case CLICK -> this.click.play(2, 1.5f, 2.0f);
                    case SHOTGUN_RELOAD -> this.shotgunReload.play(2, 1.0f, 1.0f);
                    case PUMP -> this.pump.play(2, 1.0f, 1.0f);
                    case MELEE -> this.melee.play(1, 1.0f, 0.5f);
                    case GATLING -> this.gatling.play(0, 0.3f, 2.0f);
                }
            }
        });
    }

    @Override
    public void close() throws Exception {
        this.rifle.close();
        this.shotgun.close();
        this.melee.close();
        this.gatling.close();
        this.click.close();
        this.pump.close();
    }

    public static record Resources(RenderEvents events) {}
}
