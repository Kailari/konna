package fi.jakojaannos.riista.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;

public class GameModeRenderers {
    private final Map<Integer, Factory> factories;

    public GameModeRenderers() {
        this.factories = new HashMap<>();
    }

    public void register(final int modeId, final Factory factory) {
        this.factories.put(modeId, factory);
    }

    public Optional<SystemDispatcher> get(final int modeId) {
        if (!this.factories.containsKey(modeId)) {
            return Optional.empty();
        }

        final var factory = this.factories.get(modeId);
        return Optional.of(factory.get());
    }

    public interface Factory {
        SystemDispatcher get();
    }
}
