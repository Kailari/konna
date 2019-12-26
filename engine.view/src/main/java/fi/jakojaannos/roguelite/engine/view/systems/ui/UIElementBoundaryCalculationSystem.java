package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.data.components.EngineUIComponentGroups;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.*;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.internal.UIRoot;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;
import lombok.val;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Iterates through entities, finding any with <code>BoundXXX</code>-components and bakes them into
 * {@link fi.jakojaannos.roguelite.engine.view.ui.ElementBoundaries ElementBoundaries}. Logs
 * warnings for invalid elements.
 * <p>
 * The basic idea is simple: The properties used to define coordinates of the component can be
 * divided to two groups. "Horizontal" group being "left", "right" and "width" and "Vertical" group
 * being "top", "bottom" and "height". Exactly two of each group must be defined in order to be able
 * to calculate the size.
 * <p>
 * In another words: if we know two of "min", "max" and "size", we can easily calculate the third.
 * If more than two are defined, there is a high risk of contradiction.
 */
public class UIElementBoundaryCalculationSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .tickAfter(UIHierarchySystem.class)
                    .withComponentFrom(EngineUIComponentGroups.ELEMENT_BOUND)
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

                    Optional<ProportionValue> leftValue, rightValue, widthValue;
                    leftValue = entityManager.getComponentOf(entity, BoundLeft.class).map(bound -> bound.value);
                    rightValue = entityManager.getComponentOf(entity, BoundRight.class).map(bound -> bound.value);
                    widthValue = entityManager.getComponentOf(entity, BoundWidth.class).map(bound -> bound.value);
                    if (leftValue.isPresent() && rightValue.isPresent()) {
                        bounds.minX = leftValue.get().getValue(context);
                        bounds.maxX = rightValue.get().getValue(context);
                        bounds.width = bounds.maxX - bounds.minX;

                        if (widthValue.isPresent()) {
                            throw new IllegalStateException("Width must not be defined if both Left and Right are defined!");
                        }
                    } else if (widthValue.isEmpty()) {
                        throw new IllegalStateException("You must define Width if Left and Right are both undefined!");
                    } else if (leftValue.isPresent()) {
                        bounds.width = widthValue.get().getValue(context);
                        bounds.minX = leftValue.get().getValue(context);
                        bounds.maxX = bounds.minX + bounds.width;
                    } else if (rightValue.isPresent()) {
                        bounds.width = widthValue.get().getValue(context);
                        bounds.maxX = rightValue.get().getValue(context);
                        bounds.minX = bounds.maxX - bounds.width;
                    } else {
                        throw new IllegalStateException("Exactly two of Left, Right and/or Width must be defined!");
                    }

                    Optional<ProportionValue> topValue, bottomValue, heightValue;
                    topValue = entityManager.getComponentOf(entity, BoundTop.class).map(bound -> bound.value);
                    bottomValue = entityManager.getComponentOf(entity, BoundBottom.class).map(bound -> bound.value);
                    heightValue = entityManager.getComponentOf(entity, BoundHeight.class).map(bound -> bound.value);
                    if (topValue.isPresent() && bottomValue.isPresent()) {
                        bounds.minY = topValue.get().getValue(context);
                        bounds.maxY = bottomValue.get().getValue(context);
                        bounds.height = bounds.maxY - bounds.minY;

                        if (heightValue.isPresent()) {
                            throw new IllegalStateException("Height must not be defined if both Top and Bottom are defined!");
                        }
                    } else if (heightValue.isEmpty()) {
                        throw new IllegalStateException("You must define Height if Top and Bottom are both undefined!");
                    } else if (topValue.isPresent()) {
                        bounds.height = heightValue.get().getValue(context);
                        bounds.minY = topValue.get().getValue(context);
                        bounds.maxY = bounds.minY + bounds.height;
                    } else if (bottomValue.isPresent()) {
                        bounds.height = heightValue.get().getValue(context);
                        bounds.maxY = bottomValue.get().getValue(context);
                        bounds.minY = bounds.maxY - bounds.height;
                    } else {
                        throw new IllegalStateException("Exactly two of Top, Bottom and/or Height must be defined!");
                    }

                    int xOffset = parentBounds.minX;
                    int yOffset = parentBounds.minY;
                    bounds.minX += xOffset;
                    bounds.maxX += xOffset;
                    bounds.minY += yOffset;
                    bounds.maxY += yOffset;

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
}
