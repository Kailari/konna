package fi.jakojaannos.konna.view.adapters.gameplay;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.riista.view.assets.SoundEffect;
import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.events.render.GunshotEvent;

public class WeaponSoundEffectAdapter implements EcsSystem<EcsSystem.NoResources, EcsSystem.NoEntities, WeaponSoundEffectAdapter.EventData>, AutoCloseable {
    private final SoundEffect shotgun;
    private final SoundEffect melee;
    private final SoundEffect gatling;
    private final SoundEffect rifle;

    private final SoundEffect click;
    private final SoundEffect pump;
    private final SoundEffect shotgunReload;

    public WeaponSoundEffectAdapter(
            final Path assetRoot,
            final AudioContext context
    ) {
        this.shotgun = context.createEffect(assetRoot, "shotgun/Blast1.ogg");
        this.rifle = context.createEffect(assetRoot, "shotgun/Blast3.ogg");
        this.melee = context.createEffect(assetRoot, "shotgun/Pump3.ogg");
        this.gatling = context.createEffect(assetRoot, "shotgun/Blast2.ogg");

        this.click = context.createEffect(assetRoot, "shotgun/Load1.ogg");
        this.pump = context.createEffect(assetRoot, "shotgun/Pump1.ogg");
        this.shotgunReload = context.createEffect(assetRoot, "shotgun/Load2.ogg");
    }

    @Override
    public void tick(
            final NoResources resources,
            final Stream<EntityDataHandle<NoEntities>> noEntities,
            final EventData eventData
    ) {
        eventData.gunshot.forEach(gunshot -> {
            switch (gunshot.variant()) {
                case SHOTGUN -> this.rifle.play(2, 0.75f, 1.0f);
                case RIFLE -> this.rifle.play(2, 0.75f, 1.5f);
                case CLICK -> this.click.play(2, 1.5f, 2.0f);
                case SHOTGUN_RELOAD -> this.shotgunReload.play(2, 1.0f, 1.0f);
                case PUMP -> this.pump.play(2, 1.0f, 1.0f);
                case MELEE -> this.melee.play(1, 1.0f, 0.5f);
                case GATLING -> this.gatling.play(0, 0.3f, 2.0f);
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

    public static record EventData(Iterable<GunshotEvent>gunshot) {}
}
