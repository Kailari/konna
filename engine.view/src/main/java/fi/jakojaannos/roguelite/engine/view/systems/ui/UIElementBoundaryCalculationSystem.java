package fi.jakojaannos.roguelite.engine.view.systems.ui;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.OptionalUtil;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.*;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIHierarchy;
import fi.jakojaannos.roguelite.engine.view.data.resources.ui.UIRoot;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

/**
 * Iterates through entities, finding any with <code>BoundXXX</code>-components and bakes them into {@link
 * ElementBoundaries ElementBoundaries}. Logs warnings for invalid elements.
 * <p>
 * The basic idea is simple: The properties used to define coordinates of the component can be divided to two groups.
 * "Horizontal" group being "left", "right" and "width" and "Vertical" group being "top", "bottom" and "height". Exactly
 * two of each group must be defined in order to be able to calculate the size.
 * <p>
 * In another words: if we know two of "min", "max" and "size", we can easily calculate the third. If more than two are
 * defined, there is a high risk of contradiction.
 */
public class UIElementBoundaryCalculationSystem implements ECSSystem {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static Supplier<Integer> createBoundSupplier(
            final Optional<ProportionValue> maybeProportion,
            final Lazy<Integer> lazyWidth,
            final Lazy<Integer> lazyHeight,
            final ProportionValue.Context context,
            final int fallbackValue,
            final BinaryOperator<Integer> valueProcessor,
            final Supplier<Optional<Integer>> computeSupplier
    ) {
        return () -> maybeProportion.flatMap(proportion -> computeBound(proportion,
                                                                        lazyWidth,
                                                                        lazyHeight,
                                                                        context,
                                                                        value -> valueProcessor.apply(fallbackValue,
                                                                                                      value)))
                                    .or(computeSupplier)
                                    .orElse(fallbackValue);
    }

    private static Optional<Integer> computeBound(
            final ProportionValue proportion,
            final Lazy<Integer> computedWidth,
            final Lazy<Integer> computedHeight,
            final ProportionValue.Context context,
            final Function<Integer, Integer> processor
    ) {
        if (!tryComputeDependencies(proportion, computedWidth, computedHeight)) {
            return Optional.empty();
        }

        return Optional.of(processor.apply(proportion.getValue(context)));
    }

    private static boolean tryComputeDependencies(
            final ProportionValue proportion,
            final Lazy<Integer> computedWidth,
            final Lazy<Integer> computedHeight
    ) {
        if (proportion instanceof ProportionValue.PercentOfSelf) {
            if (((ProportionValue.PercentOfSelf) proportion).isHorizontal()) {
                if (computedWidth.isComputing()) {
                    return false;
                } else {
                    computedWidth.compute();
                }
            } else {
                if (computedHeight.isComputing()) {
                    return false;
                } else {
                    computedHeight.compute();
                }
            }
        }
        return true;
    }

    private static Supplier<Optional<Integer>> createSizeComputeSupplier(
            final Lazy<Integer> lazyMin,
            final Lazy<Integer> lazyMax
    ) {
        return () -> OptionalUtil.applyIfBothArePresent(lazyMax.tryGet(), lazyMin.tryGet(), (max, min) -> max - min);
    }

    private static Supplier<Optional<Integer>> createMinComputeSupplier(
            final Lazy<Integer> lazyMax,
            final Lazy<Integer> lazySize
    ) {
        return () -> OptionalUtil.applyIfBothArePresent(lazyMax.tryGet(), lazySize.tryGet(), (max, size) -> max - size);
    }

    private static Supplier<Optional<Integer>> createMaxComputeSupplier(
            final Lazy<Integer> lazyMin,
            final Lazy<Integer> lazySize
    ) {
        return () -> OptionalUtil.applyIfBothArePresent(lazyMin.tryGet(), lazySize.tryGet(), Integer::sum);
    }

