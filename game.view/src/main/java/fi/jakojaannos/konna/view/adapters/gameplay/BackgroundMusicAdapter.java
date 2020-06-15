package fi.jakojaannos.konna.view.adapters.gameplay;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.riista.view.assets.MusicTrack;
import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.riista.view.audio.MusicPlayer;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;

public class BackgroundMusicAdapter implements EcsSystem<EcsSystem.NoResources, EcsSystem.NoEntities, EcsSystem.NoEvents>, AutoCloseable {
    private final MusicTrack background;
    private final MusicTrack backgroundIntense;
    private final MusicPlayer musicPlayer;

    public BackgroundMusicAdapter(final Path assetRoot, final AudioContext audioContext) {
        // FIXME: Load using asset manager
        this.background = audioContext.createTrack(assetRoot.resolve("music/Desolation1_noperc.ogg"));
        this.backgroundIntense = audioContext.createTrack(assetRoot.resolve("music/Desolation1.ogg"));
        this.musicPlayer = audioContext.createMusicPlayer();

        this.musicPlayer.playNow(this.background);
    }

    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        this.musicPlayer.nextTrack(this.backgroundIntense);
        this.musicPlayer.update();
    }

    @Override
    public void close() {
        this.background.close();
        this.backgroundIntense.close();
        this.musicPlayer.close();
    }
}
