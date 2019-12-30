package fi.jakojaannos.roguelite.engine.systems.ui;

import fi.jakojaannos.roguelite.engine.data.components.internal.ui.*;
import fi.jakojaannos.roguelite.engine.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.data.resources.internal.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Iterates through entities, finding any with <code>BoundXXX</code>-components and bakes them into
 * {@link ElementBoundaries ElementBoundaries}. Logs warnings for invalid elements.
 * <p>
 * The basic idea is simple: The properties used to define coordinates of the component can be
 * divided to two groups. "Horizontal" group being "left", "right" and "width" and "Vertical" group
 * being "top", "bottom" and "height". Exactly two of each group must be defined in order to be able
 * to calculate the size.
 * <p>
 * In another words: if we know two of "min", "max" and "size", we can easily calculate the third.
 * If more than two are defined, there is a high risk of contradiction.
 */
@Slf4j
public class UIElementBoundaryCalculationSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .tickAfter(UIHierarchySystem.class)
                    .requireResource(UIHierarchy.class)
                    .requireResource(UIRoot.class)
                    .withComponent(ElementBoundaries.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val hierarchy = world.getOrCreateResource(UIHierarchy.class);
        val uiRoot = world.getOrCreateResource(UIRoot.class);

        val entityManager = world.getEntityManager();
        val boundaryLookup = new HashMap<Entity, ElementBoundaries>();
        val fontSizeLookup = new HashMap<Entity, Integer>();
        entities.sorted(hierarchy::parentsFirst)
                .forEach(entity -> {
                    val parentBounds = hierarchy.getParentOf(entity)
                                                .map(boundaryLookup::get)
                                                .orElseGet(uiRoot::getBoundaries);

                    val bounds = entityManager.getComponentOf(entity, ElementBoundaries.class).orElseThrow();
                    bounds.minX = ElementBoundaries.INVALID_VALUE;
                    bounds.maxX = ElementBoundaries.INVALID_VALUE;
                    bounds.minY = ElementBoundaries.INVALID_VALUE;
                    bounds.maxY = ElementBoundaries.INVALID_VALUE;
                    bounds.width = ElementBoundaries.INVALID_VALUE;
                    bounds.height = ElementBoundaries.INVALID_VALUE;

                    val fontSize = entityManager.getComponentOf(entity, FontSize.class)
                                                .map(fs -> fs.value)
                                                .orElseGet(() -> hierarchy.getParentOf(entity)
                                                                          .map(fontSizeLookup::get)
                                                                          .orElseGet(uiRoot::getFontSize));
                    boundaryLookup.put(entity, bounds);
                    fontSizeLookup.put(entity, fontSize);
                    val context = new ProportionValue.Context(fontSize, parentBounds, bounds);

                    Optional<Integer> widthValue, heightValue;
                    widthValue = entityManager.getComponentOf(entity, BoundWidth.class).map(bound -> bound.value.getValue(context));
                    heightValue = entityManager.getComponentOf(entity, BoundHeight.class).map(bound -> bound.value.getValue(context));
                    widthValue.ifPresent(bounds::setWidth);
                    heightValue.ifPresent(bounds::setHeight);

                    Optional<Integer> leftValue = entityManager.getComponentOf(entity, BoundLeft.class)
                                                               .map(bound -> bound.value.getValue(context));
                    Optional<Integer> rightValue = entityManager.getComponentOf(entity, BoundRight.class)
                                                                .map(bound -> bound.value.getValue(context));
                    if (leftValue.isPresent() && rightValue.isPresent() && widthValue.isPresent()) {
                        LOG.warn("Width must not be defined if both Left and Right are defined! Removing the width component...");
                        entityManager.removeComponentFrom(entity, BoundWidth.class);
                    }

                    bounds.minX = leftValue.map(value -> parentBounds.minX + value)
                                           .or(getAppliedValueIfBothPresent(rightValue.map(value -> parentBounds.maxX - value),
                                                                            widthValue,
                                                                            (right, width) -> right - width))
                                           .orElse(parentBounds.minX);
                    bounds.maxX = rightValue.map(value -> parentBounds.maxX - value)
                                            .or(getAppliedValueIfBothPresent(leftValue.map(value -> parentBounds.minX + value),
                                                                             widthValue,
                                                                             Integer::sum))
                                            .orElse(parentBounds.maxX);
                    bounds.width = bounds.maxX - bounds.minX;

                    Optional<Integer> topValue = entityManager.getComponentOf(entity, BoundTop.class)
                                                              .map(bound -> bound.value.getValue(context));
                    Optional<Integer> bottomValue = entityManager.getComponentOf(entity, BoundBottom.class)
                                                                 .map(bound -> bound.value.getValue(context));
                    if (topValue.isPresent() && bottomValue.isPresent() && heightValue.isPresent()) {
                        LOG.warn("Height must not be defined if both Top and Bottom are defined! Removing the height component...");
                        entityManager.removeComponentFrom(entity, BoundHeight.class);
                    }

                    bounds.minY = topValue.map(value -> parentBounds.minY + value)
                                          .or(getAppliedValueIfBothPresent(bottomValue.map(value -> parentBounds.maxY - value),
                                                                           heightValue,
                                                                           (bottom, height) -> bottom - height))
                                          .orElse(parentBounds.minY);
                    bounds.maxY = bottomValue.map(value -> parentBounds.maxY - value)
                                             .or(getAppliedValueIfBothPresent(topValue.map(value -> parentBounds.minY + value),
                                                                              heightValue,
                                                                              Integer::sum))
                                             .orElse(parentBounds.maxY);
                    bounds.height = bounds.maxY - bounds.minY;

                    entityManager.getComponentOf(entity, BoundAnchorX.class)
                                 .map(boundAnchorX -> boundAnchorX.value)
                                 .ifPresent(anchorX -> {
                                     val anchorOffset = anchorX.getValue(context);
                                     bounds.minX += anchorOffset;
                                     bounds.maxX += anchorOffset;
                                 });
                    entityManager.getComponentOf(entity, BoundAnchorY.class)
                                 .map(boundAnchorY -> boundAnchorY.value)
                                 .ifPresent(anchorY -> {
                                     val anchorOffset = anchorY.getValue(context);
                                     bounds.minY += anchorOffset;
                                     bounds.maxY += anchorOffset;
                                 });
                });
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T, R> Optional<R> applyIfBothPresent(
            final Optional<T> a,
            final Optional<T> b,
            final BiFunction<T, T, R> operator
    ) {
        return (a.isPresent() && b.isPresent())
                ? Optional.ofNullable(operator.apply(a.get(), b.get()))
                : Optional.empty();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T, R> Supplier<Optional<R>> getAppliedValueIfBothPresent(
            final Optional<T> a,
            final Optional<T> b,
            final BiFunction<T, T, R> operator
    ) {
        return () -> applyIfBothPresent(a, b, operator);
    }
}
