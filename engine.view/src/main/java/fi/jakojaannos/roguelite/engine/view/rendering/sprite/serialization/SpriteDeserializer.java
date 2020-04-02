package fi.jakojaannos.roguelite.engine.view.rendering.sprite.serialization;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.utilities.json.JsonUtils;
import fi.jakojaannos.roguelite.engine.view.LogCategories;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Animation;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;

public class SpriteDeserializer<TTexture extends Texture> implements JsonDeserializer<Sprite> {
    private static final Logger LOG = LoggerFactory.getLogger(SpriteDeserializer.class);

    private final AssetRegistry<TTexture> textures;

    public SpriteDeserializer(final AssetRegistry<TTexture> textures) {
        this.textures = textures;
    }

    @Override
    public Sprite deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        final var jsonObject = json.getAsJsonObject();

        final List<TextureRegion> frames = new ArrayList<>();
        deserializeFrames(jsonObject, frames);

        final var animationsJson = jsonObject.getAsJsonObject("animations");
        final Map<String, Animation> animations = new HashMap<>();
        deserializeAnimations(animationsJson, frames.size(), animations);

        return new Sprite(List.copyOf(frames), Map.copyOf(animations));
    }

    private void deserializeFrames(
            final JsonObject root,
            final List<TextureRegion> outFrames
    ) {
        final var framesElement = root.get("frames");
        if (framesElement == null) {
            throw new JsonParseException("Sprite definition missing frames!");
        }

        if (framesElement.isJsonObject()) {
            final var framesJsonObject = framesElement.getAsJsonObject();
            final var textureHandle = framesJsonObject.get("texture").getAsString();
            final var texture = this.textures.getByAssetName(textureHandle);
            final var rows = framesJsonObject.get("rows").getAsInt();
            final var columns = framesJsonObject.get("columns").getAsInt();

            final var frameU = 1.0 / rows;
            final var frameV = 1.0 / columns;
            for (var row = 0; row < rows; ++row) {
                for (var column = 0; column < columns; ++column) {
                    final var u0 = column * frameU;
                    final var v0 = row * frameV;
                    final var u1 = (column + 1) * frameU;
                    final var v1 = (row + 1) * frameV;
                    outFrames.add(new TextureRegion(texture,
                                                    u0, v0,
                                                    u1, v1));
                }
            }
        } else {
            final var framesJsonArray = framesElement.getAsJsonArray();
            for (final var frameElement : framesJsonArray) {
                deserializeSingleFrame(outFrames, frameElement);
            }
        }
    }

    private void deserializeSingleFrame(
            final List<TextureRegion> outFrames,
            final JsonElement frameElement
    ) {
        final var frameJson = frameElement.getAsJsonObject();
        if (!JsonUtils.hasAll(frameJson, "x", "y", "w", "h", "texture")) {
            throw new JsonParseException("Malformed frame texture region definition!");
        }

        final var textureHandle = frameJson.get("texture").getAsString();
        final var texture = this.textures.getByAssetName(textureHandle);
        final var x = frameJson.get("x").getAsDouble();
        final var y = frameJson.get("y").getAsDouble();
        final var w = frameJson.get("w").getAsDouble();
        final var h = frameJson.get("h").getAsDouble();
        final var u0 = x / texture.getWidth();
        final var v0 = y / texture.getHeight();
        final var u1 = u0 + w / texture.getWidth();
        final var v1 = v0 + h / texture.getHeight();
        outFrames.add(new TextureRegion(texture, u0, v0, u1, v1));
    }

    private void deserializeAnimations(
            @Nullable final JsonObject animationsJson,
            final int frameCount,
            final Map<String, Animation> animations
    ) {
        if (animationsJson == null) {
            LOG.trace(LogCategories.SPRITE_SERIALIZATION,
                      "=> No animations found. Defaulting to infinite individual frames of all available frames.");
            animations.put("default", Animation.forFrameRange(0, frameCount - 1, Double.POSITIVE_INFINITY));
            return;
        }

        for (final var animationEntry : animationsJson.entrySet()) {
            deserializeAnimation(animations, animationEntry);
        }
    }

    private void deserializeAnimation(
            final Map<String, Animation> outAnimations,
            final Map.Entry<String, JsonElement> animationEntry
    ) {
        final var animationName = animationEntry.getKey();
        final var animationElement = animationEntry.getValue();

        Animation animation = null;
        // JsonArray => list of frames
        if (animationElement.isJsonArray()) {
            final var animationArray = animationElement.getAsJsonArray();
            final List<Animation.Frame> animationFrames = new ArrayList<>();

            for (final var frameElement : animationArray) {
                if (frameElement.isJsonPrimitive()) {
                    animationFrames.add(new Animation.Frame(frameElement.getAsInt(), 1.0));
                } else {
                    final var frameObj = frameElement.getAsJsonObject();

                    animationFrames.add(new Animation.Frame(frameObj.get("index").getAsInt(),
                                                            frameObj.get("duration").getAsDouble()));
                }
            }

            animation = Animation.forFrames(animationFrames);
        }
        // Object => single frame or range
        else if (animationElement.isJsonObject()) {
            final var animationJsonObject = animationElement.getAsJsonObject();
            if (JsonUtils.hasAll(animationJsonObject, "first", "last")
                    && JsonUtils.hasAnyOf(animationJsonObject, "totalDuration", "durations")) {

                final var first = animationJsonObject.get("first").getAsInt();
                final var last = animationJsonObject.get("last").getAsInt();
                if (animationJsonObject.has("totalDuration")) {
                    animation = Animation.forFrameRange(first,
                                                        last,
                                                        animationJsonObject.get("totalDuration").getAsDouble());
                } else {
                    final var durationsArray = animationJsonObject.getAsJsonArray("durations");
                    final var durations = new double[last - first + 1];
                    int index = 0;
                    for (final var duration : durationsArray) {
                        if (index >= durations.length) {
                            throw new JsonParseException("Animation duration count does not match frame count!");
                        }
                        durations[index] = duration.getAsDouble();
                        ++index;
                    }

                    animation = Animation.forFrameRangeWithDurations(first,
                                                                     last,
                                                                     durations);
                }
            } else if (animationJsonObject.has("index")) {
                animation = Animation.forSingleFrame(animationJsonObject.get("index").getAsInt(),
                                                     animationJsonObject.get("duration").getAsDouble());
            }
        }

        if (animation == null) {
            throw new JsonParseException("Malformed animation frame definition!");
        }

        outAnimations.put(animationName, animation);
    }
}
