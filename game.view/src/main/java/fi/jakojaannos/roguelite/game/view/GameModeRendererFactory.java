package fi.jakojaannos.roguelite.game.view;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.view.GameModeRenderer;

public class GameModeRendererFactory {
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends GameMode>, Factory> renderers;

    public GameModeRendererFactory() {
        this.renderers = new HashMap<>();
    }

    public <TGameMode extends GameMode> void register(
            final Class<TGameMode> modeClass,
            final Factory<TGameMode> factory
    ) {
        this.renderers.put(modeClass, factory);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <TGameMode extends GameMode> GameModeRenderer<TGameMode> get(final TGameMode mode) {
        final var modeClass = mode.getClass();
        if (!this.renderers.containsKey(modeClass)) {
            return null;
        }

        return (GameModeRenderer<TGameMode>) this.renderers.get(modeClass).get(mode);
    }

    public interface Factory<TGameMode extends GameMode> {
        GameModeRenderer<TGameMode> get(TGameMode gameMode);
    }
}
