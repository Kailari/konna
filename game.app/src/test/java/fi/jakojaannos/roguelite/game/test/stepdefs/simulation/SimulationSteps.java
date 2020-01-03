package fi.jakojaannos.roguelite.game.test.stepdefs.simulation;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.game.state.GameplayGameState;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.joml.Vector2d;

import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.*;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;

public class SimulationSteps {
    @Given("the game world just finished loading")
    public void the_game_world_just_finished_loading() {
        state = new GameplayGameState(6969,
                                      World.createNew(EntityManager.createNew(256, 32)),
                                      game.getTime());

        playerInitialPosition = getLocalPlayer().flatMap(entity -> getComponentOf(entity, Transform.class))
                                                .map(transform -> new Vector2d(transform.position))
                                                .orElseThrow();
        playerPositionBeforeRun = new Vector2d(playerInitialPosition);
    }

    @Given("the current game time is at {double} seconds")
    public void theCurrentGameTimeIsAtSeconds(double seconds) {
        timeManager.setCurrentTickAsSeconds(seconds);
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
