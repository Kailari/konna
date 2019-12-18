package fi.jakojaannos.roguelite.game.test.stepdefs.simulation;

import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.joml.Vector2d;

import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.*;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;

public class SimulationSteps {
    @Given("the game world just finished loading")
    public void the_game_world_just_finished_loading() {
        state = Roguelite.createInitialState(6969);
        playerInitialPosition = getLocalPlayer().flatMap(entity -> getComponentOf(entity, Transform.class))
                                                .map(transform -> new Vector2d(transform.position))
                                                .orElseThrow();
        playerPositionBeforeRun = new Vector2d(playerInitialPosition);
    }

    @Given("the game has run for {double} seconds")
    public void the_game_has_run_for_seconds(double seconds) {
        updatePlayerPositionBeforeRun();
        simulateSeconds(seconds);
    }

    @When("the game runs for a/1 second")
    public void the_game_runs_for_a_second() {
        updatePlayerPositionBeforeRun();
        simulateSeconds(1);
    }

    @When("the game runs for {double} seconds")
    public void the_game_runs_for_x_seconds(double seconds) {
        updatePlayerPositionBeforeRun();
        simulateSeconds(seconds);
    }

    @When("the game runs for a single tick")
    public void the_game_runs_for_a_single_tick() {
        updatePlayerPositionBeforeRun();
        simulateTick();
    }
}
