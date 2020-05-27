package fi.jakojaannos.konna.engine.assets.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import fi.jakojaannos.konna.engine.assets.AssetLoader;
import fi.jakojaannos.konna.engine.view.ui.Color;
import fi.jakojaannos.konna.engine.view.ui.UiElement;
import fi.jakojaannos.konna.engine.view.ui.UiUnit;
import fi.jakojaannos.konna.engine.view.ui.impl.UiElementImpl;

public class UiLoader implements AssetLoader<UiElement> {
    private static final Logger LOG = LoggerFactory.getLogger(UiLoader.class);

    private final Gson gson;

    public UiLoader() {
        this.gson = new GsonBuilder().setLenient()
                                     .registerTypeAdapter(UiUnit.class, new UiUnitJsonDeserializer())
                                     .registerTypeAdapter(Color.class, new ColorJsonDeserializer())
                                     .create();
    }

    @Override
    public Optional<UiElement> load(final Path path) {
        try (final var reader = new InputStreamReader(Files.newInputStream(path, StandardOpenOption.READ))) {
            final var maybeRoot = Optional.ofNullable(this.gson.fromJson(reader, UiElementImpl.class));
            maybeRoot.ifPresent(UiLoader::updateParents);
            return maybeRoot.map(UiElement.class::cast);
        } catch (final IOException e) {
            LOG.error("Reading UI from path \"{}\" failed!", path);
            LOG.error("Exception: ", e);
            return Optional.empty();
        }
    }

    private static void updateParents(final UiElementImpl element) {
        element.children().forEach(c -> {
            if (c instanceof UiElementImpl child) {
                child.setParent(element);
                updateParents(child);
            }
        });
    }

}
