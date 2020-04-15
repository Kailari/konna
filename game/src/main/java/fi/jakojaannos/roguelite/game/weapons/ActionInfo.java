package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;

public record ActionInfo(
        TimeManager timeManager,
        EntityManager entityManager,
        Transform shooterTransform,
        AttackAbility attackAbility
) {
}
