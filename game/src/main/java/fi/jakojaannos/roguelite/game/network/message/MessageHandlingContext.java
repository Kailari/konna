package fi.jakojaannos.roguelite.game.network.message;

import fi.jakojaannos.roguelite.game.network.MainThreadTask;
import lombok.Value;

import java.util.function.Consumer;

@Value
public class MessageHandlingContext {
    Consumer<MainThreadTask> mainThread;
}
