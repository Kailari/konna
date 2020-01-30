package fi.jakojaannos.roguelite.game.view.systems;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;

public class LevelRenderingSystem implements ECSSystem {
    private final Camera camera;
    private final AssetRegistry<Sprite> spriteRegistry;
    private final SpriteBatch batch;

    public LevelRenderingSystem(
            final Path assetRoot,
            final Camera camera,
            final AssetRegistry<Sprite> spritesRegistry,
            final RenderingBackend backend
    ) {
        this.camera = camera;
        this.spriteRegistry = spritesRegistry;
        this.batch = backend.createSpriteBatch(assetRoot, "sprite");
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.LEVEL)
                    .withComponent(TileMapLayer.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var cameraTransform = world.getEntityManager()
                                         .getComponentOf(world.getOrCreateResource(CameraProperties.class).cameraEntity,
                                                         Transform.class)
                                         .orElseThrow();

        final var regionX = (int) Math.floor(cameraTransform.position.x - this.camera.getVisibleAreaWidth() / 2.0);
        final var regionY = (int) Math.floor(cameraTransform.position.y - this.camera.getVisibleAreaHeight() / 2.0);
        final var regionW = (int) Math.ceil(this.camera.getVisibleAreaWidth()) + 1;
        final var regionH = (int) Math.ceil(this.camera.getVisibleAreaHeight()) + 1;

        this.camera.useWorldCoordinates();
        this.batch.begin();
        final var sprite = this.spriteRegistry.getByAssetName("sprites/tileset");
        entities.forEach(entity -> {
            final var level = world.getEntityManager().getComponentOf(entity, TileMapLayer.class).orElseThrow();

            final var tileSize = 1.0;
            for (int x = regionX; x < regionX + regionW; ++x) {
                for (int y = regionY; y < regionY + regionH; ++y) {
                    final var tile = level.tileMap.getTile(x, y);
                    this.batch.draw(sprite,
                                    "default",
                                    tile.getTypeIndex(),
                                    Math.floor(x * tileSize),
                                    Math.floor(y * tileSize),
                                    tileSize,
                                    tileSize);
                }
            }
        });
        this.batch.end();
    }
}
