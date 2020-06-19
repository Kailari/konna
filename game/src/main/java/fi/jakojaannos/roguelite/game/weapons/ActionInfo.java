package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.data.events.EventSender;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;

public record ActionInfo(
        TimeManager timeManager,
        Entities entities,
        Transform shooterTransform,
        AttackAbility attackAbility,
        EventSender<Object>events
) {
}