    private static void ensureWidthRelatedAreNotCyclic(final Stream<Optional<ProportionValue>> proportions) {
        proportions.filter(Optional::isPresent)
                   .map(Optional::get)
                   .filter(proportion -> ProportionValue.PercentOfSelf.class.isAssignableFrom(proportion.getClass()))
                   .map(ProportionValue.PercentOfSelf.class::cast)
                   .filter(ProportionValue.PercentOfSelf::isHorizontal)
                   .findAny()
                   .ifPresent(invalidProportion -> {
                       throw new IllegalStateException("Width-related properties cannot be proportional to width!");
                   });
    }

    private static void ensureHeightRelatedAreNotCyclic(final Stream<Optional<ProportionValue>> proportions) {
        proportions.filter(Optional::isPresent)
                   .map(Optional::get)
                   .filter(proportion -> ProportionValue.PercentOfSelf.class.isAssignableFrom(proportion.getClass()))
                   .map(ProportionValue.PercentOfSelf.class::cast)
                   .filter(proportion -> !proportion.isHorizontal())
                   .findAny()
                   .ifPresent(invalidProportion -> {
                       throw new IllegalStateException("Height-related properties cannot be proportional to height!");
                   });
    }

    private static void ensureWidthAndHeightAreNotCyclic(
            final ProportionValue widthProportion,
            final ProportionValue heightProportion
    ) {
        final boolean widthIsPercentOfSelf = widthProportion instanceof ProportionValue.PercentOfSelf;
        final boolean heightIsPercentOfSelf = heightProportion instanceof ProportionValue.PercentOfSelf;
        if (widthIsPercentOfSelf && heightIsPercentOfSelf) {
            final var selfProportionalWidth = (ProportionValue.PercentOfSelf) widthProportion;
            final var selfProportionalHeight = (ProportionValue.PercentOfSelf) heightProportion;
            if (selfProportionalHeight.isHorizontal() && !selfProportionalWidth.isHorizontal()) {
                throw new IllegalStateException("Width and height cannot cyclically depend on each other!");
            }
        }
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.PREPARATIONS)
                    .tickAfter(UIHierarchySystem.class)
                    .requireProvidedResource(UIHierarchy.class)
                    .requireProvidedResource(UIRoot.class)
                    .withComponent(ElementBoundaries.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var hierarchy = world.getResource(UIHierarchy.class);
        final var uiRoot = world.getResource(UIRoot.class);

        final var entityManager = world.getEntityManager();
        final var boundaryLookup = new HashMap<Entity, ElementBoundaries>();
        final var fontSizeLookup = new HashMap<Entity, Integer>();
        entities.sorted(hierarchy::parentsFirst)
                .forEach(entity -> {
                    final var parentBounds = hierarchy.getParentOf(entity)
                                                      .map(boundaryLookup::get)
                                                      .orElseGet(uiRoot::getBoundaries);

                    final var bounds = entityManager.getComponentOf(entity, ElementBoundaries.class).orElseThrow();
                    bounds.invalidate();

                    final var fontSize = entityManager.getComponentOf(entity, FontSize.class)
                                                      .map(fs -> fs.value)
                                                      .orElseGet(() -> hierarchy.getParentOf(entity)
                                                                                .map(fontSizeLookup::get)
                                                                                .orElseGet(uiRoot::getFontSize));
                    boundaryLookup.put(entity, bounds);
                    fontSizeLookup.put(entity, fontSize);
                    final var context = new ProportionValue.Context(fontSize, parentBounds, bounds);

                    final Optional<ProportionValue> maybeLeft = entityManager.getComponentOf(entity, BoundLeft.class)
                                                                             .map(ProportionValueComponent::getValue);
                    final Optional<ProportionValue> maybeRight = entityManager.getComponentOf(entity, BoundRight.class)
                                                                              .map(ProportionValueComponent::getValue);
                    final Optional<ProportionValue> maybeWidth = OptionalUtil.ifAnyEmptyOptional(
                            entityManager.getComponentOf(entity, BoundWidth.class),
                            maybeLeft,
                            maybeRight
                    ).map(ProportionValueComponent::getValue);

                    final Optional<ProportionValue> maybeTop = entityManager.getComponentOf(entity, BoundTop.class)
                                                                            .map(ProportionValueComponent::getValue);
                    final Optional<ProportionValue> maybeBottom =
                            entityManager.getComponentOf(entity, BoundBottom.class)
                                         .map(ProportionValueComponent::getValue);
                    final Optional<ProportionValue> maybeHeight = OptionalUtil.ifAnyEmptyOptional(
                            entityManager.getComponentOf(entity, BoundHeight.class),
                            maybeTop,
                            maybeBottom
                    ).map(ProportionValueComponent::getValue);

                    ensureDependenciesAreValid(maybeLeft, maybeRight, maybeWidth, maybeTop, maybeBottom, maybeHeight);

                    final Lazy<Integer> lazyLeft = new Lazy<>(bounds::setMinX);
                    final Lazy<Integer> lazyRight = new Lazy<>(bounds::setMaxX);
                    final Lazy<Integer> lazyWidth = new Lazy<>(bounds::setWidth);

                    final Lazy<Integer> lazyTop = new Lazy<>(bounds::setMinY);
                    final Lazy<Integer> lazyBottom = new Lazy<>(bounds::setMaxY);
                    final Lazy<Integer> lazyHeight = new Lazy<>(bounds::setHeight);
                    createSuppliersForComputedValues(parentBounds, context,
                                                     maybeLeft, maybeRight, maybeWidth,
                                                     maybeTop, maybeBottom, maybeHeight,
                                                     lazyLeft, lazyRight, lazyWidth,
                                                     lazyTop, lazyBottom, lazyHeight);

                    bounds.minX = lazyLeft.get();
                    bounds.maxX = lazyRight.get();
                    bounds.minY = lazyTop.get();
                    bounds.maxY = lazyBottom.get();
                    bounds.width = lazyWidth.get();
                    bounds.height = lazyHeight.get();

                    entityManager.getComponentOf(entity, BoundAnchorX.class)
                                 .map(boundAnchorX -> boundAnchorX.value)
                                 .ifPresent(anchorX -> {
                                     final var anchorOffset = anchorX.getValue(context);
                                     bounds.minX += anchorOffset;
                                     bounds.maxX += anchorOffset;
                                 });
                    entityManager.getComponentOf(entity, BoundAnchorY.class)
                                 .map(boundAnchorY -> boundAnchorY.value)
                                 .ifPresent(anchorY -> {
                                     final var anchorOffset = anchorY.getValue(context);
                                     bounds.minY += anchorOffset;
                                     bounds.maxY += anchorOffset;
                                 });
                });
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void createSuppliersForComputedValues(
            final ElementBoundaries parentBounds,
            final ProportionValue.Context context,
            final Optional<ProportionValue> maybeLeft,
            final Optional<ProportionValue> maybeRight,
            final Optional<ProportionValue> maybeWidth,
            final Optional<ProportionValue> maybeTop,
            final Optional<ProportionValue> maybeBottom,
            final Optional<ProportionValue> maybeHeight,
            final Lazy<Integer> computedLeft,
            final Lazy<Integer> computedRight,
            final Lazy<Integer> computedWidth,
            final Lazy<Integer> computedTop,
            final Lazy<Integer> computedBottom,
            final Lazy<Integer> computedHeight
    ) {
        // If width is not available, set fallback left/right if needed
        // (without width we cannot compute e.g. "left = right - width")
        if (maybeWidth.isEmpty()) {
            if (maybeRight.isEmpty()) {
                computedRight.setValue(parentBounds.maxX);
            }

            if (maybeLeft.isEmpty()) {
                computedLeft.setValue(parentBounds.minX);
            }
        }

        // If height is not available, set fallback top/bottom if needed
        if (maybeHeight.isEmpty()) {
            if (maybeTop.isEmpty()) {
                computedTop.setValue(parentBounds.minY);
            }

            if (maybeBottom.isEmpty()) {
                computedBottom.setValue(parentBounds.maxY);
            }
        }

        // Width/Height
        computedWidth.setSupplier(createBoundSupplier(maybeWidth, computedWidth, computedHeight,
                                                      context,
                                                      parentBounds.getWidth(),
                                                      (ignored, width) -> width,
                                                      createSizeComputeSupplier(computedLeft, computedRight)));
        computedHeight.setSupplier(createBoundSupplier(maybeHeight, computedWidth, computedHeight,
                                                       context,
                                                       parentBounds.getHeight(),
                                                       (ignored, height) -> height,
                                                       createSizeComputeSupplier(computedTop, computedBottom)));

        // Left/Right
        computedLeft.setSupplier(createBoundSupplier(maybeLeft, computedWidth, computedHeight,
                                                     context,
                                                     parentBounds.getMinX(),
                                                     Integer::sum,
                                                     createMinComputeSupplier(computedRight, computedWidth)));
        computedRight.setSupplier(createBoundSupplier(maybeRight, computedWidth, computedHeight,
                                                      context,
                                                      parentBounds.getMaxX(),
                                                      (parentMaxX, right) -> parentMaxX - right,
                                                      createMaxComputeSupplier(computedLeft, computedWidth)));

        // Top/Bottom
        computedTop.setSupplier(createBoundSupplier(maybeTop, computedWidth, computedHeight,
                                                    context,
                                                    parentBounds.getMinY(),
                                                    Integer::sum,
                                                    createMinComputeSupplier(computedBottom, computedHeight)));
        computedBottom.setSupplier(createBoundSupplier(maybeBottom, computedWidth, computedHeight,
                                                       context,
                                                       parentBounds.getMaxY(),
                                                       (parentMaxY, bottom) -> parentMaxY - bottom,
                                                       createMaxComputeSupplier(computedTop, computedHeight)));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void ensureDependenciesAreValid(
            final Optional<ProportionValue> maybeLeft,
            final Optional<ProportionValue> maybeRight,
            final Optional<ProportionValue> maybeWidth,
            final Optional<ProportionValue> maybeTop,
            final Optional<ProportionValue> maybeBottom,
            final Optional<ProportionValue> maybeHeight
    ) {
        if (maybeWidth.isPresent() && maybeHeight.isPresent()) {
            ensureWidthAndHeightAreNotCyclic(maybeWidth.get(), maybeHeight.get());
        }
        ensureWidthRelatedAreNotCyclic(OptionalUtil.ifAllPresent(Stream.of(maybeWidth, maybeLeft, maybeRight),
                                                                 maybeLeft,
                                                                 maybeRight)
                                                   .orElseGet(() -> Stream.of(maybeWidth)));
        ensureHeightRelatedAreNotCyclic(OptionalUtil.ifAllPresent(Stream.of(maybeHeight, maybeTop, maybeBottom),
                                                                  maybeTop,
                                                                  maybeBottom)
                                                    .orElseGet(() -> Stream.of(maybeHeight)));
    }

    private static class Lazy<T> {
        private final Consumer<T> onComputedCallback;

        @Nullable private T value;
        @Nullable private Supplier<T> supplier;
        private boolean computing;

        public boolean isComputing() {
            return this.computing;
        }

        Lazy(final Consumer<T> onComputedCallback) {
            this.onComputedCallback = onComputedCallback;
        }

        public void setValue(@Nullable final T value) {
            this.value = value;
        }

        public void setSupplier(final Supplier<T> supplier) {
            this.supplier = supplier;
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
                throw new IllegalStateException("Tried to compute value of a Lazy wrapper while computation was" +
                                                        "already in progress!");
            }

            compute();
            if (this.value == null) {
                throw new IllegalStateException("Could not compute the value!");
            }
            return this.value;
        }

        public void compute() {
            Objects.requireNonNull(this.supplier);
            if (this.value != null) {
                return;
            }

            this.computing = true;
            this.value = this.supplier.get();
            this.onComputedCallback.accept(this.value);
            this.computing = false;
        }
    }
}
