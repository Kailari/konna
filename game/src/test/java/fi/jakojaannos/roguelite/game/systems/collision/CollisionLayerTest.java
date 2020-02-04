package fi.jakojaannos.roguelite.game.systems.collision;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import fi.jakojaannos.roguelite.game.data.CollisionLayer;

import static org.junit.jupiter.api.Assertions.*;

class CollisionLayerTest {
    @Test
    void layerSetToCollideWithAnotherLayerTreatsItAsSolid() {
        assertTrue(CollisionLayer.OBSTACLE.isSolidTo(CollisionLayer.PLAYER));
    }

    @Test
    void layerNotSetToCollideWithAnotherLayerTreatsItAsNonSolid() {
        assertFalse(CollisionLayer.ENEMY.isSolidTo(CollisionLayer.PLAYER));
    }

    @Test
    void layerNotSetToOverlapAnotherLayerCanOverlapWithIt() {
        assertTrue(CollisionLayer.ENEMY.canOverlapWith(CollisionLayer.PLAYER_PROJECTILE));
    }

    @Test
    void layerNotSetToOverlapButNotCollideWithAnotherLayerTreatsItAsNonSolid() {
        assertFalse(CollisionLayer.ENEMY.isSolidTo(CollisionLayer.PLAYER_PROJECTILE));
    }

    @Test
    void layerNoneDoesNotCollideWithAnyOtherLayer() {
        assertAll(Arrays.stream(CollisionLayer.values())
                        .map(layer -> () -> assertFalse(CollisionLayer.NONE.isSolidTo(layer))));
    }

    @Test
    void layerNoneDoesNotOverlapWithAnyOtherLayer() {
        assertAll(Arrays.stream(CollisionLayer.values())
                        .map(layer -> () -> assertFalse(CollisionLayer.NONE.canOverlapWith(layer))));
    }
}
