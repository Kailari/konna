package fi.jakojaannos.riista.view.assets;

public interface Font extends AutoCloseable {
    float getLineOffset();

    boolean isKerningEnabled();

    FontTexture getForSize(int fontSize);

    @Override
    void close();
}
