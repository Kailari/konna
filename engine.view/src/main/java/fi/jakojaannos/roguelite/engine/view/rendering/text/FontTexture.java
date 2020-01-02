package fi.jakojaannos.roguelite.engine.view.rendering.text;

public interface FontTexture {
    void use();

    float getPixelHeightScale();

    float getContentScaleX();

    float getContentScaleY();
}
