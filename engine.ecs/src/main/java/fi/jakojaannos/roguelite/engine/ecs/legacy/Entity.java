package fi.jakojaannos.roguelite.engine.ecs.legacy;

@Deprecated
public interface Entity {
    int getId();

    @Deprecated
    boolean isMarkedForRemoval();
}
