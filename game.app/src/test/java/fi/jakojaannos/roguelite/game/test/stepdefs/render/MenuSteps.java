package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import fi.jakojaannos.riista.data.events.UiEvent;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGameWithGameModeAndRenderer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.renderer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulation;
import static org.junit.jupiter.api.Assertions.*;

public class MenuSteps {
    public static final String TITLE_LABEL_NAME = "title_label";

    @Given("the main menu has just loaded")
    public void the_main_menu_has_just_loaded() {
        simulation = whenGameWithGameModeAndRenderer(MainMenuGameMode.create(), renderer);

        simulation.runsSingleTick();
    }

    @When("the player clicks the {string} button")
    public void the_player_clicks_the_button(String string) {
        final var event = switch (string) {
            case "Quit" -> new UiEvent("quit-button", UiEvent.Type.CLICK);
            case "Play" -> new UiEvent("play-button", UiEvent.Type.CLICK);
            default -> throw new AssertionError("Test-case for button \"" + string + "\" is not implemented!");
        };

        simulation.state().world().fetchResource(Events.class).system().fire(event);
        simulation.runsSingleTick();
    }

    @Then("there is a title with text {string}")
    public void there_is_a_title_with_text(String string) {
        //assertUI(gameRenderer.getCurrentUserInterface())
        //        .hasExactlyOneElement(that -> that.hasName().equalTo(TITLE_LABEL_NAME)
        //                                          .isLabel()
        //                                          .hasText().equalTo(string));
    }

    @Then("there is a button with text {string}")
    public void there_is_a_button_with_text(String string) {
        // TODO: Re-write the UI assertions to use presentable state or sth.
        //assertUI(presentableState)
        //        .hasExactlyOneElement(that -> that.hasChildMatching(child -> child.isLabel()
        //                                                                          .hasText().equalTo(string)));
    }

    @Then("the game should close")
    public void the_game_should_close() {
        simulation.runsSingleTick();
        assertTrue(simulation.isTerminated());
    }

    @Then("the game should start")
    public void the_game_should_start() {
        simulation.runsSingleTick();
        assertEquals(GameplayGameMode.GAME_MODE_ID, simulation.mode().id());
    }

    @Then("the game is not in the main menu")
    public void theGameIsNotInTheMainMenu() {
        simulation.runsSingleTick();
        assertNotEquals(MainMenuGameMode.GAME_MODE_ID, simulation.mode().id());
    }

    @Then("the game now proceeds to the main menu after next tick")
    public void theGameNowProceedsToTheMainMenu() {
        simulation.runsSingleTick();
        simulation.runsSingleTick(); // NOTE: It takes two ticks before the events are processed
        assertEquals(MainMenuGameMode.GAME_MODE_ID, simulation.mode().id());
    }
}
