package fi.jakojaannos.konna.engine.assets;

public interface Font extends AutoCloseable {
    float getLineOffset();

    boolean isKerningEnabled();

    FontTexture getForSize(int fontSize);

    @Override
    void close();
}
