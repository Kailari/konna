package fi.jakojaannos.roguelite.engine.ecs.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SystemDispatcherTest {
    class SystemBase implements ECSSystem {
        @Override
        public void declareRequirements(RequirementsBuilder requirements) {
        }

        @Override
        public void tick(
                Stream<Entity> entities, World world, double delta
        ) {
            synchronized (callOrderLock) {
                callOrder.add(this);
            }
        }
    }

    class SystemA extends SystemBase {
    }

    class SystemB extends SystemBase {
    }

    class SystemC extends SystemBase {
    }

    class SystemD extends SystemBase {
    }

    static class ComponentA implements Component {
    }

    static class ComponentB implements Component {
    }

    static class ComponentC implements Component {
    }

    static class ComponentD implements Component {
    }

    private final Object callOrderLock = new Object();
    private World world;
    private EntityManager entityManager;
    private List<ECSSystem> callOrder;
    private Entity entityA, entityB, entityC, entityD;

    private ComponentGroup componentGroup = new ComponentGroup() {
        @Override
        public int getId() {
            return 0;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public Collection<Class<? extends Component>> getComponentTypes() {
            return List.of(ComponentD.class, ComponentC.class);
        }
    };

    @BeforeEach
    void beforeEach() {
        callOrder = new ArrayList<>();
        world = mock(World.class);
        entityManager = EntityManager.createNew(256, 32);
        when(world.getEntityManager()).thenReturn(entityManager);

        entityManager.registerComponentGroup(componentGroup);
        entityA = entityManager.createEntity();
        entityManager.addComponentTo(entityA, new ComponentA());
        entityB = entityManager.createEntity();
        entityManager.addComponentTo(entityB, new ComponentA());
        entityManager.addComponentTo(entityB, new ComponentB());
        entityC = entityManager.createEntity();
        entityManager.addComponentTo(entityC, new ComponentA());
        entityManager.addComponentTo(entityC, new ComponentB());
        entityManager.addComponentTo(entityC, new ComponentC());
        entityD = entityManager.createEntity();
        entityManager.addComponentTo(entityD, new ComponentA());
        entityManager.addComponentTo(entityD, new ComponentB());
        entityManager.addComponentTo(entityD, new ComponentC());
        entityManager.addComponentTo(entityD, new ComponentD());
        entityManager.applyModifications();
    }

    @Test
    void systemGroupDependingOnItselfThrows() {
        assertThrows(IllegalStateException.class, () -> {
            SystemGroup systemGroup = () -> "invalid";

            SystemDispatcher.builder()
                            .withGroup(systemGroup)
                            .addGroupDependency(systemGroup, systemGroup)
                            .build();
        });
    }

    @Test
    void systemDependingOnItselfThrows() {
        assertThrows(IllegalStateException.class, () -> {
            ECSSystem invalid = new SystemA() {
                @Override
                public void declareRequirements(RequirementsBuilder requirements) {
                    requirements.tickAfter(getClass());
                }
            };

            SystemDispatcher.builder()
                            .withSystem(invalid)
                            .build();
        });
    }

    @Test
    void systemDependingOnGroupItBelongsToThrows() {
        assertThrows(IllegalStateException.class, () -> {
            SystemGroup systemGroup = () -> "invalid";

            ECSSystem invalid = new SystemA() {
                @Override
                public void declareRequirements(RequirementsBuilder requirements) {
                    requirements.addToGroup(systemGroup)
                                .tickAfter(systemGroup);
                }
            };

            SystemDispatcher.builder()
                            .withGroup(systemGroup)
                            .withSystem(invalid)
                            .build();
        });
    }

    @Test
    void groupDependingOnSystemBelongingToItThrows() {
        assertThrows(IllegalStateException.class, () -> {
            SystemGroup systemGroup = () -> "invalid";

            ECSSystem invalid = new SystemA() {
                @Override
                public void declareRequirements(RequirementsBuilder requirements) {
                    requirements.addToGroup(systemGroup)
                                .tickBefore(systemGroup);
                }
            };

            SystemDispatcher.builder()
                            .withGroup(systemGroup)
                            .withSystem(invalid)
                            .build();
        });
    }

    @Test
    void allRegisteredSystemsAreDispatched() {
        ECSSystem systemA, systemB, systemC, systemD;
        SystemDispatcher dispatcher = SystemDispatcher
                .builder()
                .withSystem(systemA = mock(SystemA.class))
                .withSystem(systemB = mock(SystemB.class))
                .withSystem(systemC = mock(SystemC.class))
                .withSystem(systemD = mock(SystemD.class))
                .build();

        dispatcher.dispatch(world);

        verify(systemA).tick(any(), eq(world));
        verify(systemB).tick(any(), eq(world));
        verify(systemC).tick(any(), eq(world));
        verify(systemD).tick(any(), eq(world));
    }

    @Test
    void systemMarkedAsTickAfterAnotherSystemTicksAfter() {
        ECSSystem systemA = new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.tickAfter(SystemB.class);
            }
        };

        ECSSystem systemB = new SystemB();
        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .withSystem(systemB)
                                                      .build();
        dispatcher.dispatch(world);

        assertEquals(systemB, callOrder.get(0));
        assertEquals(systemA, callOrder.get(1));
    }

    @Test
    void systemMarkedAsTickBeforeAnotherSystemTicksBefore() {
        ECSSystem systemA = new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.tickBefore(SystemB.class);
            }
        };

        ECSSystem systemB = new SystemB();
        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemB)
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        assertEquals(systemA, callOrder.get(0));
        assertEquals(systemB, callOrder.get(1));
    }

    @Test
    void systemMarkedAsTickAfterGroupTicksAfter() {
        SystemGroup group = () -> "test";

        ECSSystem systemA = new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.tickAfter(group);
            }
        };

        ECSSystem systemB = new SystemB() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.addToGroup(group);
            }
        };
        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withGroup(group)
                                                      .withSystem(systemB)
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        assertEquals(systemB, callOrder.get(0));
        assertEquals(systemA, callOrder.get(1));
    }

    @Test
    void systemMarkedAsTickBeforeGroupTicksBefore() {
        SystemGroup group = () -> "test";

        ECSSystem systemA = new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.tickBefore(group);
            }
        };

        ECSSystem systemB = new SystemB() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.addToGroup(group);
            }
        };
        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withGroup(group)
                                                      .withSystem(systemB)
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        //assertEquals(systemA, callOrder.get(0));
        //assertEquals(systemB, callOrder.get(1));
    }

    @Test
    void dispatcherRespectsHardcodedGroupDependencies() {
        SystemGroup groupA = () -> "testA";
        SystemGroup groupB = () -> "testB";

        ECSSystem systemA = new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.addToGroup(groupA);
            }
        };

        ECSSystem systemB = new SystemB() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.addToGroup(groupB);
            }
        };
        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withGroups(groupA, groupB)
                                                      .addGroupDependency(groupA, groupB)
                                                      .withSystem(systemB)
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        assertEquals(systemB, callOrder.get(0));
        assertEquals(systemA, callOrder.get(1));
    }

    @Test
    void systemReceivesEntities() {
        ECSSystem systemA = spy(new SystemA());

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.count() > 0),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemWithRequiredComponentsReceivesEntities() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponent(ComponentC.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.count() > 0),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemReceivesEntitiesWithAllRequiredComponents() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponent(ComponentA.class)
                            .withComponent(ComponentB.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.allMatch(
                        entity -> entityManager.hasComponent(entity, ComponentA.class)
                                && entityManager.hasComponent(entity, ComponentB.class))),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemWithRequiredGroupsReceivesEntities() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponentFrom(componentGroup);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.count() > 0),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemReceivesEntitiesWithComponentsFromRequiredGroup() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponentFrom(componentGroup);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.allMatch(
                        entity -> entityManager.hasComponent(entity, ComponentC.class)
                                || entityManager.hasComponent(entity, ComponentD.class))),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemWithRequiredGroupsAndComponentsReceivesEntities() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponentFrom(componentGroup)
                            .withComponent(ComponentB.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.count() > 0),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemReceivesEntitiesWithAllRequiredGroupsAndComponents() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponentFrom(componentGroup)
                            .withComponent(ComponentB.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.allMatch(
                        entity -> (entityManager.hasComponent(entity, ComponentC.class)
                                || entityManager.hasComponent(entity, ComponentD.class))
                                && entityManager.hasComponent(entity, ComponentB.class))),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemReceivesEntitiesWithAllRequiredComponentsAndSomeOtherComponents() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponentFrom(componentGroup);
                //.withComponent(ComponentB.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.anyMatch(
                        entity -> (entityManager.hasComponent(entity, ComponentC.class)
                                || entityManager.hasComponent(entity, ComponentD.class))
                                && entityManager.hasComponent(entity, ComponentB.class)
                                && entityManager.hasComponent(entity, ComponentA.class))),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemDoesNotReceiveEntitiesWithExcludedComponents() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withoutComponent(ComponentD.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.count() == 3),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemDoesNotReceiveEntitiesWithComponentsFromExcludedGroups() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withoutComponent(ComponentD.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.noneMatch(
                        entity -> entityManager.hasComponent(entity, ComponentD.class))),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemReceivesEntitiesWithAllRequiredComponentsAndNoExcludedComponents() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withoutComponent(ComponentD.class)
                            .withComponent(ComponentB.class);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.allMatch(
                        entity -> entityManager.hasComponent(entity, ComponentB.class)
                                && !entityManager.hasComponent(entity, ComponentD.class))),
                      any(),
                      eq(0.02));
    }

    @Test
    void systemReceivesEntitiesWithAllRequiredComponentsAndNoExcludedGroups() {
        ECSSystem systemA = spy(new SystemA() {
            @Override
            public void declareRequirements(RequirementsBuilder requirements) {
                requirements.withComponent(ComponentB.class)
                            .withoutComponentsFrom(componentGroup);
            }
        });

        SystemDispatcher dispatcher = SystemDispatcher.builder()
                                                      .withSystem(systemA)
                                                      .build();
        dispatcher.dispatch(world);

        verify(systemA, times(1))
                .tick(streamThat(entities -> entities.allMatch(
                        entity -> entityManager.hasComponent(entity, ComponentB.class)
                                && !entityManager.hasComponent(entity, ComponentD.class)
                                && !entityManager.hasComponent(entity, ComponentC.class))),
                      any(),
                      eq(0.02));
    }

    private static <T> Stream<T> streamThat(ArgumentMatcher<Stream<T>> matcher) {
        return argThat(new SafeStreamMatcher<>(matcher));
    }

    private static class SafeStreamMatcher<T> implements ArgumentMatcher<Stream<T>> {
        private final ArgumentMatcher<Stream<T>> matcher;
        private List<T> input;

        private SafeStreamMatcher(ArgumentMatcher<Stream<T>> matcher) {
            this.matcher = matcher;
        }

        @Override
        public boolean matches(Stream<T> item) {
            // This is to protect against JUnit calling this more than once
            input = input == null ? item.collect(Collectors.toList()) : input;
            return matcher.matches(input.stream());
        }
    }
}
