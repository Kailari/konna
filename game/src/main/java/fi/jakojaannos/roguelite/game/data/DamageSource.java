package fi.jakojaannos.roguelite.game.data;

import javax.annotation.Nullable;

public interface DamageSource<TKiller> {
    /**
     * Whom the kills done by this damage source should be awarded to
     *
     * @return killer who is responsible for this source
     */
    TKiller getKiller();

    enum Generic implements DamageSource<Object> {
        WORLD(null),
        UNDEFINED(null);

        private static final Object UNKNOWN_KILLER = new Object();

        @Nullable private final Object killer;

        @Override
        public Object getKiller() {
            return this.killer == null ? UNKNOWN_KILLER : this.killer;
        }

        Generic(@Nullable final Object killer) {
            this.killer = killer;
        }
    }

    class Entity implements DamageSource<fi.jakojaannos.roguelite.engine.ecs.legacy.Entity> {
        private final fi.jakojaannos.roguelite.engine.ecs.legacy.Entity player;

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.Entity getKiller() {
            return this.player;
        }

        public Entity(final fi.jakojaannos.roguelite.engine.ecs.legacy.Entity player) {
            this.player = player;
        }
    }
}
