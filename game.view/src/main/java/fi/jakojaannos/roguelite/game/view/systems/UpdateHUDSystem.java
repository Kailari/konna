package fi.jakojaannos.roguelite.game.view.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;

public class UpdateHUDSystem implements ECSSystem {
    private final UIElement timePlayedTimer;
    private final UIElement killsCounter;

    public UpdateHUDSystem(final UserInterface userInterface) {
        this.timePlayedTimer = userInterface.findElementsWithMatchingProperty(UIProperty.NAME,
                                                                              name -> name.equals("time-played-timer"))
                                            .findFirst()
                                            .orElseThrow();
        this.killsCounter = userInterface.findElementsWithMatchingProperty(UIProperty.NAME,
                                                                           name -> name.equals("score-kills"))
                                         .findFirst()
                                         .orElseThrow();
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .requireResource(Players.class)
                    .requireProvidedResource(Time.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var timeManager = world.getResource(Time.class);
        final var sessionStats = world.getOrCreateResource(SessionStats.class);
        world.getOrCreateResource(Players.class)
             .getLocalPlayer()
             .ifPresent(localPlayer -> {
                 final var localPlayerAbilities = world.getEntityManager()
                                                       .getComponentOf(localPlayer, AttackAbility.class)
                                                       .orElseThrow();
                 final var localPlayerKills = sessionStats.getKillsOf(localPlayerAbilities.damageSource);
                 this.killsCounter.setProperty(UIProperty.TEXT, String.format("Kills: %02d",
                                                                              localPlayerKills));
             });

        final var ticks = sessionStats.endTimeStamp - sessionStats.beginTimeStamp;
        final var secondsRaw = ticks / (1000 / timeManager.getTimeStep());
        final var hours = secondsRaw / 3600;
        final var minutes = (secondsRaw - (hours * 3600)) / 60;
        final var seconds = secondsRaw - (hours * 3600) - (minutes * 60);

        this.timePlayedTimer.setProperty(UIProperty.TEXT, String.format("%02d:%02d:%02d",
                                                                        hours, minutes, seconds));
    }
}
