package fi.jakojaannos.roguelite.engine.view.rendering.sprite.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"unchecked", "rawtypes"})
class SpriteDeserializerTest {
    private Gson gson;

    @BeforeEach
    void beforeEach() {
        AssetRegistry textures = mock(AssetRegistry.class);
        when(textures.getByAssetName(any())).thenReturn(new Texture() {
            @Override
            public int getWidth() {
                return 32;
            }

            @Override
            public int getHeight() {
                return 32;
            }

            @Override
            public void use() {
            }

            @Override
            public void close() {
            }
        });
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Sprite.class, new SpriteDeserializer(textures))
                .create();
    }

    @Test
    void deserializationSucceedsWhenAnimationsBlockIsOmitted() {
        String json = "" +
                "{" +
                "   \"frames\": [" +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 0," +
                "           \"y\": 0," +
                "           \"w\": 32," +
                "           \"h\": 32" +
                "       }" +
                "   ]" +
                "}";

        assertDoesNotThrow(() -> {
            gson.fromJson(json, Sprite.class);
        });
    }

    @Test
    void deserializationFailsWhenFramesBlockIsMissing() {
        String json = "{" +
                "   \"animations\": {" +
                "       \"default\": [ 0 ]" +
                "   }" +
                "}";

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, Sprite.class));
    }

    @Test
    void deserializationSucceedsWhenFramesBlockIsDefinedAsAnAtlas() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }" +
                "}";

        assertDoesNotThrow(() -> {
            gson.fromJson(json, Sprite.class);
        });
    }

    @Test
    void deserializationWhenFramesBlockIsDefinedAsAnAtlasYieldsCorrectNumberOfFrames() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }" +
                "}";

        assertEquals(16, gson.fromJson(json, Sprite.class)
                             .getAnimationFrameCount("default"));
    }

    @Test
    void deserializationSucceedsWhenFramesBlockIsDefinedAsListOfFrames() {
        String json = "" +
                "{" +
                "   \"frames\": [" +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 0," +
                "           \"y\": 0," +
                "           \"w\": 8," +
                "           \"h\": 8" +
                "       }," +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 8," +
                "           \"y\": 0," +
                "           \"w\": 24," +
                "           \"h\": 8" +
                "       }," +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 8," +
                "           \"y\": 8," +
                "           \"w\": 24," +
                "           \"h\": 24" +
                "       }" +
                "   ]" +
                "}";

        assertDoesNotThrow(() -> {
            gson.fromJson(json, Sprite.class);
        });
    }

    @Test
    void deserializationWhenFramesBlockIsDefinedAsListOfFramesYieldsCorrectNumberOfFrames() {
        String json = "" +
                "{" +
                "   \"frames\": [" +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 0," +
                "           \"y\": 0," +
                "           \"w\": 8," +
                "           \"h\": 8" +
                "       }," +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 8," +
                "           \"y\": 0," +
                "           \"w\": 24," +
                "           \"h\": 8" +
                "       }," +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 8," +
                "           \"y\": 8," +
                "           \"w\": 24," +
                "           \"h\": 24" +
                "       }" +
                "   ]" +
                "}";

        assertEquals(3, gson.fromJson(json, Sprite.class)
                            .getAnimationFrameCount("default"));
    }

    @Test
    void deserializationFailsWhenFramesAreDefinedIncorrectly() {
        String json = "" +
                "{" +
                "   \"frames\": [" +
                "       {" +
                "           \"texture\": \"valid\"," +
                "           \"x\": 8," +
                "           \"h\": 24" +
                "       }" +
                "   ]" +
                "}";

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, Sprite.class)
                                                         .getAnimationFrameCount("default"));
    }

    @Test
    void deserializationFailsWhenAnimationsAreDefinedAsRangeButRangeStartIsMissing() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"last\": 2," +
                "       \"totalDuration\": 10" +
                "   }" +
                "}";

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, Sprite.class));
    }

    @Test
    void deserializationFailsWhenAnimationsAreDefinedAsRangeButRangeLastIsMissing() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"first\": 2," +
                "       \"totalDuration\": 10" +
                "   }" +
                "}";

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, Sprite.class));
    }

    @Test
    void deserializationFailsWhenAnimationsAreDefinedAsRangeAndDurationIsOmitted() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"default\": {" +
                "           \"first\": 2," +
                "           \"last\": 10" +
                "       }" +
                "   }" +
                "}";

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, Sprite.class));
    }

    @Test
    void deserializationSucceedsWhenAnimationsAreDefinedAsRangeAndTotalDurationIsGiven() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"default\": {" +
                "           \"first\": 2," +
                "           \"last\": 10," +
                "           \"totalDuration\": 10" +
                "       }" +
                "   }" +
                "}";

        assertDoesNotThrow(() -> gson.fromJson(json, Sprite.class));
    }

    @Test
    void deserializationWhenAnimationsAreDefinedAsRangeAndTotalDurationIsGivenYieldsCorrectNumberOfFrames() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"default\": {" +
                "           \"first\": 2," +
                "           \"last\": 10," +
                "           \"totalDuration\": 10" +
                "       }" +
                "   }" +
                "}";

        assertEquals(9, gson.fromJson(json, Sprite.class).getAnimationFrameCount("default"));
    }

    @Test
    void deserializationSucceedsWhenAnimationsAreDefinedAsRangeAndIndividualDurationsAreGiven() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"default\": {" +
                "           \"first\": 8," +
                "           \"last\": 10," +
                "           \"durations\": [ 10, 9, 7 ]" +
                "       }" +
                "   }" +
                "}";

        assertDoesNotThrow(() -> gson.fromJson(json, Sprite.class));
    }

    @Test
    void deserializationFailsWhenAnimationsAreDefinedAsRangeAndIndividualDurationsAreGivenButDurationCountDoesNotMatch() {
        String json = "" +
                "{" +
                "   \"frames\": {" +
                "       \"texture\": \"valid\"," +
                "       \"rows\": 4," +
                "       \"columns\": 4" +
                "   }," +
                "   \"animations\": {" +
                "       \"default\": {" +
                "           \"first\": 8," +
                "           \"last\": 10," +
                "           \"durations\": [ 10, 9, 7, 42 ]" +
                "       }" +
                "   }" +
                "}";

        assertThrows(JsonParseException.class, () -> gson.fromJson(json, Sprite.class));
    }
}
