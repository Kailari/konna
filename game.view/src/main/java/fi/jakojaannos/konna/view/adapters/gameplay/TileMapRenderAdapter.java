package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.assets.StaticMesh;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;

public class TileMapRenderAdapter implements EcsSystem<TileMapRenderAdapter.Resources, TileMapRenderAdapter.EntityData, EcsSystem.NoEvents> {
    private final StaticMesh wallMesh;
    private final StaticMesh floorMesh;

    public TileMapRenderAdapter(final AssetManager assetManager) {
        this.wallMesh = assetManager.getStorage(StaticMesh.class)
                                    .getOrDefault("models/wall.fbx");
        this.floorMesh = assetManager.getStorage(StaticMesh.class)
                                     .getOrDefault("models/floor.fbx");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var cameraPosition = resources.cameraProperties.getPosition();

        final var diameter = 50;
        final var radius = diameter / 2.0f;
        final var regionX = (int) Math.floor(cameraPosition.x - radius);
        final var regionY = (int) Math.floor(cameraPosition.y - radius);
        final var regionW = (int) Math.ceil(diameter);
        final var regionH = (int) Math.ceil(diameter);

        entities.forEach(entity -> {
            final var level = entity.getData().tileMapLayer;

            for (int x = regionX; x < regionX + regionW; ++x) {
                for (int y = regionY; y < regionY + regionH; ++y) {
                    final var tile = level.tileMap.getTile(x, y);

                    if (tile.typeIndex() == 0) {
                        continue;
                    }

                    resources.renderer.mesh()
                                      .drawStatic(new Transform(x, y),
                                                  tile.solid() ? this.wallMesh : this.floorMesh);
                }
            }
        });
    }

    public static record Resources(
            Renderer renderer,
            CameraProperties cameraProperties
    ) {}

    public static record EntityData(
            TileMapLayer tileMapLayer
    ) {}
}
