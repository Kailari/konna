package fi.jakojaannos.roguelite.game.data.resources;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Resource;

public class Players implements Resource {
    @Nullable private Entity localPlayer;

    @Deprecated
    @Nullable
    public Entity getPlayer() {
        // FIXME: Add an additional "removed" state for entities and use that instead. Currently,
        //  the player can be nulled during the last tick they are considered alive
        if (this.localPlayer != null && this.localPlayer.isMarkedForRemoval()) {
            this.localPlayer = null;
        }

        return this.localPlayer;
    }

    public Optional<Entity> getLocalPlayer() {
        return Optional.ofNullable(this.localPlayer);
    }

    public void setLocalPlayer(@Nullable final Entity player) {
        this.localPlayer = player;
    }

    public void removePlayer(final Entity entity) {
        if (this.localPlayer == null) {
            return;
        }

        if (this.localPlayer.getId() == entity.getId()) {
            this.localPlayer = null;
        }
    }
}
