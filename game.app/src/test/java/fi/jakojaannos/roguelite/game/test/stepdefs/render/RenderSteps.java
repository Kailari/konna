package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import io.cucumber.java.en.When;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.renderTick;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulateTick;

public class RenderSteps {
    @When("the game is rendered")
    public void the_game_is_rendered() {
        renderTick();
    }
}
