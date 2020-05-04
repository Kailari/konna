package fi.jakojaannos.roguelite.game.weapons.modules;

import java.util.ArrayList;
import java.util.List;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.weapons.*;

public class OverheatBaseModule implements WeaponModule<OverheatBaseModule.Attributes> {
    private final List<HeatSource> heatSources = new ArrayList<>();

    @Override
    public void register(final WeaponHooks hooks, final Attributes attributes) {
        hooks.weaponStateQuery(this::stateQuery, Phase.TRIGGER);

        hooks.registerStateFactory(State.class, State::new);
    }

    private void stateQuery(
            final Weapon weapon,
            final WeaponStateQuery query,
            final ActionInfo info
    ) {
        final var state = weapon.getState(State.class);

        query.heat = state.getHeat(weapon, info.timeManager());
    }

    public void registerHeatSource(final HeatSource source) {
        this.heatSources.add(source);
    }

    public static record Attributes() {}

    public class State {
        private double heat;

        public double getHeat(final Weapon weapon, final TimeManager timeManager) {
            final var delta = OverheatBaseModule.this.heatSources
                    .stream()
                    .mapToDouble(source -> source.getHeatDeltaSinceLastQuery(weapon, timeManager))
                    .sum();

            this.heat += delta;
            if(this.heat < 0){
                this.heat = 0;
            }
            return this.heat;
        }
    }
}
