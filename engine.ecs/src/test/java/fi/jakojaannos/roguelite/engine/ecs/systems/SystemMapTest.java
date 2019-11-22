package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockECSSystem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SystemMapTest {
    @ParameterizedTest
    @CsvSource({"valid,,valid", ",valid,valid"})
    void putThrowsIfAnyOfParametersAreNull(String name, String system, String dependency) {
        SystemMap map = new SystemMap();
        map.put("valid", new MockECSSystem());

        assertThrows(AssertionError.class,
                     () -> map.put(
                             name,
                             system == null ? null : new MockECSSystem(),
                             dependency
                     ));
    }

    @Test
    void putThrowsIfDependencyIsNotRegistered() {
        SystemMap map = new SystemMap();
        assertThrows(IllegalStateException.class,
                     () -> map.put("valid",
                                   new MockECSSystem(),
                                   "invalid"));
    }

    @Test
    void putSucceedsIfAllParametersAreValid_noDependencies() {
        SystemMap map = new SystemMap();

        assertDoesNotThrow(() -> map.put("valid",
                                         new MockECSSystem()));
    }

    @Test
    void putSucceedsIfAllParametersAreValid_singleDependency() {
        SystemMap map = new SystemMap();
        map.put("valid_dep_1", new MockECSSystem());

        assertDoesNotThrow(() -> map.put("valid",
                                         new MockECSSystem(),
                                         "valid_dep_1"));
    }

    @Test
    void putSucceedsIfAllParametersAreValid_manyDependencies() {
        SystemMap map = new SystemMap();
        map.put("valid_dep_1", new MockECSSystem());
        map.put("valid_dep_2", new MockECSSystem());
        map.put("valid_dep_3", new MockECSSystem());
        map.put("valid_dep_4", new MockECSSystem());

        assertDoesNotThrow(() -> map.put("valid",
                                         new MockECSSystem(),
                                         "valid_dep_1",
                                         "valid_dep_2",
                                         "valid_dep_3",
                                         "valid_dep_4"
        ));
    }

    @Test
    void nonPrioritizedStreamGetsAllRegisteredSystems_noDependencies() {
        SystemMap map = new SystemMap();
        List<ECSSystem> systems = List.of(
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem()
        );
        for (int i = 0; i < systems.size(); ++i) {
            map.put("valid_" + i, systems.get(i));
        }

        assertTrue(map.nonPrioritizedStream().allMatch(systems::contains));
    }

    @Test
    void nonPrioritizedStreamGetsAllRegisteredSystems_simpleDependencies() {
        SystemMap map = new SystemMap();
        List<ECSSystem> systems = List.of(
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem()
        );
        map.put("valid_0", systems.get(0));
        for (int i = 1; i < systems.size(); ++i) {
            map.put("valid_" + i, systems.get(i), "valid_" + (i - 1));
        }

        assertTrue(map.nonPrioritizedStream().allMatch(systems::contains));
    }

    @Test
    void forEachPrioritizedThrowsIfConsumerIsNull() {
        SystemMap map = new SystemMap();
        assertThrows(AssertionError.class, () -> map.forEachPrioritized(null));
    }

    @Test
    void forEachPrioritizedSucceedsWhenMapIsEmpty() {
        SystemMap map = new SystemMap();
        assertDoesNotThrow(() -> map.forEachPrioritized(system -> {
        }));
    }

    @Test
    void forEachPrioritizedIteratesInExpectedOrder_simpleDependencies() {
        SystemMap map = new SystemMap();
        List<ECSSystem> systems = new ArrayList<>(List.of(
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem(),
                new MockECSSystem())
        );
        map.put("valid_0", systems.get(0));
        for (int i = 1; i < systems.size(); ++i) {
            map.put("valid_" + i, systems.get(i), "valid_" + (i - 1));
        }

        map.forEachPrioritized(system -> assertEquals(systems.remove(0), system));
    }

    @Test
    void forEachPrioritizedIteratesInExpectedOrder_complexDependencies() {
        SystemMap map = new SystemMap();
        Map<String, ECSSystem> systems = Map.ofEntries(
                Map.entry("valid_0", new MockECSSystem()),
                Map.entry("valid_1", new MockECSSystem()),
                Map.entry("valid_2", new MockECSSystem()),
                Map.entry("valid_3", new MockECSSystem()),
                Map.entry("valid_4", new MockECSSystem()),
                Map.entry("valid_5", new MockECSSystem()),
                Map.entry("valid_6", new MockECSSystem()),
                Map.entry("valid_7", new MockECSSystem()),
                Map.entry("valid_8", new MockECSSystem()),
                Map.entry("valid_9", new MockECSSystem()),
                Map.entry("valid_10", new MockECSSystem())
        );

        //  0
        // / \
        // 1 2 3 4
        // |   \ /
        // 5    6
        // \    /
        //  7  8
        //  \  /
        //   9
        map.put("valid_10", systems.get("valid_10"));
        map.put("valid_0", systems.get("valid_0"));
        map.put("valid_1", systems.get("valid_1"), "valid_0");
        map.put("valid_2", systems.get("valid_2"), "valid_0");
        map.put("valid_3", systems.get("valid_3"));
        map.put("valid_4", systems.get("valid_4"));
        map.put("valid_5", systems.get("valid_5"), "valid_1");
        map.put("valid_6", systems.get("valid_6"), "valid_3", "valid_4");
        map.put("valid_7", systems.get("valid_7"), "valid_5");
        map.put("valid_8", systems.get("valid_8"), "valid_6");
        map.put("valid_9", systems.get("valid_9"), "valid_7", "valid_8");

        List<ECSSystem> processed = new ArrayList<>();
        map.forEachPrioritized(system -> {
            // Ugly hack for finding the name of the system
            String name = systems.entrySet()
                                 .stream()
                                 .filter(e -> e.getValue().equals(system))
                                 .map(Map.Entry::getKey)
                                 .findFirst()
                                 .orElseThrow();

            // Asserts that all dependencies have been met already
            assertTrue(map.getDependencies(name).allMatch(processed::contains));
            processed.add(system);
        });
    }
}
