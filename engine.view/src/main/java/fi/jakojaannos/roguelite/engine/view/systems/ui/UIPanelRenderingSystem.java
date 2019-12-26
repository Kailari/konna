package fi.jakojaannos.roguelite.engine.view.systems.ui;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.content.SpriteRegistry;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.panel.BorderSize;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.internal.panel.PanelSprite;
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.Texture;
import fi.jakojaannos.roguelite.engine.view.sprite.Sprite;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.stream.Stream;

@RequiredArgsConstructor
public class UIPanelRenderingSystem<TTexture extends Texture> implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(UISystemGroups.RENDERING)
                    .withComponent(ElementBoundaries.class)
                    .withComponent(PanelSprite.class);
    }

    private static final int DEFAULT_BORDER_SIZE = 5;

    private final SpriteBatch<TTexture> spriteBatch;
    private final SpriteRegistry<TTexture> spriteRegistry;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        this.spriteBatch.begin();
        entities.forEach(entity -> {
            val sprite = entityManager.getComponentOf(entity, PanelSprite.class)
                                      .map(panelSprite -> this.spriteRegistry.getByAssetName(panelSprite.sprite))
                                      .orElseThrow();
            int borderSize = entityManager.getComponentOf(entity, BorderSize.class)
                                          .map(bs -> bs.value)
                                          .orElse(DEFAULT_BORDER_SIZE);

            val boundaries = entityManager.getComponentOf(entity, ElementBoundaries.class).orElseThrow();
            val x = boundaries.minX;
            val y = boundaries.minY;
            val width = boundaries.width;
            val height = boundaries.height;

            drawPanelRow(sprite, 0, x, y, width, borderSize, borderSize);
            drawPanelRow(sprite, 1, x, y + borderSize, width, height - 2 * borderSize, borderSize);
            drawPanelRow(sprite, 2, x, y + height - borderSize, width, borderSize, borderSize);
        });
        this.spriteBatch.end();
    }

    private void drawPanelRow(
            final Sprite<TTexture> sprite,
            final int row,
            final double x,
            final double y,
            final double width,
            final double height,
            final double borderSize
    ) {
        this.spriteBatch.draw(sprite, "panel_normal", row * 3, x, y, borderSize, height);
        this.spriteBatch.draw(sprite, "panel_normal", row * 3 + 1, x + borderSize, y, width - 2 * borderSize, height);
        this.spriteBatch.draw(sprite, "panel_normal", row * 3 + 2, x + width - borderSize, y, borderSize, height);
    }
}
