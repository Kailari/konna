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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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

                    val maybeLeft = entityManager.getComponentOf(entity, BoundLeft.class).map(ProportionalValueComponent::getValue);
                    val maybeRight = entityManager.getComponentOf(entity, BoundRight.class).map(ProportionalValueComponent::getValue);
                    val maybeWidth = entityManager.getComponentOf(entity, BoundWidth.class)
                                                  .filter(ignored -> maybeLeft.isEmpty() || maybeRight.isEmpty())
                                                  .map(ProportionalValueComponent::getValue);

                    val maybeTop = entityManager.getComponentOf(entity, BoundTop.class).map(ProportionalValueComponent::getValue);
                    val maybeBottom = entityManager.getComponentOf(entity, BoundBottom.class).map(ProportionalValueComponent::getValue);
                    val maybeHeight = entityManager.getComponentOf(entity, BoundHeight.class)
                                                   .filter(ignored -> maybeTop.isEmpty() || maybeBottom.isEmpty())
                                                   .map(ProportionalValueComponent::getValue);

                    if (maybeWidth.isPresent() && maybeHeight.isPresent()) {
                        val widthProportion = maybeWidth.get();
                        val heightProportion = maybeHeight.get();
                        if (widthProportion instanceof ProportionValue.PercentOfSelf && heightProportion instanceof ProportionValue.PercentOfSelf) {
                            val selfProportionalWidth = (ProportionValue.PercentOfSelf) widthProportion;
                            val selfProportionalHeight = (ProportionValue.PercentOfSelf) heightProportion;
                            if (selfProportionalHeight.isHorizontal() && !selfProportionalWidth.isHorizontal()) {
                                throw new IllegalStateException("Width and height cannot cyclically depend on each other!");
                            }
                        }
                    }

                    ((maybeLeft.isPresent() && maybeRight.isPresent())
                            ? Stream.of(maybeWidth, maybeLeft, maybeRight)
                            : Stream.of(maybeWidth))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(proportion -> ProportionValue.PercentOfSelf.class.isAssignableFrom(proportion.getClass()))
                            .map(ProportionValue.PercentOfSelf.class::cast)
                            .filter(ProportionValue.PercentOfSelf::isHorizontal)
                            .findAny()
                            .ifPresent(invalidProportion -> {
                                throw new IllegalStateException("Width-related properties cannot be proportional to width!");
                            });

                    ((maybeTop.isPresent() && maybeBottom.isPresent())
                            ? Stream.of(maybeHeight, maybeTop, maybeBottom)
                            : Stream.of(maybeHeight))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .filter(proportion -> ProportionValue.PercentOfSelf.class.isAssignableFrom(proportion.getClass()))
                            .map(ProportionValue.PercentOfSelf.class::cast)
                            .filter(proportion -> !proportion.isHorizontal())
                            .findAny()
                            .ifPresent(invalidProportion -> {
                                throw new IllegalStateException("Height-related properties cannot be proportional to height!");
                            });


                    LazyComputableWithFallback<Integer> computedLeft = new LazyComputableWithFallback<>(bounds::setMinX);
                    LazyComputableWithFallback<Integer> computedRight = new LazyComputableWithFallback<>(bounds::setMaxX);
                    LazyComputableWithFallback<Integer> computedWidth = new LazyComputableWithFallback<>(bounds::setWidth);

                    LazyComputableWithFallback<Integer> computedTop = new LazyComputableWithFallback<>(bounds::setMinY);
                    LazyComputableWithFallback<Integer> computedBottom = new LazyComputableWithFallback<>(bounds::setMaxY);
                    LazyComputableWithFallback<Integer> computedHeight = new LazyComputableWithFallback<>(bounds::setHeight);

                    // Width/Height
                    if ((maybeRight.isEmpty() || maybeLeft.isEmpty()) && maybeWidth.isEmpty() && !(maybeLeft.isEmpty() && maybeRight.isEmpty())) {
                        if (maybeRight.isEmpty()) {
                            computedRight.setValue(parentBounds.maxX);
                        } else if (maybeLeft.isEmpty()) {
                            computedLeft.setValue(parentBounds.minX);
                        }
                    }
                    if ((maybeTop.isEmpty() || maybeBottom.isEmpty()) && maybeHeight.isEmpty() && !(maybeTop.isEmpty() && maybeBottom.isEmpty())) {
                        if (maybeTop.isEmpty()) {
                            computedTop.setValue(parentBounds.minY);
                        } else if (maybeBottom.isEmpty()) {
                            computedBottom.setValue(parentBounds.maxY);
                        }
                    }

                    computedWidth.addSupplier(() -> maybeWidth.flatMap(proportion -> computeWidth(proportion, computedHeight, context)));
                    computedWidth.addSupplier(() -> applyIfBothArePresent(computedRight.tryGet(),
                                                                          computedLeft.tryGet(),
                                                                          (right, left) -> right - left));
                    computedWidth.addFallback(() -> parentBounds.width);

                    computedHeight.addSupplier(() -> maybeHeight.flatMap(proportion -> {
                        if (proportion instanceof ProportionValue.PercentOfSelf) {
                            if (!((ProportionValue.PercentOfSelf) proportion).isHorizontal()
                                    || computedWidth.isComputing()) {
                                return Optional.<Integer>empty();
                            } else {
                                computedWidth.compute();
                            }
                        }

                        return Optional.of(proportion.getValue(context));
                    }));
                    computedHeight.addSupplier(() -> applyIfBothArePresent(computedBottom.tryGet(),
                                                                           computedTop.tryGet(),
                                                                           (bottom, top) -> bottom - top));
                    computedHeight.addFallback(() -> parentBounds.height);

                    // Left/Right
                    computedLeft.addSupplier(() -> maybeLeft.flatMap(proportion -> {
                        if (ensureDependenciesAreReady(proportion, computedWidth, computedHeight)) {
                            return Optional.<Integer>empty();
                        }

                        return Optional.of(parentBounds.minX + proportion.getValue(context));
                    }));
                    computedLeft.addSupplier(() -> applyIfBothArePresent(computedRight.tryGet(),
                                                                         computedWidth.tryGet(),
                                                                         (right, width) -> right - width));
                    computedLeft.addFallback(parentBounds::getMinX);

                    computedRight.addSupplier(() -> maybeRight.flatMap(proportion -> {
                        if (ensureDependenciesAreReady(proportion, computedWidth, computedHeight)) {
                            return Optional.<Integer>empty();
                        }

                        return Optional.of(parentBounds.maxX - proportion.getValue(context));
                    }));
                    computedRight.addSupplier(() -> applyIfBothArePresent(computedLeft.tryGet(),
                                                                          computedWidth.tryGet(),
                                                                          (left, width) -> left + width));
                    computedRight.addFallback(parentBounds::getMaxX);

                    // Top/Bottom
                    computedTop.addSupplier(() -> maybeTop.flatMap(proportion -> {
                        if (ensureDependenciesAreReady(proportion, computedWidth, computedHeight)) {
                            return Optional.<Integer>empty();
                        }

                        return Optional.of(parentBounds.minY + proportion.getValue(context));
                    }));
                    computedTop.addSupplier(() -> applyIfBothArePresent(computedBottom.tryGet(),
                                                                        computedHeight.tryGet(),
                                                                        (bottom, height) -> bottom - height));
                    computedTop.addFallback(parentBounds::getMinY);

                    computedBottom.addSupplier(() -> maybeBottom.flatMap(proportion -> {
                        if (ensureDependenciesAreReady(proportion, computedWidth, computedHeight)) {
                            return Optional.<Integer>empty();
                        }

                        return Optional.of(parentBounds.maxY - proportion.getValue(context));
                    }));
                    computedBottom.addSupplier(() -> applyIfBothArePresent(computedTop.tryGet(),
                                                                           computedHeight.tryGet(),
                                                                           (top, height) -> top + height));
                    computedBottom.addFallback(parentBounds::getMaxY);

                    bounds.minX = computedLeft.get();
                    bounds.maxX = computedRight.get();
                    bounds.minY = computedTop.get();
                    bounds.maxY = computedBottom.get();
                    bounds.width = computedWidth.get();
                    bounds.height = computedHeight.get();

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

    public Optional<Integer> computeWidth(
            final ProportionValue proportion,
            final LazyComputableWithFallback<Integer> computedHeight,
            final ProportionValue.Context context
    ) {
        if (proportion instanceof ProportionValue.PercentOfSelf) {
            if (((ProportionValue.PercentOfSelf) proportion).isHorizontal() || computedHeight.isComputing()) {
                return Optional.<Integer>empty();
            } else {
                computedHeight.compute();
            }
        }

        return Optional.of(proportion.getValue(context));
    }

    public boolean ensureDependenciesAreReady(
            final ProportionValue proportion,
            final LazyComputableWithFallback<Integer> computedWidth,
            final LazyComputableWithFallback<Integer> computedHeight
    ) {
        if (proportion instanceof ProportionValue.PercentOfSelf) {
            if (((ProportionValue.PercentOfSelf) proportion).isHorizontal()) {
                if (computedWidth.isComputing()) {
                    return true;
                } else {
                    computedWidth.compute();
                }
            } else {
                if (computedHeight.isComputing()) {
                    return true;
                } else {
                    computedHeight.compute();
                }
            }
        }
        return false;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static <T, R> Optional<R> applyIfBothArePresent(
            final Optional<T> a,
            final Optional<T> b,
            final BiFunction<T, T, R> operator
    ) {
        return (a.isPresent() && b.isPresent())
                ? Optional.ofNullable(operator.apply(a.get(), b.get()))
                : Optional.empty();
    }

    @RequiredArgsConstructor
    private static class LazyComputableWithFallback<T> {
        @Getter private boolean computing = false;
        @Setter @Nullable private T value;
        private final Consumer<T> valueSetter;
        private List<Supplier<Optional<T>>> suppliers = new ArrayList<>();

        public void addFallback(final Supplier<T> supplier) {
            addSupplier(() -> Optional.of(supplier.get()));
        }

        public void addSupplier(final Supplier<Optional<T>> supplier) {
            this.suppliers.add(supplier);
        }

        public Optional<T> tryGet() {
            if (this.computing) {
                return Optional.empty();
            }

            compute();
            return Optional.ofNullable(this.value);
        }

        public T get() {
            if (this.computing) {
                throw new IllegalStateException("Tried to compute value of a Lazy wrapper while computation was already in progress! Check for cyclic dependencies!");
            }

            compute();
            if (this.value == null) {
                throw new IllegalStateException("Could not compute the value!");
            }
            return this.value;
        }

        public void compute() {
            if (this.value != null) {
                return;
            }

            this.computing = true;
            for (val supplier : this.suppliers) {
                val computedValue = supplier.get();
                if (computedValue.isPresent()) {
                    this.value = computedValue.get();
                    this.valueSetter.accept(this.value);
                    break;
                }
            }
            this.computing = false;
        }
    }
}
