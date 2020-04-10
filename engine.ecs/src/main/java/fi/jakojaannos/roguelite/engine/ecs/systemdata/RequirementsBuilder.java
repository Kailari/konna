package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.Requirements;

public class RequirementsBuilder<TResources, TEntityData, TEvents> implements Requirements<TResources, TEntityData, TEvents> {
    private SystemInputRecord.EntityData<TEntityData> entityDataRecord;
    private SystemInputRecord.Resources<TResources> resourcesRecord;
    private SystemInputRecord.Events<TEvents> eventsRecord;

    @Override
    public Requirements<TResources, TEntityData, TEvents> events(
            final Class<TEvents> eventsClass
    ) {
        this.eventsRecord = SystemInputRecord.Events.createFor(eventsClass);
        return this;
    }

    @Override
    public Requirements<TResources, TEntityData, TEvents> resources(
            final Class<TResources> resourcesClass
    ) {
        this.resourcesRecord = SystemInputRecord.Resources.createFor(resourcesClass);
        return this;
    }

    @Override
    public Requirements<TResources, TEntityData, TEvents> entityData(
            final Class<TEntityData> entityDataClass
    ) {
        this.entityDataRecord = SystemInputRecord.EntityData.createFor(entityDataClass);
        return this;
    }

    public ParsedRequirements<TResources, TEntityData, TEvents> build() {
        if (this.entityDataRecord == null) {
            try {
                this.entityDataRecord = createNoEntitiesRecord();
            } catch (final ClassCastException e) {
                throw new IllegalStateException("Entity data not defined or malformed in system requirements! "
                                                + "Either use NoEntities or call .entityData(clazz) "
                                                + "when declaring requirements!");
            }
        }

        if (this.resourcesRecord == null) {
            try {
                this.resourcesRecord = createNoResourcesRecord();
            } catch (final ClassCastException e) {
                throw new IllegalStateException("Resource data not defined or malformed in system requirements! "
                                                + "Either use NoResources or call .resources(clazz) "
                                                + "when declaring requirements!");
            }
        }

        if (this.eventsRecord == null) {
            try {
                this.eventsRecord = createNoEventsRecord();
            } catch (final ClassCastException e) {
                throw new IllegalStateException("Event data not defined or malformed in system requirements! "
                                                + "Either use NoEvents or call .events(clazz) "
                                                + "when declaring requirements!");
            }
        }

        return new ParsedRequirements<>(this.entityDataRecord, this.resourcesRecord, this.eventsRecord);
    }

    @SuppressWarnings("unchecked")
    private SystemInputRecord.EntityData<TEntityData> createNoEntitiesRecord() {
        return (SystemInputRecord.EntityData<TEntityData>)
                SystemInputRecord.EntityData.createFor(EcsSystem.NoEntities.class);
    }

    @SuppressWarnings("unchecked")
    private SystemInputRecord.Resources<TResources> createNoResourcesRecord() {
        return (SystemInputRecord.Resources<TResources>)
                SystemInputRecord.Resources.createFor(EcsSystem.NoResources.class);
    }

    @SuppressWarnings("unchecked")
    private SystemInputRecord.Events<TEvents> createNoEventsRecord() {
        return (SystemInputRecord.Events<TEvents>)
                SystemInputRecord.Events.createFor(EcsSystem.NoEvents.class);
    }
}
