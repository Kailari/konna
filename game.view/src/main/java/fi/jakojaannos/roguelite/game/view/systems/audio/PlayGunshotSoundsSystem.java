package fi.jakojaannos.roguelite.game.view.systems.audio;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.events.render.GunshotEvent;
import fi.jakojaannos.roguelite.game.view.data.SoundEffect;

import static org.lwjgl.openal.ALC10.*;

public class PlayGunshotSoundsSystem implements EcsSystem<EcsSystem.NoResources, EcsSystem.NoEntities, PlayGunshotSoundsSystem.Events>, AutoCloseable {
    private final SoundEffect shotgun;
    private final SoundEffect melee;
    private final SoundEffect gatling;

    private final long context;
    private final long device;

    public PlayGunshotSoundsSystem(final Path assetRoot) {
        // Create device/context
        final var defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        this.device = alcOpenDevice(defaultDeviceName);

        final int[] attributes = {0};
        this.context = alcCreateContext(this.device, attributes);
        alcMakeContextCurrent(this.context);

        final ALCCapabilities alcCapabilities = ALC.createCapabilities(this.device);
        final ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        this.shotgun = new SoundEffect(assetRoot, "shotgun/Blast1.ogg", 8);
        this.melee = new SoundEffect(assetRoot, "shotgun/Pump3.ogg", 8);
        this.gatling = new SoundEffect(assetRoot, "shotgun/Blast3.ogg", 8);
    }

    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final Events events
    ) {
        switch (events.gunshot.variant()) {
            case SHOTGUN -> this.shotgun.play();
            case MELEE -> this.melee.play();
            case GATLING -> this.gatling.play();
        }
    }

    @Override
    public void close() {
        this.shotgun.close();
        this.melee.close();
        this.gatling.close();
        alcDestroyContext(this.context);
        alcCloseDevice(this.device);
    }

    public static record Events(GunshotEvent gunshot) {}
}
