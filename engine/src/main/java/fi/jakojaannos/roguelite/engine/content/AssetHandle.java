package fi.jakojaannos.roguelite.engine.content;

public record AssetHandle(String domain, String name) {
    public static final String DEFAULT_DOMAIN = "konna";

    public AssetHandle(final String name) {
        this(DEFAULT_DOMAIN, name);
    }

    @Override
    public String toString() {
        return String.format("%s:%s", this.domain, this.name);
    }
}
