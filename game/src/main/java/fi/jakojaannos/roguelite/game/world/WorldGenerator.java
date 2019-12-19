package fi.jakojaannos.roguelite.game.world;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.game.data.archetypes.FollowerArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.ObstacleArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.archetypes.StalkerArchetype;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;

public class WorldGenerator<TTile> {
    private final TileMap<TTile> tileMap;
    private final Random random = new Random();

    public WorldGenerator(TTile defaultTile) {
        tileMap = new TileMap<>(defaultTile);
    }

    public TileMap<TTile> getTileMap() {
        return this.tileMap;
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

        val mainRoomWidth = mainRoomSizeMin + this.random.nextInt(mainRoomSizeMax - mainRoomSizeMin);
        val mainRoomHeight = mainRoomSizeMin + this.random.nextInt(mainRoomSizeMax - mainRoomSizeMin);
        val startX = -mainRoomWidth / 2;
        val startY = -mainRoomHeight / 2;

        val unitsPerHallwayHorizontal = mainRoomWidth / hallwaysPerWall;
        val unitsPerHallwayVertical = mainRoomHeight / hallwaysPerWall;

        val horizontalHallwayOffset = unitsPerHallwayHorizontal - hallwaySize;
        val verticalHallwayOffset = unitsPerHallwayVertical - hallwaySize;

        // Generate main room
        for (int ix = 0; ix < mainRoomWidth; ++ix) {
            for (int iy = 0; iy < mainRoomHeight; ++iy) {
                val x = startX + ix;
                val y = startY + iy;

                val isVerticalEdge = ix == 0 || ix == mainRoomWidth - 1;
                val isHorizontalEdge = iy == 0 || iy == mainRoomHeight - 1;
                val isHorizontalHallwayDoor = (isHorizontalEdge && ((ix + horizontalHallwayOffset) % unitsPerHallwayHorizontal) < hallwaySize);
                val isVerticalHallwayDoor = (isVerticalEdge && ((iy + verticalHallwayOffset) % unitsPerHallwayVertical) < hallwaySize);

                val isVerticalWall = isVerticalEdge && !isVerticalHallwayDoor;
                val isHorizontalWall = isHorizontalEdge && !isHorizontalHallwayDoor;

                final TTile tileType = (isVerticalWall || isHorizontalWall)
                        ? wall
                        : floor;

                this.tileMap.setTile(x, y, tileType);
            }
        }

        // Generate hallways
        val entities = world.getEntityManager();
        for (int i = 0; i < hallwaysPerWall; ++i) {
            val hallwayStartX = startX + hallwaySize + i * unitsPerHallwayHorizontal;
            val hallwayStartY = startY + hallwaySize + i * unitsPerHallwayVertical;
            for (int ix = -1; ix <= hallwaySize; ++ix) {
                for (int iy = 0; iy <= hallwayLength; ++iy) {
                    final TTile tileType = ix == -1 || ix == hallwaySize || iy == hallwayLength ? wall : floor;

                    this.tileMap.setTile(hallwayStartX + ix, startY - iy - 1, tileType);
                    this.tileMap.setTile(hallwayStartX + ix, startY + mainRoomHeight + iy, tileType);

                    this.tileMap.setTile(startX - iy - 1, hallwayStartY + ix, tileType);
                    this.tileMap.setTile(startX + mainRoomWidth + iy, hallwayStartY + ix, tileType);
                }
            }

            val spawnerXH = hallwayStartX + hallwaySize / 2;
            val spawnerYH = hallwayLength - 2;
            val stalkerFrequency = 15.0;
            val followerFrequency = 10;
            val slimeFrequency = 20;

            val followerFactory = SpawnerComponent.EntityFactory.withRandomDistance(FollowerArchetype::spawnFollower);
            val stalkerFactory = SpawnerComponent.EntityFactory.withRandomDistance(StalkerArchetype::spawnStalker);

            createSpawner(spawnerXH - 1, startY - spawnerYH - 1, stalkerFrequency, entities, stalkerFactory);
            createSpawner(spawnerXH + 1, startY - spawnerYH - 1, followerFrequency, entities, followerFactory);
            createSpawner(spawnerXH - 1, startY + mainRoomHeight + spawnerYH, stalkerFrequency, entities, stalkerFactory);
            createSpawner(spawnerXH + 1, startY + mainRoomHeight + spawnerYH, followerFrequency, entities, followerFactory);
            createSpawner(spawnerXH, startY - spawnerYH, slimeFrequency, entities, SlimeArchetype::spawnLargeSlime);

            val spawnerXV = hallwayLength - 2;
            val spawnerYV = hallwayStartY + hallwaySize / 2;

            createSpawner(startX - spawnerXV - 1, spawnerYV - 1, stalkerFrequency, entities, stalkerFactory);
            createSpawner(startX - spawnerXV - 1, spawnerYV + 1, followerFrequency, entities, followerFactory);
            createSpawner(startX + mainRoomWidth + spawnerXV, spawnerYV - 1, stalkerFrequency, entities, stalkerFactory);
            createSpawner(startX + mainRoomWidth + spawnerXV, spawnerYV + 1, followerFrequency, entities, followerFactory);
        }

        val nObstacles = 10;
        val obstacleMaxSize = 2.0;
        val obstacleMinSize = 1.0;
        for (int i = 0; i < nObstacles; ++i) {
            val size = obstacleMinSize + (obstacleMaxSize - obstacleMinSize) * this.random.nextDouble();
            double x, y;
            do {
                x = startX + this.random.nextDouble() * (mainRoomWidth - size);
                y = startY + this.random.nextDouble() * (mainRoomHeight - size);
            } while (Vector2d.distance(0, 0, x, y) < 4.0);
            ObstacleArchetype.create(entities, new Transform(x, y), size);
        }
    }

    private void createSpawner(
            final int x,
            final int y,
            final double spawnFrequency,
            final EntityManager entityManager,
            final SpawnerComponent.EntityFactory factory
    ) {
        val spawner = entityManager.createEntity();
        entityManager.addComponentTo(spawner, new Transform(x, y));
        val spawnerComponent = new SpawnerComponent(spawnFrequency, factory);
        spawnerComponent.maxSpawnDistance = 0.25;
        spawnerComponent.spawnCoolDown = 1.0;
        entityManager.addComponentTo(spawner, spawnerComponent);
    }
}
