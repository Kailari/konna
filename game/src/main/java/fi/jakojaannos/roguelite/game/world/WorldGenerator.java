package fi.jakojaannos.roguelite.game.world;

import org.joml.Vector2d;

import java.util.Random;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.ObstacleArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.StalkerArchetype;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;

public class WorldGenerator<TTile> {
    private final TileMap<TTile> tileMap;
    private final Random random = new Random();

    public TileMap<TTile> getTileMap() {
        return this.tileMap;
    }

    public WorldGenerator(final TTile defaultTile) {
        this.tileMap = new TileMap<>(defaultTile);
    }

    public void prepareInitialRoom(
            final long seed,
            final World world,
            final TTile floor,
            final TTile wall,
            final int mainRoomSizeMin,
            final int mainRoomSizeMax,
            final int hallwayLength,
            final int hallwaySize,
            final int hallwaysPerWall
    ) {
        this.random.setSeed(seed);

        final var mainRoomWidth = mainRoomSizeMin + this.random.nextInt(mainRoomSizeMax - mainRoomSizeMin);
        final var mainRoomHeight = mainRoomSizeMin + this.random.nextInt(mainRoomSizeMax - mainRoomSizeMin);
        final var startX = -mainRoomWidth / 2;
        final var startY = -mainRoomHeight / 2;

        final var unitsPerHallwayHorizontal = mainRoomWidth / hallwaysPerWall;
        final var unitsPerHallwayVertical = mainRoomHeight / hallwaysPerWall;

        final var horizontalHallwayOffset = unitsPerHallwayHorizontal - hallwaySize;
        final var verticalHallwayOffset = unitsPerHallwayVertical - hallwaySize;

        // Generate main room
        for (int ix = 0; ix < mainRoomWidth; ++ix) {
            for (int iy = 0; iy < mainRoomHeight; ++iy) {
                final var x = startX + ix;
                final var y = startY + iy;

                final var isVerticalEdge = ix == 0 || ix == mainRoomWidth - 1;
                final var isHorizontalEdge = iy == 0 || iy == mainRoomHeight - 1;
                final var isHorizontallyInDoor =
                        ((ix + horizontalHallwayOffset) % unitsPerHallwayHorizontal) < hallwaySize;
                final var isVerticallyInDoor =
                        ((iy + verticalHallwayOffset) % unitsPerHallwayVertical) < hallwaySize;
                final var isHorizontalHallwayDoor = (isHorizontalEdge && isHorizontallyInDoor);
                final var isVerticalHallwayDoor = (isVerticalEdge && isVerticallyInDoor);

                final var isVerticalWall = isVerticalEdge && !isVerticalHallwayDoor;
                final var isHorizontalWall = isHorizontalEdge && !isHorizontalHallwayDoor;

                final TTile tileType = (isVerticalWall || isHorizontalWall)
                        ? wall
                        : floor;

                this.tileMap.setTile(x, y, tileType);
            }
        }

        // Generate hallways
        final var timeManager = world.fetchResource(TimeManager.class);
        for (int i = 0; i < hallwaysPerWall; ++i) {
            final var hallwayStartX = startX + hallwaySize + i * unitsPerHallwayHorizontal;
            final var hallwayStartY = startY + hallwaySize + i * unitsPerHallwayVertical;
            for (int ix = -1; ix <= hallwaySize; ++ix) {
                for (int iy = 0; iy <= hallwayLength; ++iy) {
                    final TTile tileType = ix == -1 || ix == hallwaySize || iy == hallwayLength ? wall : floor;

                    this.tileMap.setTile(hallwayStartX + ix, startY - iy - 1, tileType);
                    this.tileMap.setTile(hallwayStartX + ix, startY + mainRoomHeight + iy, tileType);

                    this.tileMap.setTile(startX - iy - 1, hallwayStartY + ix, tileType);
                    this.tileMap.setTile(startX + mainRoomWidth + iy, hallwayStartY + ix, tileType);
                }
            }

            final var spawnerXH = hallwayStartX + hallwaySize / 2;
            final var spawnerYH = hallwayLength - 2;
            final var stalkerFreq = 8.5;
            final var followerFreq = 5.25;
            final var slimeFrequency = 15;

            final var followerFactory =
                    SpawnerComponent.EntityFactory.withRandomDistance(FollowerArchetype::spawnFollower);
            final var stalkerFactory =
                    SpawnerComponent.EntityFactory.withRandomDistance(StalkerArchetype::spawnStalker);

            createSpawner(spawnerXH - 1, startY - spawnerYH - 1, stalkerFreq, world, stalkerFactory, timeManager);
            createSpawner(spawnerXH + 1, startY - spawnerYH - 1, followerFreq, world, followerFactory, timeManager);
            createSpawner(spawnerXH - 1, startY + mainRoomHeight + spawnerYH, stalkerFreq, world, stalkerFactory, timeManager);
            createSpawner(spawnerXH + 1, startY + mainRoomHeight + spawnerYH, followerFreq, world, followerFactory, timeManager);
            createSpawner(spawnerXH, startY - spawnerYH, slimeFrequency, world, SlimeArchetype::createLargeSlime, timeManager);

            final var spawnerXV = hallwayLength - 2;
            final var spawnerYV = hallwayStartY + hallwaySize / 2;

            createSpawner(startX - spawnerXV - 1, spawnerYV - 1, stalkerFreq, world, stalkerFactory, timeManager);
            createSpawner(startX - spawnerXV - 1, spawnerYV + 1, followerFreq, world, followerFactory, timeManager);
            createSpawner(startX + mainRoomWidth + spawnerXV, spawnerYV - 1, stalkerFreq, world, stalkerFactory, timeManager);
            createSpawner(startX + mainRoomWidth + spawnerXV, spawnerYV + 1, followerFreq, world, followerFactory, timeManager);
        }

        final var nObstacles = 10;
        final var obstacleMaxSize = 2.0;
        final var obstacleMinSize = 1.0;
        for (int i = 0; i < nObstacles; ++i) {
            final var size = obstacleMinSize + (obstacleMaxSize - obstacleMinSize) * this.random.nextDouble();
            double x, y;
            do {
                x = startX + this.random.nextDouble() * (mainRoomWidth - size);
                y = startY + this.random.nextDouble() * (mainRoomHeight - size);
            } while (Vector2d.distance(0, 0, x, y) < 4.0);
            ObstacleArchetype.create(world, new Transform(x, y), size);
        }
    }

    private void createSpawner(
            final int x,
            final int y,
            final double timeBetweenSpawns,
            final Entities entities,
            final SpawnerComponent.EntityFactory factory,
            final TimeManager timeManager
    ) {
        final var spawnerComponent = new SpawnerComponent(timeManager.convertToTicks(timeBetweenSpawns), factory);
        spawnerComponent.maxSpawnDistance = 0.25;
        entities.createEntity(new Transform(x, y), spawnerComponent);
    }
}
