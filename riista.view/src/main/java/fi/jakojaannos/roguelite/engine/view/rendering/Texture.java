package fi.jakojaannos.roguelite.engine.view.rendering;

public interface Texture extends AutoCloseable {
    int getWidth();

    int getHeight();

    void use();
}
