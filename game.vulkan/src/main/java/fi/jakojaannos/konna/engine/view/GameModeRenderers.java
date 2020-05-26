package fi.jakojaannos.konna.engine.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GameModeRenderers {
    private final Map<Integer, Factory> factories;

    public GameModeRenderers() {
        this.factories = new HashMap<>();
    }

    public void register(final int modeId, final Factory factory) {
        this.factories.put(modeId, factory);
    }

    public Optional<RenderDispatcher> get(final int modeId) {
        if (!this.factories.containsKey(modeId)) {
            return Optional.empty();
        }

        final var factory = this.factories.get(modeId);
        return Optional.of(factory.get());
    }

    public interface Factory {
        RenderDispatcher get();
    }
}
