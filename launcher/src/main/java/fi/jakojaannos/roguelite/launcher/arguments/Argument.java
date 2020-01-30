package fi.jakojaannos.roguelite.launcher.arguments;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Argument {
    private final List<String> aliases;
    private final Action action;

    public static Builder withName(final String name, final String... aliases) {
        final var aliasList = new ArrayList<String>(aliases.length + 1);
        aliasList.add(name);
        aliasList.addAll(Arrays.asList(aliases));
        return new Builder(aliasList);
    }

    boolean nameMatches(final String name) {
        return this.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(name));
    }

    void consumeArguments(final ArgumentParameters params) throws ArgumentParsingException {
        this.action.perform(params);
    }

    public interface Action {
        void perform(ArgumentParameters params) throws ArgumentParsingException;
    }

    @RequiredArgsConstructor
    public static class Builder {
        private final List<String> aliases;

        public Argument withAction(final Action action) {
            return new Argument(this.aliases, action);
        }
    }
}
