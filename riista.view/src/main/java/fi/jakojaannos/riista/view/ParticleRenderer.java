package fi.jakojaannos.riista.view;

import org.joml.Vector3d;

public interface ParticleRenderer {
    void drawParticleSystem(Vector3d position, final double time, final int count);
}
