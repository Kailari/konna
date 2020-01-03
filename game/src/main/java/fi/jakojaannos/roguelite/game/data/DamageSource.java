package fi.jakojaannos.roguelite.game.data;

import lombok.RequiredArgsConstructor;

public interface DamageSource<TKiller> {
    /**
     * Whom the kills done by this damage source should be awarded to
     *
     * @return killer who is responsible for this source
     */
    TKiller getKiller();

    @RequiredArgsConstructor
    enum Generic implements DamageSource<Object> {
        WORLD(null),
        UNDEFINED(null);

        private static final Object UNKNOWN_KILLER = new Object();

        private final Object killer;

        @Override
        public Object getKiller() {
            return this.killer == null ? UNKNOWN_KILLER : this.killer;
        }
    }

    @RequiredArgsConstructor
    class Entity implements DamageSource<fi.jakojaannos.roguelite.engine.ecs.Entity> {
        private final fi.jakojaannos.roguelite.engine.ecs.Entity player;

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.Entity getKiller() {
            return this.player;
        }
    }
}
