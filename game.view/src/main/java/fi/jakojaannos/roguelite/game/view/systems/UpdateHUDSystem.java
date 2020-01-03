package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import lombok.val;

import java.util.stream.Stream;

public class UpdateHUDSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .requireResource(Players.class)
                    .requireResource(Time.class);
    }

    private final UIElement timePlayedTimer;
    private final UIElement killsCounter;

    public UpdateHUDSystem(final UserInterface userInterface) {
        this.timePlayedTimer = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("time-played-timer"))
                                            .findFirst()
                                            .orElseThrow();
        this.killsCounter = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("score-kills"))
                                         .findFirst()
                                         .orElseThrow();
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val timeManager = world.getOrCreateResource(Time.class);
        val sessionStats = world.getOrCreateResource(SessionStats.class);
        val localPlayer = world.getOrCreateResource(Players.class).player;
        if (localPlayer != null) {
            val localPlayerAbilities = world.getEntityManager().getComponentOf(localPlayer, CharacterAbilities.class)
                                            .orElseThrow();
            val localPlayerKills = sessionStats.getKillsOf(localPlayerAbilities.damageSource);
            this.killsCounter.setProperty(UIProperty.TEXT, String.format("Kills: %02d",
                                                                         localPlayerKills));
        }

        val ticks = sessionStats.endTimeStamp - sessionStats.beginTimeStamp;
        val secondsRaw = ticks / (1000 / timeManager.getTimeStep());
        val hours = secondsRaw / 3600;
        val minutes = (secondsRaw - (hours * 3600)) / 60;
        val seconds = secondsRaw - (hours * 3600) - (minutes * 60);

        this.timePlayedTimer.setProperty(UIProperty.TEXT, String.format("%02d:%02d:%02d",
                                                                        hours, minutes, seconds));
    }
}
