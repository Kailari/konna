package fi.jakojaannos.riista.vulkan.assets.ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

import fi.jakojaannos.riista.assets.AssetLoader;
import fi.jakojaannos.riista.view.ui.Color;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.view.ui.UiUnit;

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

            final var lookup = new HashMap<String, UiElementImpl>();
            final var variants = new ArrayList<UiElementImpl>();
            maybeRoot.ifPresent(root -> postProcessHierarchy(root, lookup, variants));
            updateVariants(lookup, variants);

            return maybeRoot.map(UiElement.class::cast);
        } catch (final IOException e) {
            LOG.error("Reading UI from path \"{}\" failed!", path);
            LOG.error("Exception: ", e);
            return Optional.empty();
        }
    }

    private static void updateVariants(
            final Map<String, UiElementImpl> lookup,
            final List<UiElementImpl> variants
    ) {
        variants.forEach(variant -> {
            if (variant.name().startsWith("hover:")) {
                // Name without `hover:`
                final var targetName = variant.name().substring(6);
                if (!lookup.containsKey(targetName)) {
                    LOG.warn("UI element variant \"{}\" missing the base element \"{}\"!",
                             variant.name(),
                             targetName);
                    return;
                }

                lookup.get(targetName)
                      .hoverElement(variant);
            }
        });
    }

    private static void postProcessHierarchy(
            final UiElementImpl element,
            final Map<String, UiElementImpl> lookup,
            final List<UiElementImpl> variants
    ) {
        if (element.name().startsWith("hover:")) {
            variants.add(element);
        } else {
            lookup.put(element.name(), element);
        }

        element.children().forEach(c -> {
            if (c instanceof UiElementImpl child) {
                child.setParent(element);
                postProcessHierarchy(child, lookup, variants);
            }
        });

        element.clearVariantChildren();
    }
}
