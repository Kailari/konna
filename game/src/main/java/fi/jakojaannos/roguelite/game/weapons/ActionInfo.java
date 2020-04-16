package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;

public record ActionInfo(
        TimeManager timeManager,
        Entities entities,
        Transform shooterTransform,
        AttackAbility attackAbility
) {
}
