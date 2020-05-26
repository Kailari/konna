package fi.jakojaannos.konna.engine.assets.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.regex.Pattern;

import fi.jakojaannos.konna.engine.assets.AssetLoader;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;
import fi.jakojaannos.konna.engine.view.ui.impl.UiElementImpl;

import static fi.jakojaannos.konna.engine.view.ui.UiUnit.percent;
import static fi.jakojaannos.konna.engine.view.ui.UiUnit.pixels;

public class UiLoader implements AssetLoader<UiElement> {
    private static final Logger LOG = LoggerFactory.getLogger(UiLoader.class);

    private final Gson gson;

    public UiLoader() {
        this.gson = new GsonBuilder().setLenient()
                                     .registerTypeAdapter(UiUnit.class, (JsonDeserializer<UiUnit>) (json, typeOfT, context) -> {
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
                                             case "em" -> throw new UnsupportedOperationException("Not implemented");
                                             default -> throw new JsonParseException("Malformed UI unit: Unknown unit type \"" + postfix + "\"");
                                         };
                                     })
                                     .create();
    }

    private String defaultIfEmpty(final String string, final String defaultValue) {
        return string.isBlank()
                ? defaultValue
                : string;
    }

    @Override
    public Optional<UiElement> load(final Path path) {
        try (final var reader = new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ))) {
            return Optional.ofNullable(this.gson.fromJson(reader, UiElementImpl.class));
        } catch (final IOException e) {
            LOG.error("Reading UI from path \"{}\" failed!", path);
            LOG.error("Exception: ", e);
            return Optional.empty();
        }
    }
}
