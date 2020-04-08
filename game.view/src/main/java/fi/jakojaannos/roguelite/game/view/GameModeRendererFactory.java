package fi.jakojaannos.roguelite.game.view;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.view.GameModeRenderer;

public class GameModeRendererFactory {
    private final Map<Integer, Factory> factories;

    public GameModeRendererFactory() {
        this.factories = new HashMap<>();
    }

    public void register(final int modeId, final Factory factory) {
        this.factories.put(modeId, factory);
    }

    @Nullable
    public GameModeRenderer get(final GameMode mode) {
        if (!this.factories.containsKey(mode.id())) {
            return null;
        }

        final var factory = this.factories.get(mode.id());
        return factory.get(mode);
    }

    public interface Factory {
        GameModeRenderer get(GameMode gameMode);
    }
}
