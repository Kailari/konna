package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class SessionStatsHudRenderAdapter implements EcsSystem<SessionStatsHudRenderAdapter.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    private final UiElement hud;

    public SessionStatsHudRenderAdapter(final AssetManager assetManager) {
        this.hud = assetManager.getStorage(UiElement.class)
                               .getOrDefault("ui/in-game-hud.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EcsSystem.NoEntities>> entities,
            final EcsSystem.NoEvents noEvents
    ) {
        final var renderer = resources.renderer;
        final var timeManager = resources.timeManager;
        final var sessionStats = resources.sessionStats;

        resources.players.getLocalPlayer()
                         .flatMap(player -> player.getComponent(AttackAbility.class))
                         .map(attackAbility -> sessionStats.getKillsOf(attackAbility.damageSource))
                         .ifPresent(kills -> renderer.ui().setValue("KILLS[LOCAL_PLAYER]", kills));

        final var ticks = sessionStats.endTimeStamp - sessionStats.beginTimeStamp;
        final var secondsRaw = ticks / (1000 / timeManager.getTimeStep());
        final var hours = secondsRaw / 3600;
        final var minutes = (secondsRaw - (hours * 3600)) / 60;
        final var seconds = secondsRaw - (hours * 3600) - (minutes * 60);

        renderer.ui().setValue("TIME_PLAYED_HOURS", hours);
        renderer.ui().setValue("TIME_PLAYED_MINUTES", minutes);
        renderer.ui().setValue("TIME_PLAYED_SECONDS", seconds);

        renderer.ui().draw(this.hud);
    }

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager,
            SessionStats sessionStats,
            Players players
    ) {}
}
