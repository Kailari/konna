package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.state;

public class MenuSteps {

    @Given("the main menu has just loaded")
    public void the_main_menu_has_just_loaded() {
        state = null;
        throw new io.cucumber.java.PendingException();
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
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("the game should start")
    public void the_game_should_start() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
}
