package fi.jakojaannos.riista.vulkan.assets.ui;

import com.google.gson.*;

import java.lang.reflect.Type;

import javax.annotation.Nullable;

import fi.jakojaannos.riista.view.ui.Color;

public class ColorJsonDeserializer implements JsonDeserializer<Color> {
    @Override
    public Color deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        if (json.isJsonArray()) {
            return deserializeFromArray(json.getAsJsonArray());
        } else if (json.isJsonObject()) {
            return deserializeFromObject(json.getAsJsonObject());
        } else {
            throw new JsonParseException("Color must either be an array with length of three or four "
                                         + "OR an object which defines r, g, b (and optionally a)");
        }
    }

    private Color deserializeFromObject(final JsonObject jsonObject) {

        final var a = jsonObject.has("a")
                ? getClamped(jsonObject.get("a"))
                : 1.0f;

        return new Color(getClamped(jsonObject.get("r")),
                         getClamped(jsonObject.get("g")),
                         getClamped(jsonObject.get("b")),
                         a);
    }

    private Color deserializeFromArray(final JsonArray jsonArray) {
        if (jsonArray.size() < 3) {
            throw new JsonParseException("Not enough components for color. "
                                         + "Expected at least 3, got " + jsonArray.size());
        }

        final var r = getClamped(jsonArray.get(0));
        final var g = getClamped(jsonArray.get(1));
        final var b = getClamped(jsonArray.get(2));
        final var a = jsonArray.size() < 4
                ? 1.0f
                : getClamped(jsonArray.get(3));

        return new Color(r, g, b, a);
    }

    private float getClamped(@Nullable final JsonElement jsonElement) {
        if (jsonElement == null) {
            return 0.0f;
        }

        if (!jsonElement.isJsonPrimitive() || !jsonElement.getAsJsonPrimitive().isNumber()) {
            throw new JsonParseException("Expected element to be a number. Got \""
                                         + jsonElement.toString() + "\"");
        }

        final var value = jsonElement.getAsFloat();
        return Math.max(Math.min(value, 1.0f), 0.0f);
    }
}
