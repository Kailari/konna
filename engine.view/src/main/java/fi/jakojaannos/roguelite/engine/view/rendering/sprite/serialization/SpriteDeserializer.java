package fi.jakojaannos.roguelite.engine.view.rendering.sprite.serialization;

import com.google.gson.*;
import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.utilities.json.JsonUtils;
import fi.jakojaannos.roguelite.engine.view.LogCategories;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.TextureRegion;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Animation;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpriteDeserializer<TTexture extends Texture>
        implements JsonDeserializer<Sprite> {

    private final AssetRegistry<TTexture> textures;

    public SpriteDeserializer(AssetRegistry<TTexture> textures) {
        this.textures = textures;
    }

    @Override
    public Sprite deserialize(
            final JsonElement json,
            final Type typeOfT,
            final JsonDeserializationContext context
    ) throws JsonParseException {
        val jsonObject = json.getAsJsonObject();

        List<TextureRegion> frames = new ArrayList<>();
        deserializeFrames(jsonObject, frames);

        val animationsJson = jsonObject.getAsJsonObject("animations");
        Map<String, Animation> animations = new HashMap<>();
        deserializeAnimations(animationsJson, frames.size(), animations);

        return new Sprite(List.copyOf(frames), Map.copyOf(animations));
    }

    private void deserializeFrames(
            final JsonObject root,
            final List<TextureRegion> outFrames
    ) {
        val framesElement = root.get("frames");
        if (framesElement == null) {
            throw new JsonParseException("Sprite definition missing frames!");
        }

        if (framesElement.isJsonObject()) {
            val framesJsonObject = framesElement.getAsJsonObject();
            val textureHandle = framesJsonObject.get("texture").getAsString();
            val texture = this.textures.getByAssetName(textureHandle);
            val rows = framesJsonObject.get("rows").getAsInt();
            val columns = framesJsonObject.get("columns").getAsInt();

            val frameU = 1.0 / rows;
            val frameV = 1.0 / columns;
            for (var row = 0; row < rows; ++row) {
                for (var column = 0; column < columns; ++column) {
                    val u0 = column * frameU;
                    val v0 = row * frameV;
                    val u1 = (column + 1) * frameU;
                    val v1 = (row + 1) * frameV;
                    outFrames.add(new TextureRegion(texture,
                                                    u0, v0,
                                                    u1, v1));
                }
            }
        } else {
            val framesJsonArray = framesElement.getAsJsonArray();
            for (val frameElement : framesJsonArray) {
                deserializeSingleFrame(outFrames, frameElement);
            }
        }
    }

    private void deserializeSingleFrame(
            final List<TextureRegion> outFrames,
            final JsonElement frameElement
    ) {
        val frameJson = frameElement.getAsJsonObject();
        if (!JsonUtils.hasAll(frameJson, "x", "y", "w", "h", "texture")) {
            throw new JsonParseException("Malformed frame texture region definition!");
        }

        val textureHandle = frameJson.get("texture").getAsString();
        val texture = this.textures.getByAssetName(textureHandle);
        val x = frameJson.get("x").getAsDouble();
        val y = frameJson.get("y").getAsDouble();
        val w = frameJson.get("w").getAsDouble();
        val h = frameJson.get("h").getAsDouble();
        val u0 = x / texture.getWidth();
        val v0 = y / texture.getHeight();
        val u1 = u0 + w / texture.getWidth();
        val v1 = v0 + h / texture.getHeight();
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

        for (val animationEntry : animationsJson.entrySet()) {
            deserializeAnimation(animations, animationEntry);
        }
    }

    private void deserializeAnimation(
            Map<String, Animation> animations,
            Map.Entry<String, JsonElement> animationEntry
    ) {
        val animationName = animationEntry.getKey();
        val animationElement = animationEntry.getValue();

        Animation animation = null;
        // JsonArray => list of frames
        if (animationElement.isJsonArray()) {
            val animationArray = animationElement.getAsJsonArray();
            List<Animation.Frame> animationFrames = new ArrayList<>();
            for (val frameElement : animationArray) {
                val frameObj = frameElement.getAsJsonObject();

                animationFrames.add(new Animation.Frame(frameObj.get("index").getAsInt(),
                                                        frameObj.get("duration").getAsDouble()));
            }

            animation = Animation.forFrames(animationFrames);
        }
        // Object => single frame or range
        else if (animationElement.isJsonObject()) {
            val animationJsonObject = animationElement.getAsJsonObject();
            if (JsonUtils.hasAll(animationJsonObject, "first", "last")
                    && JsonUtils.hasAnyOf(animationJsonObject, "totalDuration", "durations")) {

                val first = animationJsonObject.get("first").getAsInt();
                val last = animationJsonObject.get("last").getAsInt();
                if (animationJsonObject.has("totalDuration")) {
                    animation = Animation.forFrameRange(first,
                                                        last,
                                                        animationJsonObject.get("totalDuration").getAsDouble());
                } else {
                    val durationsArray = animationJsonObject.getAsJsonArray("durations");
                    val durations = new double[last - first + 1];
                    int index = 0;
                    for (val duration : durationsArray) {
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

        animations.put(animationName, animation);
    }
}
