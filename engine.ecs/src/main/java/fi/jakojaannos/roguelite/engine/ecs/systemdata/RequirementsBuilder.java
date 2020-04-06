package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.Requirements;

public class RequirementsBuilder<TResources, TEntityData, TEvents> implements Requirements<TResources, TEntityData, TEvents> {
    private SystemInputRecord<TEntityData> entityDataRecord;
    private SystemInputRecord<TResources> resourcesRecord;

    @Override
    public Requirements<TResources, TEntityData, TEvents> events(
            final Class<TEvents> eventsClass
    ) {
        throw new UnsupportedOperationException("System events are not implemented");
    }

    @Override
    public Requirements<TResources, TEntityData, TEvents> resources(
            final Class<TResources> resourcesClass
    ) {
        this.resourcesRecord = SystemInputRecord.createFor(resourcesClass);
        return this;
    }

    @Override
    public Requirements<TResources, TEntityData, TEvents> entityData(
            final Class<TEntityData> entityDataClass
    ) {
        this.entityDataRecord = SystemInputRecord.createFor(entityDataClass);
        return this;
    }

    @SuppressWarnings("unchecked")
    public ParsedRequirements<TResources, TEntityData, TEvents> build() {
        if (this.entityDataRecord == null) {
            try {
                this.entityDataRecord =
                        (SystemInputRecord<TEntityData>) SystemInputRecord.createFor(EcsSystem.NoEntities.class);
            } catch (final ClassCastException e) {
                throw new IllegalStateException("Entity data not defined in system requirements! "
                                                + "Either use NoEntities or call .entityData(clazz) "
                                                + "in declare requirements!");
            }
        }

        if (this.resourcesRecord == null) {
            try {
                this.resourcesRecord =
                        (SystemInputRecord<TResources>) SystemInputRecord.createFor(EcsSystem.NoResources.class);
            } catch (final ClassCastException e) {
                throw new IllegalStateException("Entity data not defined in system requirements! "
                                                + "Either use NoEntities or call .entityData(clazz) "
                                                + "in declare requirements!");
            }
        }

        return new ParsedRequirements<>(this.entityDataRecord, this.resourcesRecord);
    }
}
