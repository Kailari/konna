package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.resources.SessionStats;
import lombok.val;

import java.util.stream.Stream;

public class UpdateHUDSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .tickBefore(UserInterfaceRenderingSystem.class)
                    .requireResource(Time.class);
    }

    private final UIElement timePlayedTimer;

    public UpdateHUDSystem(final UserInterface userInterface) {
        this.timePlayedTimer = userInterface.findElementsWithMatchingProperty(UIProperty.NAME, name -> name.equals("time-played-timer"))
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

        val ticks = sessionStats.endTimeStamp - sessionStats.beginTimeStamp;
        val secondsRaw = ticks / (1000 / timeManager.getTimeStep());
        val hours = secondsRaw / 3600;
        val minutes = (secondsRaw - (hours * 3600)) / 60;
        val seconds = secondsRaw - (hours * 3600) - (minutes * 60);

        this.timePlayedTimer.setProperty(UIProperty.TEXT, String.format("%02d:%02d:%02d",
                                                                        hours, minutes, seconds));
    }
}
