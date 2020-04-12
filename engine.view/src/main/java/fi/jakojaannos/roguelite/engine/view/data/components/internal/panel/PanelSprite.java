package fi.jakojaannos.roguelite.engine.view.data.components.internal.panel;

public class PanelSprite {
    private String sprite;

    public String getSprite() {
        return this.sprite;
    }

    public void setSprite(final String sprite) {
        this.sprite = sprite;
    }

    public PanelSprite(final String sprite) {
        this.sprite = sprite;
    }
}
