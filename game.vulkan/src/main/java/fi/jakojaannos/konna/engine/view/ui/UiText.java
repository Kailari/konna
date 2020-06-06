package fi.jakojaannos.konna.engine.view.ui;

public class UiText {
    private String format;
    private String[] args;
    private Integer size;
    private Alignment align;
    private Alignment verticalAlign;
    private Color color;

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

    public Alignment verticalAlign() {
        return this.verticalAlign;
    }

    public void verticalAlign(final Alignment align) {
        this.verticalAlign = align;
    }

    public void color(final Color color) {
        this.color = color;
    }

    public Color color() {
        return this.color;
    }
}
