package fi.jakojaannos.roguelite.game.view.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.EnableOn;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.events.HordeStartEvent;
import fi.jakojaannos.roguelite.game.data.resources.Horde;

import static fi.jakojaannos.roguelite.engine.view.ui.query.UIMatchers.withName;

public class UpdateHordeMessageSystem implements EcsSystem<UpdateHordeMessageSystem.Resources, EcsSystem.NoEntities, UpdateHordeMessageSystem.EventData> {
    private final UIElement hordeMessage;
    private final long messageDuration;

    public UpdateHordeMessageSystem(final UserInterface userInterface, final long messageDuration) {
        this.hordeMessage = userInterface.findElements(withName("horde-message"))
                                         .findFirst()
                                         .orElseThrow();

        this.hordeMessage.setProperty(UIProperty.HIDDEN, true);
        this.messageDuration = messageDuration;
    }


    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final EventData eventData
    ) {
        // The event is present only on the first tick of the horde
        if (eventData.hordeStart != null) {
            this.hordeMessage.setProperty(UIProperty.TEXT,
                                          "Wave #" + resources.horde.hordeIndex + " incoming");
            this.hordeMessage.setProperty(UIProperty.HIDDEN, false);
        }

        final var currentTime = resources.timeManager.getCurrentGameTime();
        final var elapsed = currentTime - resources.horde.startTimestamp;
        if (elapsed > this.messageDuration) {
            this.hordeMessage.setProperty(UIProperty.HIDDEN, true);
        }
    }

    public static record Resources(
            TimeManager timeManager,
            Horde horde
    ) {}

    public static record EventData(
            @EnableOn HordeStartEvent hordeStart
    ) {}
}
