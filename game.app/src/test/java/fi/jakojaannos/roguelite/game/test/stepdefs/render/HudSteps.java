package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import fi.jakojaannos.roguelite.game.view.state.GameplayGameStateRenderer;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.AssertUI.assertUI;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.gameRenderer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.state;

public class HudSteps {
    private static final String TIMER_LABEL_NAME = GameplayGameStateRenderer.TIME_PLAYED_LABEL_NAME;
    public static final String GAME_OVER_CONTAINER_NAME = "game-over-container";

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

    @Then("the game over splash should be hidden.")
    public void theGameOverSplashShouldBeHidden() {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(GAME_OVER_CONTAINER_NAME)
                .isHidden();
    }

    @Then("the game over splash should be visible")
    public void theGameOverSplashShouldBeVisible() {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(GAME_OVER_CONTAINER_NAME)
                .isVisible();
    }

    @Then("the game over splash should have text {string}.")
    public void theGameOverSplashShouldHaveText(String text) {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(GAME_OVER_CONTAINER_NAME)
                .hasChildMatching(child -> child
                        .isLabel()
                        .hasText().equalTo(text));
    }

    @Then("the game over splash should have text {string} and {string}.")
    public void theGameOverSplashShouldHaveTextAnd(String a, String b) {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(GAME_OVER_CONTAINER_NAME)
                .hasChildMatching(child -> child
                        .isLabel()
                        .hasText().whichContains(a, b));
    }

    @Then("the kill counter has text {string}")
    public void theKillCounterHasText(String text) {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName("score-kills")
                .isLabel()
                .hasText().whichContains(text);
    }
}
