package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ui.TextSizeProvider;
import fi.jakojaannos.roguelite.engine.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import lombok.Getter;

public abstract class GameState implements WorldProvider {
    @Getter private final World world;
    @Getter private final UserInterface userInterface;

    private final SystemDispatcher dispatcher;

    public GameState(
            final World world,
            final TimeManager timeManager,
            final UserInterface.ViewportSizeProvider viewportSizeProvider,
            final TextSizeProvider textSizeProvider
    ) {
        this.world = world;
        this.world.createResource(Time.class, new Time(timeManager));

        this.userInterface = createUserInterface(viewportSizeProvider, textSizeProvider);
        world.createResource(UserInterface.class, this.userInterface);

        this.dispatcher = createDispatcher();
    }

    protected abstract UserInterface createUserInterface(
            UserInterface.ViewportSizeProvider viewportSizeProvider,
            TextSizeProvider textSizeProvider
    );

    protected abstract SystemDispatcher createDispatcher();

    public void tick() {
        this.dispatcher.dispatch(this.world);
        this.world.getEntityManager().applyModifications();

        // TODO: Update UI
    }
}
