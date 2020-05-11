package fi.jakojaannos.roguelite.game.weapons;

/**
 * Phases for executing weapon events in modules. Events are executed in the order: check, trigger, post. Check phase
 * should be used for checking if certain actions can be performed, without changing the weapon's state. Trigger phase
 * should be used to execute that action, and post phase for reacting to it or cleaning up.
 * <p>
 * Events can be cancelled only in check phase. If none of the modules cancels the event during it, then the trigger
 * phase is executed, and after that post phase is executed.
 */
public enum Phase {
    /**
     * Check phase should be used to check if certain actions can be performed. This phase should not make any changes
     * to the weapon's state.
     * <p>
     * Events can be cancelled during check-phase.
     */
    CHECK,

    /**
     * Trigger phase should be used to execute certain actions. Events <b>can't</b> be cancelled during trigger phase.
     */
    TRIGGER,

    /**
     * Post phase should be used to reacting to executed events or cleaning up. Events <b>can't</b> be cancelled during
     * trigger phase.
     */
    POST
}
