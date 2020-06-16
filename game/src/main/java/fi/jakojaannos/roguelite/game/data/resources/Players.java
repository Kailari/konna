package fi.jakojaannos.roguelite.game.data.resources;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.riista.ecs.EntityHandle;

public class Players {
    @Nullable private EntityHandle localPlayer;

    public Optional<EntityHandle> getLocalPlayer() {
        return Optional.ofNullable(this.localPlayer);
    }

    public void setLocalPlayer(@Nullable final EntityHandle player) {
        this.localPlayer = player;
    }

    public Players() {
    }

    public Players(final EntityHandle localPlayer) {
        this.localPlayer = localPlayer;
    }

    public void removePlayer(final EntityHandle entity) {
        if (this.localPlayer == null) {
            return;
        }

        if (this.localPlayer.getId() == entity.getId()) {
            this.localPlayer = null;
        }
    }
}
