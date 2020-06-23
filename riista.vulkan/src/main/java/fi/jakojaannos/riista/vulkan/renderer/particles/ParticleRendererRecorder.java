package fi.jakojaannos.riista.vulkan.renderer.particles;

import org.joml.Vector3d;
import org.joml.Vector3f;

import fi.jakojaannos.riista.view.ParticleRenderer;
import fi.jakojaannos.riista.view.Presentable;
import fi.jakojaannos.riista.vulkan.application.PresentableState;

public class ParticleRendererRecorder implements ParticleRenderer {
    private PresentableState writeState;

    public void setWriteState(final PresentableState state) {
        this.writeState = state;
    }

    @Override
    public void drawParticleSystem(final Vector3d position, final double time, final int count) {
        final var entry = this.writeState.particleSystemEntries().get();
        entry.position.set(position);
        entry.time = (float) time;
        entry.count = count;
    }

    public static class SystemEntry implements Presentable {
        public final Vector3f position = new Vector3f();

        public float time;
        public int count;

        @Override
        public void reset() {
            this.position.set(0.0);
        }
    }
}
