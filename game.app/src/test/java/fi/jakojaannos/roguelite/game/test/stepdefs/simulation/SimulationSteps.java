package fi.jakojaannos.roguelite.game.test.stepdefs.simulation;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.joml.Vector2d;

import fi.jakojaannos.riista.GameRunnerTimeManager;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGameWithGameModeAndRenderer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalGameState.*;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.renderer;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulation;

public class SimulationSteps {
    @Given("the game world just finished loading")
    public void the_game_world_just_finished_loading() {
        simulation = whenGameWithGameModeAndRenderer(GameplayGameMode.create(6969, new GameRunnerTimeManager(20L)),
                                                     renderer);

        playerInitialPosition = getLocalPlayer().flatMap(player -> player.getComponent(Transform.class))
                                                .map(transform -> new Vector2d(transform.position))
                                                .orElseThrow();
        playerPositionBeforeRun = new Vector2d(playerInitialPosition);
    }

    @Given("the current game time is at {double} seconds")
    public void theCurrentGameTimeIsAtSeconds(double seconds) {
        simulation.setCurrentTickAsSeconds(seconds);
    }

    @Given("the game has run for {double} seconds")
    public void the_game_has_run_for_seconds(double seconds) {
        updatePlayerPositionBeforeRun();
        simulation.runsForSeconds(seconds);
    }

    @Given("the game has run for a single tick")
    public void the_game_has_run_for_tick() {
        updatePlayerPositionBeforeRun();
        simulation.runsSingleTick();
    }

    @When("the game runs for a/1 second")
    public void the_game_runs_for_a_second() {
        updatePlayerPositionBeforeRun();
        simulation.runsForSeconds(1);
    }

    @When("the game runs for {double} seconds")
    public void the_game_runs_for_x_seconds(double seconds) {
        updatePlayerPositionBeforeRun();
        simulation.runsForSeconds(seconds);
    }

    @When("the game runs for a single tick")
    public void the_game_runs_for_a_single_tick() {
        updatePlayerPositionBeforeRun();
        simulation.runsSingleTick();
    }
}
