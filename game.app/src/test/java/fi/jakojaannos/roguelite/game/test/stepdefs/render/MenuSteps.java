package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import fi.jakojaannos.roguelite.game.state.MainMenuGameState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuSteps {

    @Given("the main menu has just loaded")
    public void the_main_menu_has_just_loaded() {
        state = new MainMenuGameState(World.createNew(EntityManager.createNew(256, 32)),
                                      game.getTime());
    }

    @When("the player clicks the {string} button")
    public void the_player_clicks_the_button(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("there is a title with text {string}")
    public void there_is_a_title_with_text(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("there is a button with text {string}")
    public void there_is_a_button_with_text(String string) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
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
