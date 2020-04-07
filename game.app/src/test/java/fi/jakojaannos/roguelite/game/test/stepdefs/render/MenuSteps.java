package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.game.state.GameplayGameMode;
import fi.jakojaannos.roguelite.game.state.MainMenuGameMode;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.AssertUI.assertUI;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuSteps {
    public static final String TITLE_LABEL_NAME = "title_label";

    @Given("the main menu has just loaded")
    public void the_main_menu_has_just_loaded() {
        state = new MainMenuGameMode();
        state.world().provideResource(Events.class, events);
        simulateTick();
    }

    @When("the player clicks the {string} button")
    public void the_player_clicks_the_button(String string) {
        final var userInterface = gameRenderer.getUserInterfaceForMode(state);
        final var buttonCenter = userInterface.findElementsWithMatchingProperty(UIProperty.TEXT, text -> text.equals(string))
                                              .findFirst()
                                              .flatMap(element -> element.getProperty(UIProperty.CENTER))
                                              .orElseThrow();

        final var mouse = state.world().getOrCreateResource(Mouse.class);
        mouse.position.set(buttonCenter)
                      .mul(1.0 / gameRenderer.getCamera().getViewport().getWidthInPixels(),
                           1.0 / gameRenderer.getCamera().getViewport().getHeightInPixels());
        mouse.clicked = false;
        simulateTick();
        renderTick();

        mouse.clicked = true;
        simulateTick();
        renderTick();

        mouse.clicked = false;
        simulateTick();
        renderTick();
    }

    @Then("there is a title with text {string}")
    public void there_is_a_title_with_text(String string) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo(TITLE_LABEL_NAME)
                                                  .isLabel()
                                                  .hasText().equalTo(string));
    }

    @Then("there is a button with text {string}")
    public void there_is_a_button_with_text(String string) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasChildMatching(child -> child.isLabel()
                                                                                  .hasText().equalTo(string)));
    }

    @Then("the game should close")
    public void the_game_should_close() {
        simulateTick();
        assertTrue(game.isFinished());
    }

    @Then("the game should start")
    public void the_game_should_start() {
        simulateTick();
        assertTrue(state instanceof GameplayGameMode);
    }

    @Then("the game is not in the main menu")
    public void theGameIsNotInTheMainMenu() {
        simulateTick();
        assertFalse(state instanceof MainMenuGameMode);
    }

    @Then("the game is now in the main menu")
    public void theGameIsNowInTheMainMenu() {
        simulateTick();
        assertTrue(state instanceof MainMenuGameMode);
    }
}
