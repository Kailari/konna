package fi.jakojaannos.konna.engine.view.ui;

public class UiText {
    private String format;
    private String[] args;
    private int size;
    private Alignment align;

    public String format() {
        return this.format;
    }

    public void format(final String format) {
        this.format = format;
    }

    public String[] args() {
        return this.args;
    }

    public void args(final String[] args) {
        this.args = args;
    }

    public int size() {
        return this.size;
    }

    public void size(final int size) {
        this.size = size;
    }

    public Alignment align() {
        return this.align;
    }

    public void align(final Alignment align) {
        this.align = align;
    }
}
