package fi.jakojaannos.konna;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class PresentableState {
    private final List<Vector2d> positions = new ArrayList<>();
    private long timestamp;

    public List<Vector2d> positions() {
        return this.positions;
    }

    public long timestamp() {
        return this.timestamp;
    }

    public void clear(final TimeManager timeManager) {
        this.positions.clear();
        this.timestamp = timeManager.getCurrentGameTime();
    }
}
