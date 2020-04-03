package fi.jakojaannos.roguelite.game.weapons;

public class ClipMagazineHandler implements Weapon.MagazineHandler<ClipMagazineState> {
    @Override
    public ClipMagazineState createState() {
        return new ClipMagazineState();
    }
}
