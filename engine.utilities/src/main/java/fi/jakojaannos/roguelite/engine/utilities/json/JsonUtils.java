package fi.jakojaannos.roguelite.engine.utilities.json;

import com.google.gson.JsonObject;
import lombok.experimental.UtilityClass;

@UtilityClass
public class JsonUtils {
    public boolean hasAll(final JsonObject jsonObject, final String... memberNames) {
        for (final var memberName : memberNames) {
            if (!jsonObject.has(memberName)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasAnyOf(final JsonObject jsonObject, final String... memberNames) {
        for (final var memberName : memberNames) {
            if (jsonObject.has(memberName)) {
                return true;
            }
        }

        return false;
    }
}
