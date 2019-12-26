package fi.jakojaannos.roguelite.engine.view.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.Resource;

public class RenderPass implements Resource {
    public int value;
    public int maxHierarchyDepth = 10;
}
