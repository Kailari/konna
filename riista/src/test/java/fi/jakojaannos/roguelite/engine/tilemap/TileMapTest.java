package fi.jakojaannos.roguelite.engine.tilemap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.utilities.Streams;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TileMapTest {
    private TileMap<String> level;

    @BeforeEach
    void beforeEach() {
        level = new TileMap<>("default");
    }

    @Test
    void getTileReturnsDefaultTileWhenTileIsNotSet() {
        assertEquals("default", level.getTile(1337, 9001));
    }

    @Test
    void setTileUpdatesMapWhenTileIsValid() {
        level.setTile(42, 69, "test");
        assertEquals("test", level.getTile(42, 69));
    }

    @Test
    void setTileWorksWithNegativeXCoordinates() {
        level.setTile(-42, 69, "test");
        assertEquals("test", level.getTile(-42, 69));
    }

    @Test
    void setTileWorksWithNegativeYCoordinates() {
        level.setTile(42, -69, "test");
        assertEquals("test", level.getTile(42, -69));
    }

    @Test
    void simpleRoomGeneratorCanBeImplementedUsingSetTile() {
        int startX = -25;
        int startY = -20;
        int roomWidth = 50;
        int roomHeight = 40;
        Streams.ofCoordinates(startX, startY, roomWidth, roomHeight)
               .filter(pos -> pos.x == startX + roomWidth - 1 || pos.x == startX || pos.y == startY + roomHeight - 1 || pos.y == startY)
               .forEach(pos -> level.setTile(pos, "wall"));
        Streams.ofCoordinates(startX + 1, startY + 1, roomWidth - 2, roomHeight - 2)
               .forEach(pos -> level.setTile(pos, "floor"));

        for (int x = -50; x < 50; ++x) {
            for (int y = -50; y < 50; ++y) {
                final String expected;
                if (((x == startX || x == startX + roomWidth - 1) && (y >= startY && y < startY + roomHeight)) || ((y == startY || y == startY + roomHeight - 1) && (x >= startX && x < startX + roomWidth))) {
                    expected = "wall";
                } else if (x > startX && x < startX + roomWidth - 1 && y > startY && y < startY + roomHeight - 1) {
                    expected = "floor";
                } else {
                    expected = "default";
                }
                String actual = level.getTile(x, y);
                assertEquals(expected, actual, String.format("Expected <%s> at (%d, %d), but got <%s>", expected, x, y, actual));
            }
        }
    }
}
