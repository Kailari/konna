package fi.jakojaannos.konna.engine.application;

import java.util.HashMap;
import java.util.Map;

public class UiVariables {
    private final Map<String, Object> values = new HashMap<>();

    public void set(final String key, final Object value) {
        this.values.put(key, value);
    }

    public void clear() {
        this.values.clear();
    }

    public Object get(final String key) {
        return this.values.getOrDefault(key, 0);
    }
}
