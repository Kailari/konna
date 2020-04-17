package fi.jakojaannos.roguelite.game.view.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.event.RenderEvents;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.Color;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import fi.jakojaannos.roguelite.game.weapons.ActionInfo;
import fi.jakojaannos.roguelite.game.weapons.WeaponInventory;

public class UpdateHUDSystem implements EcsSystem<UpdateHUDSystem.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    private static final Color COLOR_BAD = new Color(0.75, 0.15, 0.15);
    private static final Color COLOR_GOOD = new Color(0.95, 0.95, 0.95);

    private final UIElement timePlayedTimer;
    private final UIElement killsCounter;
    private final UIElement ammoCounter;

    public UpdateHUDSystem(final UserInterface userInterface) {
        this.timePlayedTimer = userInterface.findElements(that -> that.hasName().equalTo("time-played-timer"))
                                            .findFirst()
                                            .orElseThrow();
        this.killsCounter = userInterface.findElements(that -> that.hasName().equalTo("score-kills"))
                                         .findFirst()
                                         .orElseThrow();
        this.ammoCounter = userInterface.findElements(that -> that.hasName().equalTo("weapon-ammo"))
                                        .findFirst()
                                        .orElseThrow();
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        final var timeManager = resources.timeManager;
        final var sessionStats = resources.sessionStats;
        resources.players
                .getLocalPlayer()
                .ifPresent(localPlayer -> {
                    final var localPlayerAbilities = localPlayer.getComponent(AttackAbility.class)
                                                                .orElseThrow();
                    final var inventory = localPlayer.getComponent(WeaponInventory.class)
                                                     .orElseThrow();
                    final var playerPos = localPlayer.getComponent(Transform.class).orElseThrow();

                    final var localPlayerKills = sessionStats.getKillsOf(localPlayerAbilities.damageSource);
                    this.killsCounter.setProperty(UIProperty.TEXT, String.format("Kills: %02d",
                                                                                 localPlayerKills));

                    final var info = new ActionInfo(timeManager,
                                                    resources.entities,
                                                    playerPos,
                                                    localPlayerAbilities,
                                                    resources.events);
                    final var query = inventory.getWeaponAtSlot(localPlayerAbilities.equippedSlot)
                                               .doStateQuery(info);
                    final int ammo = query.currentAmmo;
                    final int maxAmmo = query.maxAmmo;
                    final var maxAmmoString = maxAmmo == 666 ? "REL" : String.format("%03d", maxAmmo);
                    this.ammoCounter.setProperty(UIProperty.TEXT, String.format("%03d/%s", ammo, maxAmmoString));
                    this.ammoCounter.setProperty(UIProperty.COLOR, ammo == 0 ? COLOR_BAD : COLOR_GOOD);
                });

        final var ticks = sessionStats.endTimeStamp - sessionStats.beginTimeStamp;
        final var secondsRaw = ticks / (1000 / timeManager.getTimeStep());
        final var hours = secondsRaw / 3600;
        final var minutes = (secondsRaw - (hours * 3600)) / 60;
        final var seconds = secondsRaw - (hours * 3600) - (minutes * 60);

        this.timePlayedTimer.setProperty(UIProperty.TEXT, String.format("%02d:%02d:%02d",
                                                                        hours, minutes, seconds));
    }

    public static record Resources(
            TimeManager timeManager,
            Players players,
            SessionStats sessionStats,
            Entities entities,
            RenderEvents events
    ) {}
}
