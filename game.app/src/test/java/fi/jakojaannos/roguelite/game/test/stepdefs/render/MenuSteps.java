package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.val;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.AssertUI.assertUI;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuSteps {
    public static final String TITLE_LABEL_NAME = "title_label";

    @Given("the main menu has just loaded")
    public void the_main_menu_has_just_loaded() {
        state = new MainMenuGameState(World.createNew(EntityManager.createNew(256, 32)),
                                      game.getTime());
    }

    @When("the player clicks the {string} button")
    public void the_player_clicks_the_button(String string) {
        val userInterface = gameRenderer.getUserInterfaceForState(state);
        val buttonCenter = userInterface.findElementsWithMatchingProperty(UIProperty.TEXT, text -> text.equals(string))
                                        .findFirst()
                                        .flatMap(element -> element.getProperty(UIProperty.CENTER))
                                        .orElseThrow();

        val mouse = state.getWorld().getOrCreateResource(Mouse.class);
        mouse.position.set(buttonCenter)
                      .mul(1.0 / gameRenderer.getCamera().getViewport().getWidthInPixels(),
                           1.0 / gameRenderer.getCamera().getViewport().getHeightInPixels());
        mouse.clicked = true;
        simulateTick();

        mouse.clicked = false;
        simulateTick();
    }

    @Then("there is a title with text {string}")
    public void there_is_a_title_with_text(String string) {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithName(TITLE_LABEL_NAME)
                .isLabel()
                .hasText().equalTo(string);
    }

    @Then("there is a button with text {string}")
    public void there_is_a_button_with_text(String string) {
        assertUI(gameRenderer.getUserInterfaceForState(state))
                .hasExactlyOneElementWithMatchingChild(
                        child -> child.isLabel()
                                      .hasText().equalTo(string));
    }

    @Then("the game should close")
    public void the_game_should_close() {
        simulateTick();
        assertTrue(game.isFinished());
    }

    @Then("the game should start")
    public void the_game_should_start() {
        simulateTick();
        assertTrue(state instanceof GameplayGameState);
    }
}
