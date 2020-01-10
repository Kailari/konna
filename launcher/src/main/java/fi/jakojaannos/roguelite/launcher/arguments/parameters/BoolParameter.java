package fi.jakojaannos.roguelite.launcher.arguments.parameters;

public class BoolParameter extends Parameter<Boolean> {
    BoolParameter(String name) {
        super(name);
    }

    @Override
    public Boolean parse(String string) {
        return Boolean.valueOf(string);
    }
}
