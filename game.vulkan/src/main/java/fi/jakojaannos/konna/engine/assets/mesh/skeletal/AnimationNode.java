package fi.jakojaannos.konna.engine.assets.mesh.skeletal;

import org.joml.Matrix4f;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class AnimationNode {
    private final String name;
    private final ArrayList<AnimationNode> children = new ArrayList<>();

    @Nullable
    private final AnimationNode parent;
    @Nullable
    private Map<Double, FrameTransform> transforms;

    public Set<Double> getFrameSet() {
        final var ownKeys = Optional.ofNullable(this.transforms)
                                    .map(Map::keySet)
                                    .stream()
                                    .flatMap(Collection::stream);
        final var childKeys = this.children.stream()
                                           .map(AnimationNode::getFrameSet)
                                           .flatMap(Collection::stream);

        return Stream.concat(ownKeys, childKeys)
                     .collect(Collectors.toUnmodifiableSet());
    }

    public void setFrameTransforms(final Map<Double, FrameTransform> transforms) {
        this.transforms = transforms;
    }

    public AnimationNode(final String name, @Nullable final AnimationNode parent) {
        this.name = name;
        this.parent = parent;
    }

    public static Matrix4f getParentTransforms(@Nullable final AnimationNode node, final Double frame) {
        if (node == null) {
            return new Matrix4f();
        } else {
            final var nodeTransform = node.resolveFrameTransform(frame);
            final var parentTransform = getParentTransforms(node.parent, frame);
            return parentTransform.mul(nodeTransform, new Matrix4f());
        }
    }

    private Matrix4f resolveFrameTransform(final Double frame) {
        // Special case: No transforms specified or transform data is empty. Return identity
        //               matrix as that effectively means "no transformations".
        if (this.transforms == null || this.transforms.isEmpty()) {
            return new Matrix4f();
        }

        // Special case: If there is only one element, that determines constant local transforms
        //               for the node. We cannot interpolate nor will the data ever change.
        if (this.transforms.size() == 1) {
            return this.transforms.values().iterator().next().matrix();
        }

        // Return transform for the given frame if one exists
        final var transform = this.transforms.get(frame);
        if (transform != null) {
            return transform.matrix();
        }

        // The frame did not exist, but as we have two or more frames, the transform can be
        // interpolated from the available data. Figure out the next and previous frames and
        // interpolate between them.
        final var keyList = new ArrayList<>(this.transforms.keySet());
        keyList.add(frame);
        keyList.sort(Double::compareTo);

        final var index = keyList.indexOf(frame);
        final var previousIndex = index - 1;
        final var nextIndex = index + 1;

        // Frame is before the first frame, use first frame transform
        if (previousIndex < 0) {
            // The added `frame` is the first in the list, use next index
            return this.transforms.get(keyList.get(nextIndex)).matrix();
        }

        // Frame is past the last frame, use last frame transform
        if (nextIndex >= keyList.size()) {
            // The added `frame` is the last in the list, use previous index
            return this.transforms.get(keyList.get(previousIndex)).matrix();
        }

        // Default to previous frame
        final var previousKey = keyList.get(previousIndex);
        final var previous = this.transforms.get(previousKey);
        return previous.matrix();
    }

    public AnimationNode findByName(final String name) {
        if (this.name.equals(name)) {
            return this;
        }

        return this.children.stream()
                            .map(child -> child.findByName(name))
                            .filter(Objects::nonNull)
                            .findAny()
                            .orElse(null);
    }

    public void addChild(final AnimationNode child) {
        this.children.add(child);
    }
}
