package fi.jakojaannos.roguelite.game.state;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.ui.ProportionValue;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.val;

public class MainMenuGameState extends GameState {
    public MainMenuGameState(
            final World world,
            final TimeManager timeManager,
            final UserInterface.ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider textSizeProvider
    ) {
        super(world, timeManager, viewportSizeProvider, textSizeProvider);
    }

    @Override
    protected SystemDispatcher createDispatcher() {
        return SystemDispatcher
                .builder()
                .build();
    }

    @Override
    protected UserInterface createUserInterface(
            final UserInterface.ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider textSizeProvider
    ) {
        val width = 600;
        val height = 100;
        val borderSize = 25;
        return UserInterface
                .builder(viewportSizeProvider, textSizeProvider)
                .element("play_button",
                         UIElementType.PANEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().parentHeight(0.3))
                                           .width(ProportionValue.absolute(width))
                                           .height(ProportionValue.absolute(height))
                                           .borderSize(borderSize)
                                           .sprite("sprites/ui/ui")
                                           // TODO: panelAnimationName
                                           .child("play_button_label",
                                                  UIElementType.LABEL,
                                                  labelBuilder -> labelBuilder
                                                          .anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                                          .anchorY(ProportionValue.percentOf().parentHeight(0.5))
                                                          .left(ProportionValue.percentOf().ownWidth(-0.5))
                                                          .top(ProportionValue.percentOf().ownHeight(-0.5))
                                                          .text("Play")
                                                          .fontSize(24)))
                .element("title_label",
                         UIElementType.LABEL,
                         builder -> builder.anchorX(ProportionValue.percentOf().parentWidth(0.5))
                                           .anchorY(ProportionValue.percentOf().parentHeight(0.25))
                                           .left(ProportionValue.percentOf().ownWidth(-0.5))
                                           .top(ProportionValue.percentOf().ownHeight(-1.0))
                                           .text("Konna")
                                           .fontSize(48))
                .build();
    }
}
