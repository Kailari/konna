package fi.jakojaannos.konna.engine.assets.ui;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.regex.Pattern;

import fi.jakojaannos.konna.engine.view.ui.UiUnit;

import static fi.jakojaannos.konna.engine.view.ui.UiUnit.*;

public class UiUnitJsonDeserializer implements JsonDeserializer<UiUnit> {
    @Override
    public UiUnit deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        if (!json.isJsonPrimitive()) {
            throw new JsonParseException("Malformed UI unit: Not a primitive");
        }

        final var jsonPrimitive = json.getAsJsonPrimitive();
        final var asString = jsonPrimitive.getAsString();

        final var regex = Pattern.compile("(^[\\-0-9]*)(.*)");
        final var matcher = regex.matcher(defaultIfEmpty(asString, "100%"));
        if (!matcher.matches()) {
            throw new JsonParseException("Malformed UI unit: Format string did not match the expected format. Offending string: \"" + asString + "\"");
        }

        final var groups = matcher.groupCount();
        if (groups < 1) {
            throw new JsonParseException("Malformed UI unit: Invalid format. Offending string: \"" + asString + "\"");
        }

        final var number = matcher.group(1);
        final double numberValue;
        try {
            numberValue = Double.parseDouble(number);
        } catch (final NumberFormatException e) {
            throw new JsonParseException("Malformed UI unit: The unit should start with a number. Offending string: \"" + number + "\"");
        }

        final var postfix = matcher.groupCount() > 1
                ? matcher.group(2)
                : "";

        return switch (postfix) {
            case "" -> pixels(numberValue);
            case "%" -> percent(numberValue);
            case "%m" -> multiple(numberValue);
            case "em" -> throw new UnsupportedOperationException("Not implemented");
            default -> throw new JsonParseException("Malformed UI unit: Unknown unit type \"" + postfix + "\"");
        };
    }

    private static String defaultIfEmpty(final String string, final String defaultValue) {
        return string.isBlank()
                ? defaultValue
                : string;
    }
}
