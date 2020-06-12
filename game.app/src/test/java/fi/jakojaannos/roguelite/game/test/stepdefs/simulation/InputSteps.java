package fi.jakojaannos.roguelite.game.test.stepdefs.simulation;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import fi.jakojaannos.roguelite.engine.input.ButtonInput;
import fi.jakojaannos.roguelite.engine.input.InputButton;
import fi.jakojaannos.roguelite.game.data.resources.Inputs;
import fi.jakojaannos.roguelite.game.test.global.GlobalGameState;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulation;

public class InputSteps {
    @Given("the player has held {string} for {double} seconds")
    public void the_player_has_held_key_for_seconds(String key, double seconds) {
        GlobalGameState.updatePlayerPositionBeforeRun();
        pressKey(key);
        simulation.runsForSeconds(seconds);
    }

    @Given("the player has released the key {string}")
    public void the_player_has_released_the_key(String key) {
        releaseKey(key);
    }

    @When("player presses key {string}")
    public void player_presses_key(String key) {
        pressKey(key);
    }

    @When("player releases key {string}")
    public void player_releases_key(String key) {
        releaseKey(key);
    }

    @When("player does nothing")
    public void player_does_nothing() {
        final var inputs = simulation.state().world().fetchResource(Inputs.class);
        inputs.inputAttack = false;
        inputs.inputDown = false;
        inputs.inputLeft = false;
        inputs.inputRight = false;
        inputs.inputUp = false;
        simulation.inputQueue().clear();
    }


    private void pressKey(String key) {
        simulation.inputQueue()
                  .offer(ButtonInput.pressed(InputButton.Keyboard.valueOf("KEY_" + key.toUpperCase())));
    }

    private void releaseKey(String key) {
        simulation.inputQueue()
                  .offer(ButtonInput.released(InputButton.Keyboard.valueOf("KEY_" + key.toUpperCase())));
    }
}
