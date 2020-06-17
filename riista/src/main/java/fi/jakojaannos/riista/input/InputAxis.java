package fi.jakojaannos.riista.input;

public interface InputAxis {
    enum Mouse implements InputAxis {
        X,
        Y,
        X_POS,
        Y_POS,
    }
}
