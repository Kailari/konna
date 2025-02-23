package fi.jakojaannos.roguelite.launcher.arguments.parameters;

import java.nio.file.Paths;

import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;

public class PathParameter extends Parameter<String> {
    private boolean mustBeDirectory;
    private boolean mustExist;

    PathParameter(final String name) {
        super(name);
    }

    public fi.jakojaannos.roguelite.launcher.arguments.parameters.PathParameter mustBeDirectory() {
        this.mustExist = true;
        this.mustBeDirectory = true;
        return this;
    }

    public fi.jakojaannos.roguelite.launcher.arguments.parameters.PathParameter mustExist() {
        this.mustExist = true;
        return this;
    }

    @Override
    public String parse(final String string) throws ArgumentParsingException {
        final var file = Paths.get(string).toFile();
        if (this.mustExist && !file.exists()) {
            throw new ArgumentParsingException(String.format(
                    "File/directory in path \"%s\" does not exist",
                    string
            ));
        }

        if (this.mustBeDirectory && !file.isDirectory()) {
            throw new ArgumentParsingException(String.format(
                    "File/path \"%s\" is not a directory",
                    string
            ));
        }
        return string;
    }
}
