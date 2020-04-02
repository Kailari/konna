package fi.jakojaannos.roguelite.engine.utilities.json;

import com.google.gson.JsonObject;

public class JsonUtils {
    public static boolean hasAll(final JsonObject jsonObject, final String... memberNames) {
        for (final var memberName : memberNames) {
            if (!jsonObject.has(memberName)) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasAnyOf(final JsonObject jsonObject, final String... memberNames) {
        for (final var memberName : memberNames) {
            if (jsonObject.has(memberName)) {
                return true;
            }
        }

        return false;
    }

    private JsonUtils() {
    }
}
