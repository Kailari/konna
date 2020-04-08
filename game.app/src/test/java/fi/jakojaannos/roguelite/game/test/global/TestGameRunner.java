package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;

public class TestGameRunner extends GameRunner {
    public TestGameRunner(final TestTimeManager timeManager) {
        super(timeManager);
    }

    @Override
    protected boolean shouldContinueLoop() {
        return false;
    }

    @Override
    protected void onStateChange(final GameState state) {
    }

    @Override
    protected void onModeChange(final GameMode gameMode) {
        GlobalState.gameRenderer.changeGameMode(gameMode);
    }
}
