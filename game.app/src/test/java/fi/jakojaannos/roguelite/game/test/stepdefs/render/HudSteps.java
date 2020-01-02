package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import fi.jakojaannos.roguelite.game.view.state.GameplayGameStateRenderer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.AssertUI.assertUI;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.gameRenderer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.state;

public class HudSteps {
    private static final String TIMER_LABEL_NAME = GameplayGameStateRenderer.TIME_PLAYED_LABEL_NAME;

    @Then("there is a timer label on the top-middle of the screen")
    public void thereIsATimerLabelOnTheTopMiddleOfTheScreen() {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(TIMER_LABEL_NAME)
                .isVerticallyIn().top()
                //.isHorizontallyIn().middle()
                .isLabel();
    }

    @And("the timer label reads {string}")
    public void theTimerLabelReads(String expected) {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(TIMER_LABEL_NAME)
                .isLabel()
                .hasText().equalTo(expected);
    }
}
