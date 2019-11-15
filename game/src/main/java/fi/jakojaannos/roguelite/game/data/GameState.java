package fi.jakojaannos.roguelite.game.data;

import fi.jakojaannos.roguelite.engine.ecs.Cluster;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.Roguelite;
import lombok.NonNull;

public class GameState {
    public float targetWorldSize = 32.0f;

    public float realViewWidth = 1.0f;
    public float realViewHeight = 1.0f;

    public double mouseX = -999.0;
    public double mouseY = -999.0;

    public boolean inputLeft = false;
    public boolean inputRight = false;
    public boolean inputUp = false;
    public boolean inputDown = false;
    public boolean inputAttack = true;

    public double playerSpeed = 8.0f;
    public double playerSize = 1.0f;

    public float crosshairSize = 0.5f;

    public Entity player;
    public Entity crosshair;
    @NonNull public Cluster world;

    public GameState() {
        this(new Cluster(256));
    }

    public GameState(@NonNull Cluster world) {
        this.world = world;
    }
}
