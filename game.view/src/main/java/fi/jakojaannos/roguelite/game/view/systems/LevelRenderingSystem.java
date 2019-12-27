package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLTexture;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import lombok.val;

import java.nio.file.Path;
import java.util.stream.Stream;

public class LevelRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.withComponent(TileMapLayer.class);
    }

    private final LWJGLCamera camera;
    private final SpriteRegistry spriteRegistry;
    private final SpriteBatch batch;

    public LevelRenderingSystem(
            final Path assetRoot,
            final LWJGLCamera camera,
            final SpriteRegistry spritesRegistry
    ) {
        this.camera = camera;
        spriteRegistry = spritesRegistry;
        this.batch = new LWJGLSpriteBatch(assetRoot, "sprite");
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val camera = world.getEntityManager()
                          .getComponentOf(world.getOrCreateResource(CameraProperties.class).cameraEntity,
                                          Camera.class)
                          .orElseThrow();

        val regionX = (int) Math.floor(camera.pos.x - this.camera.getViewportWidthInUnits() / 2.0);
        val regionY = (int) Math.floor(camera.pos.y - this.camera.getViewportHeightInUnits() / 2.0);
        val regionW = (int) Math.ceil(this.camera.getViewportWidthInUnits()) + 1;
        val regionH = (int) Math.ceil(this.camera.getViewportHeightInUnits()) + 1;

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
