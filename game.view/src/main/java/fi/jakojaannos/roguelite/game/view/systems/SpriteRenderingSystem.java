package fi.jakojaannos.roguelite.game.view.systems;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2d;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;

@Slf4j
public class SpriteRenderingSystem implements ECSSystem, AutoCloseable {
    private static final Vector2d ZERO_VECTOR = new Vector2d(0.0);
    private final Camera camera;
    private final AssetRegistry<Sprite> spriteRegistry;
    private final SpriteBatch spriteBatch;

    public SpriteRenderingSystem(
            final Path assetRoot,
            final Camera camera,
            final AssetRegistry<Sprite> spriteRegistry,
            final RenderingBackend backend
    ) {
        this.camera = camera;
        this.spriteRegistry = spriteRegistry;

        this.spriteBatch = backend.createSpriteBatch(assetRoot, "sprite");
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.ENTITIES)
                    .withComponent(Transform.class)
                    .withComponent(SpriteInfo.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        // Render using two-pass approach. By using correct data-structures with sensible estimates
        // for the initial capacity, the time complexity should be quite close to O(n). The process
        // simply put as follows:
        //  1. Gather required context on all entities we are going to render. While collecting
        //     the entities do:
        //      a. Group collected entities by z-layer
        //      b. Group collected entities by texture
        //  2. Render layers in ascending order using SpriteBatch
        //
        // Current implementation is quick 'n dumb unholy mess of streams and hash maps, which in
        // turn is, most likely very, very inefficient, both memory- and CPU -wise.
        final var renderQueue = new HashMap<Integer, HashMap<Texture, List<SpriteRenderEntry>>>();
        entities.forEach(
                entity -> {
                    final var transform = world.getEntityManager().getComponentOf(entity, Transform.class)
                                               .orElseThrow();
                    final var info = world.getEntityManager().getComponentOf(entity, SpriteInfo.class).orElseThrow();
                    if (info.spriteName.equals("sprites/turret")) {
                        return;
                    }

                    final var texturesForZLayer = renderQueue.computeIfAbsent(info.zLayer,
                                                                              zLayer -> new HashMap<>());
                    final var sprite = this.spriteRegistry.getByAssetName(info.spriteName);
                    final var texture = sprite.getSpecificFrame(info.animationName, info.getCurrentFrame())
                                              .getTexture();
                    final var spritesForTexture = texturesForZLayer.computeIfAbsent(texture,
                                                                                    tex -> new ArrayList<>());

                    final var maybeCollider = world.getEntityManager().getComponentOf(entity, Collider.class);
                    final var origin = maybeCollider.map(collider -> collider.origin)
                                                    .orElse(ZERO_VECTOR);
                    final var size = maybeCollider.map(collider -> new Vector2d(collider.width,
                                                                                collider.height))
                                                  .orElse(ZERO_VECTOR);
                    final var position = transform.position;

                    spritesForTexture.add(new SpriteRenderEntry(sprite,
                                                                info.animationName,
                                                                info.getCurrentFrame(),
                                                                info.zLayer,
                                                                position.x,
                                                                position.y,
                                                                origin.x,
                                                                origin.y,
                                                                size.x,
                                                                size.y,
                                                                transform.rotation));
                }
        );

        this.camera.useWorldCoordinates();
        this.spriteBatch.begin();
        renderQueue.keySet()
                   .stream()
                   .sorted()
                   .map(renderQueue::get)
                   .forEach(spritesForTexture ->
                                    spritesForTexture.forEach(
                                            (texture, entries) ->
                                                    entries.forEach(entry -> this.spriteBatch.draw(entry.getSprite(),
                                                                                                   entry.getAnimation(),
                                                                                                   entry.getFrame(),
                                                                                                   entry.getX(),
                                                                                                   entry.getY(),
                                                                                                   entry.getOriginX(),
                                                                                                   entry.getOriginY(),
                                                                                                   entry.getWidth(),
                                                                                                   entry.getHeight(),
                                                                                                   entry.getRotation())
                                                    )));
        this.spriteBatch.end();
    }

    @Override
    public void close() throws Exception {
        this.spriteBatch.close();
    }

    @Value
    private static class SpriteRenderEntry {
        Sprite sprite;
        String animation;
        int frame;
        int zLayer;
        double x;
        double y;
        double originX;
        double originY;
        double width;
        double height;
        double rotation;
    }
}
