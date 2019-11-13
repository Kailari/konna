package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DispatcherBuilderTest {
    @Test
    void buildReturnsValidSystemDispatcher_noSystems() {
        Assertions.assertDoesNotThrow(() -> new DispatcherBuilder<>().build());
    }

    @Test
    void buildReturnsValidSystemDispatcher_mockSystems() {
        Assertions.assertDoesNotThrow(() -> new DispatcherBuilder<>().withSystem("mock_1", new MockECSSystem<>())
                                                                     .withSystem("mock_2", new MockECSSystem<>())
                                                                     .withSystem("mock_3", new MockECSSystem<>(), "mock_2")
                                                                     .withSystem("mock_4", new MockECSSystem<>(), "mock_2")
                                                                     .build());
    }
}
