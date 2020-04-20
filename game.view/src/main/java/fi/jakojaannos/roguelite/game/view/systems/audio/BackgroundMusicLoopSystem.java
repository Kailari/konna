package fi.jakojaannos.roguelite.game.view.systems.audio;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.audio.MusicPlayer;
import fi.jakojaannos.roguelite.engine.view.audio.MusicTrack;

public class BackgroundMusicLoopSystem implements EcsSystem<EcsSystem.NoResources, EcsSystem.NoEntities, EcsSystem.NoEvents>, AutoCloseable {
    private final MusicTrack background;
    private final MusicTrack backgroundIntense;
    private final MusicPlayer musicPlayer;

    public BackgroundMusicLoopSystem(final Path assetRoot, final AudioContext audioContext) {
        this.background = audioContext.createTrack(assetRoot.resolve("music/Desolation1_noperc.ogg"));
        this.backgroundIntense = audioContext.createTrack(assetRoot.resolve("music/Desolation1.ogg"));
        this.musicPlayer = audioContext.createMusicPlayer();

        this.musicPlayer.playNow(this.background);
    }

    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<NoEntities>> noEntities,
            final NoEvents noEvents
    ) {
        this.musicPlayer.nextTrack(this.backgroundIntense);
        this.musicPlayer.update();
    }

    @Override
    public void close() throws Exception {
        this.background.close();
        this.backgroundIntense.close();
        this.musicPlayer.close();
    }
}
