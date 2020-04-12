package fi.jakojaannos.roguelite.engine.view.data.components.internal;

public class Name {
    public String value;

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Name(final String value) {
        this.value = value;
    }
}
