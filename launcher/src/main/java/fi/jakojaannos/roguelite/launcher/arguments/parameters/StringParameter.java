package fi.jakojaannos.roguelite.launcher.arguments.parameters;

public class StringParameter extends Parameter<String> {
    public StringParameter(final String name) {
        super(name);
    }

    @Override
    public String parse(final String string) {
        return string;
    }
}
