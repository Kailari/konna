package fi.jakojaannos.roguelite.engine.view.rendering.sprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class Animation {
    private final List<Frame> frames;
    private final double totalDuration;

    private Animation(final List<Frame> frames) {
        if (frames.stream().anyMatch(frame -> frame.duration < 0)) {
            throw new IllegalArgumentException("Cannot have frame with negative duration!");
        }

        this.frames = frames;
        this.totalDuration = this.frames.stream()
                                        .mapToDouble(frame -> frame.duration)
                                        .sum();
    }

    public static Animation forSingleFrame(final int index, final double duration) {
        return new Animation(List.of(new Frame(index, duration)));
    }

    public static Animation forFrameRange(
            final int firstFrame,
            final int lastFrame,
            final double totalDuration
    ) {
        final var count = lastFrame - firstFrame + 1;
        final var durationPerFrame = totalDuration < 0
                ? Double.POSITIVE_INFINITY
                : totalDuration / count;
        return new Animation(IntStream.rangeClosed(firstFrame, lastFrame)
                                      .mapToObj(index -> new Frame(index, durationPerFrame))
                                      .collect(Collectors.toUnmodifiableList()));
    }

    public static Animation forFrameRangeWithDurations(
            final int firstFrame,
            final int lastFrame,
            final double[] durations
    ) {
        final List<Double> durationList = new ArrayList<>();
        Arrays.stream(durations)
              .map(Double.class::cast)
              .forEach(durationList::add);
        return new Animation(IntStream.rangeClosed(firstFrame, lastFrame)
                                      .mapToObj(index -> new Frame(index, durationList.remove(0)))
                                      .collect(Collectors.toUnmodifiableList()));
    }

    public static Animation forFrames(
            final List<Frame> frames
    ) {
        return new Animation(List.copyOf(frames));
    }

    public int frameCount() {
        return this.frames.size();
    }

    public int getFrameIndexOfFrame(final int n) {
        return this.frames.get(n).index;
    }

    public int getFrameIndexAtTime(final double time) {
        var t = time % this.totalDuration;
        int i = -1;
        do {
            ++i;
            final var frameDuration = this.frames.get(i).duration;
            if (Double.isInfinite(frameDuration)) {
                break;
            }

            t -= frameDuration;
        } while (t > 0);

        return this.frames.get(i).index;
    }

    public static record Frame(int index, double duration) {
        public Frame {
            this.index = index;
            this.duration = duration < 0
                    ? Double.POSITIVE_INFINITY
                    : duration;
        }
    }
}
