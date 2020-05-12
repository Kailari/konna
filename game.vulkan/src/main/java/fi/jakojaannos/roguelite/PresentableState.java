package fi.jakojaannos.roguelite;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.List;

public record PresentableState(List<Vector2d>positions) {
    public PresentableState() {
        this(new ArrayList<>());
    }
}
