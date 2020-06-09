package fi.jakojaannos.roguelite.engine.event;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.state.StateEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;

/**
 * TODO: IDEA does not recognize record javadoc properly. Remove whitespace once it is fixed
 *
 * @ param ui       Event bus for receiving events <strong>from</strong> the UI for the game
 * @ param input    Event bus for receiving input events for the game
 * @ param state    Event bus for receiving/sending game state events for the game
 */
public record Events(
        EventReceiver<UIEvent>ui,
        EventReceiver<InputEvent>input,
        EventSender<StateEvent>state,
        EventSender<Object>system
) {
}
