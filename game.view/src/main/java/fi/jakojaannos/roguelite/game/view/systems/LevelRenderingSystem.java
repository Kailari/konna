package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import lombok.val;

import java.nio.file.Path;
import java.util.stream.Stream;

public class LevelRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.LEVEL)
                    .withComponent(TileMapLayer.class);
    }

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
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val cameraTransform = world.getEntityManager()
                                   .getComponentOf(world.getOrCreateResource(CameraProperties.class).cameraEntity,
                                                   Transform.class)
                                   .orElseThrow();

        val regionX = (int) Math.floor(cameraTransform.position.x - this.camera.getVisibleAreaWidth() / 2.0);
        val regionY = (int) Math.floor(cameraTransform.position.y - this.camera.getVisibleAreaHeight() / 2.0);
        val regionW = (int) Math.ceil(this.camera.getVisibleAreaWidth()) + 1;
        val regionH = (int) Math.ceil(this.camera.getVisibleAreaHeight()) + 1;

        this.camera.useWorldCoordinates();
        this.batch.begin();
        val sprite = this.spriteRegistry.getByAssetName("sprites/tileset");
        entities.forEach(entity -> {
            val level = world.getEntityManager().getComponentOf(entity, TileMapLayer.class).orElseThrow();

            val tileSize = 1.0;
            for (int x = regionX; x < regionX + regionW; ++x) {
                for (int y = regionY; y < regionY + regionH; ++y) {
                    val tile = level.tileMap.getTile(x, y);
                    this.batch.draw(sprite,
                                    "default",
                                    tile.getTypeIndex(),
                                    x * tileSize,
                                    y * tileSize,
                                    tileSize,
                                    tileSize);
                }
            }
        });
        this.batch.end();
    }
}
