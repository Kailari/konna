package fi.jakojaannos.roguelite.engine.ecs.legacy;

/**
 * Some 'thing' in the game world. Conceptually entities are collections of components. Technically they are identifiers
 * which can be used to fetch components associated with the ID.
 * <p>
 * Everything in the game {@link LegacyWorld world} is an entity in some way. Enemies are entities. The Player is an entity.
 * As of writing this, the level geometry is one huge complex entity. That being said, as stated above, entities
 * themselves are simple. They are just an ID for a collection of components.
 * <p>
 * To achieve complex behavior, or actually any behavior at all, interaction with {@link Component components} and
 * {@link ECSSystem Systems} are required. <code>ECSSystem</code>s are logical units which manipulate
 * <code>Component</code>s, iterating based on entities' component combinations.
 * <code>Component</code>s on the other hand, are just plain data.
 * <p>
 * Entities cannot be manipulated directly. All interactions, including adding/getting/removing components are to be
 * done through the {@link EntityManager} used to create the entity.
 *
 * @see Component
 * @see EntityManager
 * @see ECSSystem
 */
@Deprecated
public interface Entity {
    /**
     * Gets the unique identifier for this entity. Entity IDs are guaranteed to be unique.
     *
     * @return the unique ID of this entity.
     */
    @Deprecated
    int getId();

    @Deprecated
    boolean isMarkedForRemoval();
}
