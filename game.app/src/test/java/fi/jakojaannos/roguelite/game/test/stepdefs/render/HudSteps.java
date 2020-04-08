package fi.jakojaannos.roguelite.game.test.stepdefs.render;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.joml.Vector2d;
import org.joml.Vector2i;

import java.util.Collections;
import java.util.stream.Collectors;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import fi.jakojaannos.roguelite.game.view.gamemode.GameplayGameModeRenderer;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.AssertUI.assertUI;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.PositionMatcherBuilder.isHorizontallyIn;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.ui.PositionMatcherBuilder.isVerticallyIn;
import static fi.jakojaannos.roguelite.game.test.global.GlobalState.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HudSteps {
    public static final String GAME_OVER_CONTAINER_NAME = "game-over-container";
    private static final String TIMER_LABEL_NAME = GameplayGameModeRenderer.TIME_PLAYED_LABEL_NAME;
    private static final double HEALTHBAR_NEAR_THRESHOLD = 2.0; // World units

    @Given("no enemies have taken damage")
    public void noEnemiesHaveTakenDamage() {
        // NO-OP
    }

    @Given("one enemy has taken damage recently")
    public void oneEnemyHasTakenDamageRecently() {
        dealDamageToNumberOfEnemies(1, timeManager.convertToTicks(0.5));
    }

    @Given("{int} enemies have taken damage {int} seconds ago")
    public void enemiesHaveTakenDamageSecondsAgo(int n, int seconds) {
        dealDamageToNumberOfEnemies(n, timeManager.convertToTicks(seconds));
    }

    @Given("{int} enemies have taken damage recently")
    public void enemiesHaveTakenDamageRecently(int n) {
        dealDamageToNumberOfEnemies(n, timeManager.convertToTicks(0.5));
    }

    protected void dealDamageToNumberOfEnemies(final int n, final long ticksSinceDamaged) {
        final var healths = state.world()
                                 .getEntityManager()
                                 .getEntitiesWith(Health.class)
                                 .map(EntityManager.EntityComponentPair::component)
                                 .collect(Collectors.toList());

        Collections.shuffle(healths, random);
        healths.stream()
               .limit(n)
               .forEach(health -> {
                   health.currentHealth -= 1.0;
                   health.lastDamageInstanceTimeStamp = timeManager.getCurrentGameTime() - ticksSinceDamaged;
               });
    }

    @Then("there is a timer label on the top-middle of the screen")
    public void thereIsATimerLabelOnTheTopMiddleOfTheScreen() {
        final var ui = gameRenderer.getUserInterfaceForMode(state);
        assertUI(ui)
                .hasExactlyOneElement(that -> that.hasName().equalTo(TIMER_LABEL_NAME)
                                                  .matching(isVerticallyIn(ui).min())
                                                  .matching(isHorizontallyIn(ui).middle())
                                                  .isLabel());
    }

    @And("the timer label reads {string}")
    public void theTimerLabelReads(String expected) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo(TIMER_LABEL_NAME)
                                                  .isLabel()
                                                  .hasText().equalTo(expected));
    }

    @Then("the game over splash should be hidden.")
    public void theGameOverSplashShouldBeHidden() {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo(GAME_OVER_CONTAINER_NAME)
                                                  .isHidden());
    }

    @Then("the game over splash should be visible")
    public void theGameOverSplashShouldBeVisible() {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo(GAME_OVER_CONTAINER_NAME)
                                                  .isVisible());
    }

    @Then("the game over splash should have text {string}.")
    public void theGameOverSplashShouldHaveText(String text) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo(GAME_OVER_CONTAINER_NAME)
                                                  .hasChildMatching(child -> child.isLabel()
                                                                                  .hasText().equalTo(text)));
    }

    @Then("the game over splash should have text {string} and {string}.")
    public void theGameOverSplashShouldHaveTextAnd(String a, String b) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo(GAME_OVER_CONTAINER_NAME)
                                                  .hasChildMatching(child -> child.isLabel()
                                                                                  .hasText().whichContains(a, b)));
    }

    @Then("the kill counter has text {string}")
    public void theKillCounterHasText(String text) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().equalTo("score-kills")
                                                  .isLabel()
                                                  .hasText().whichContains(text));
    }

    @Then("there should be no health-bars rendered")
    public void thereShouldBeNoHealthBarsRendered() {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasNoElementMatching(that -> that.hasName().whichStartsWith("healthbar")
                                                  .isProgressBar());
    }

    @Then("there should be one health-bar visible")
    public void thereShouldBeOneHealthBarVisible() {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasExactlyOneElement(that -> that.hasName().whichStartsWith("healthbar")
                                                  .isVisible()
                                                  .isProgressBar());
    }

    @Then("there should be {int} health-bars visible")
    public void thereShouldBeHealthBarsVisible(int n) {
        assertUI(gameRenderer.getUserInterfaceForMode(state))
                .hasElements(n, that -> that.hasName().whichStartsWith("healthbar")
                                            .isVisible()
                                            .isProgressBar());
    }

    @Then("the health-bar should be close to the damaged enemy")
    public void theHealthBarShouldBeCloseToTheDamagedEnemy() {
        assertTrue(gameRenderer.getUserInterfaceForMode(state)
                               .findElements(that -> that.hasName().whichStartsWith("healthbar")
                                                         .isVisible()
                                                         .isProgressBar())
                               .allMatch(this::projectedPositionIsNearDamagedEnemy));
    }

    @Then("each health-bar should be close to a damaged enemy")
    public void eachHealthBarShouldBeCloseToADamagedEnemy() {
        assertTrue(gameRenderer.getUserInterfaceForMode(state)
                               .findElements(that -> that.hasName().whichStartsWith("healthbar")
                                                         .isVisible()
                                                         .isProgressBar())
                               .allMatch(this::projectedPositionIsNearDamagedEnemy));
    }

    private boolean projectedPositionIsNearDamagedEnemy(final UIElement element) {
        return state.world()
                    .getEntityManager()
                    .getEntitiesWith(Health.class)
                    .filter(pair -> pair.component().currentHealth < pair.component().maxHealth)
                    .map(EntityManager.EntityComponentPair::entity)
                    .map(entity -> state.world().getEntityManager().getComponentOf(entity, Transform.class).orElseThrow().position)
                    .anyMatch(enemyPosition -> isNearPosition(element, enemyPosition, HEALTHBAR_NEAR_THRESHOLD));
    }

    private boolean isNearPosition(
            final UIElement uiElement,
            final Vector2d enemyPosition,
            final double epsilon
    ) {
        return uiElement.getProperty(UIProperty.CENTER)
                        .map(this::projectScreenToWorld)
                        .orElseThrow()
                        .distance(enemyPosition) < epsilon;
    }

    private Vector2d projectScreenToWorld(final Vector2i position) {
        final var camera = gameRenderer.getCamera();
        final var viewport = camera.getViewport();
        return new Vector2d(((position.x / (double) viewport.getWidthInPixels()) * camera.getVisibleAreaWidth()) - camera.getVisibleAreaWidth() / 2.0,
                            ((position.y / (double) viewport.getHeightInPixels()) * camera.getVisibleAreaHeight()) - camera.getVisibleAreaHeight() / 2.0);
    }
}
