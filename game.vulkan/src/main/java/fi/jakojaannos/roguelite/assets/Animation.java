package fi.jakojaannos.roguelite.assets;

import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public record Animation(
        String name,
        List<Frame>frames,
        double duration
) {
    private static final Logger LOG = LoggerFactory.getLogger(Animation.class);

    public static class Node {
        private final String name;
        private final ArrayList<Node> children = new ArrayList<>();

        @Nullable
        private final Node parent;
        @Nullable
        private Map<Double, Matrix4f> transforms;

        public Set<Double> getFrameSet() {
            final var ownKeys = Optional.ofNullable(this.transforms)
                                        .map(Map::keySet)
                                        .stream()
                                        .flatMap(Collection::stream);
            final var childKeys = this.children.stream()
                                               .map(Node::getFrameSet)
                                               .flatMap(Collection::stream);

            return Stream.concat(ownKeys, childKeys)
                         .collect(Collectors.toUnmodifiableSet());
        }

        public void setFrameTransforms(final Map<Double, Matrix4f> transforms) {
            this.transforms = transforms;
        }

        public Node(final String name, @Nullable final Node parent) {
            this.name = name;
            this.parent = parent;
        }

        public static Matrix4f getParentTransforms(@Nullable final Node node, final Double frame) {
            if (node == null) {
                return new Matrix4f();
            } else {
                final var parentTransform = new Matrix4f(getParentTransforms(node.parent, frame));

                final Matrix4f nodeTransform;
                if (node.transforms != null) {
                    final var frameTransform = node.transforms.get(frame);
                    nodeTransform = Optional.ofNullable(frameTransform)
                                            .orElseGet(Matrix4f::new);
                } else {
                    nodeTransform = new Matrix4f();
                }

                return parentTransform.mul(nodeTransform);
            }
        }

        public Node findByName(final String name) {
            if (this.name.equals(name)) {
                return this;
            }

            return this.children.stream()
                                .map(child -> child.findByName(name))
                                .filter(Objects::nonNull)
                                .findAny()
                                .orElse(null);
        }

        public void addChild(final Node child) {
            this.children.add(child);
        }
    }

    public static record Frame(Matrix4f[]boneTransforms) {}
}
