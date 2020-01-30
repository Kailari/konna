package fi.jakojaannos.roguelite.engine.ecs;

/**
 * Describes a group of systems. It should be noted that implementors are defining more of a "identifier" for a said
 * group than the logical group. That is, storing information on what systems belong to which groups etc. should be left
 * to dispatcher implementation and
 * <code>SystemGroups</code> themselves do not contain any meaningful data, apart from their
 * name/identifiers.
 *
 * @see DispatcherBuilder#withGroup(SystemGroup)
 * @see DispatcherBuilder#addGroupDependency(SystemGroup, SystemGroup)
 * @see RequirementsBuilder#addToGroup(SystemGroup)
 */
public interface SystemGroup {
    String getName();
}
