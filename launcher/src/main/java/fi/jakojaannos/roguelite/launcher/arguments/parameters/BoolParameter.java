package fi.jakojaannos.roguelite.launcher.arguments.parameters;

public class BoolParameter extends Parameter<Boolean> {
    BoolParameter(final String name) {
        super(name);
    }

    @Override
    public Boolean parse(final String string) {
        return Boolean.valueOf(string);
    }
}
