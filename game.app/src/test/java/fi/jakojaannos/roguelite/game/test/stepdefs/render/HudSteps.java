package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import io.cucumber.java.en.Then;

import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.game.test.global.GlobalState;

import static fi.jakojaannos.roguelite.game.test.global.GlobalState.simulation;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HudSteps {
    @Then("the game over splash should be hidden.")
    public void theGameOverSplashShouldBeHidden() {
        simulation.runsSingleTick();

        final var state = GlobalState.renderer.fetchPresentableState();
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .noneMatch(entry -> entry.format.equalsIgnoreCase("game over")));
    }

    @Then("the game over splash should be visible")
    public void theGameOverSplashShouldBeVisible() {
        simulation.runsSingleTick();

        final var state = GlobalState.renderer.fetchPresentableState();
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .anyMatch(entry -> entry.format.equalsIgnoreCase("game over")));
    }

    @Then("the game over splash should have text {string}.")
    public void theGameOverSplashShouldHaveText(String text) {
        simulation.runsSingleTick();

        final var state = GlobalState.renderer.fetchPresentableState();
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .anyMatch(entry -> entry.compileString(state.uiVariables())
                                                        .toLowerCase()
                                                        .contains(text.toLowerCase())));
    }

    @Then("the game over splash should have text {string} and {string}.")
    public void theGameOverSplashShouldHaveTextAnd(String a, String b) {
        simulation.runsSingleTick();

        final var state = GlobalState.renderer.fetchPresentableState();
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .anyMatch(entry -> entry.compileString(state.uiVariables())
                                                        .toLowerCase()
                                                        .contains(a.toLowerCase())));
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .anyMatch(entry -> entry.compileString(state.uiVariables())
                                                        .toLowerCase()
                                                        .contains(b.toLowerCase())));
    }

    @Then("the kill counter has text {string}")
    public void theKillCounterHasText(String text) {
        simulation.runsSingleTick();

        final var state = GlobalState.renderer.fetchPresentableState();
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .anyMatch(entry -> entry.compileString(state.uiVariables())
                                                        .toLowerCase()
                                                        .contains(text.toLowerCase())));
    }

    @Then("the timer label reads {string}")
    public void theTimerLabelReads(String expected) {
        simulation.runsSingleTick();

        final var state = GlobalState.renderer.fetchPresentableState();
        assertTrue(StreamSupport.stream(state.textEntries().spliterator(), false)
                                .anyMatch(entry -> entry.compileString(state.uiVariables())
                                                        .toLowerCase()
                                                        .contains(expected.toLowerCase())));
    }
}
