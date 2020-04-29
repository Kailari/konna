package fi.jakojaannos.roguelite.game.view.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Network;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.network.NetworkManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIElementType;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.engine.view.ui.builder.GenericUIElementBuilder;

import static fi.jakojaannos.roguelite.engine.view.ui.ProportionValue.*;

/**
 * Keeps network information visible on the GUI
 */
public class NetworkHUDSystem implements ECSSystem {
    private final UserInterface userInterface;
    private final UIElement connectedStatus;
    private final UIElement connectionError;

    public NetworkHUDSystem(final UserInterface userInterface) {
        this.userInterface = userInterface;

        final UIElement networkRoot = findOrCreateNetworkRoot();
        this.connectedStatus = networkRoot.findChildren(that -> that.hasName().equalTo("connection_status"))
                                          .findAny()
                                          .orElseThrow();
        this.connectionError = networkRoot.findInChildren(that -> that.hasName().equalTo("connection_error"))
                                          .findAny()
                                          .orElseThrow();
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var network = world.fetchResource(Network.class);
        final var isConnected = network.getNetworkManager()
                                       .map(NetworkManager::isConnected)
                                       .orElse(false);

        this.connectedStatus.setProperty(UIProperty.TEXT,
                                         isConnected
                                                 ? "Connected"
                                                 : "Not connected");

        final var error = network.getConnectionError();
        this.connectionError.setProperty(UIProperty.HIDDEN, isConnected || error.isEmpty());
        this.connectionError.setProperty(UIProperty.TEXT, error.orElse("No error"));
    }

    private UIElement findOrCreateNetworkRoot() {
        return this.userInterface.findElements(that -> that.hasName().equalTo("network_root"))
                                 .findAny()
                                 .orElseGet(() -> this.userInterface.addElement("network_root",
                                                                                UIElementType.NONE,
                                                                                this::createNetworkRoot));
    }

    private void createNetworkRoot(final GenericUIElementBuilder parentBuilder) {
        parentBuilder.child("connection_status",
                            UIElementType.LABEL,
                            builder -> builder.fontSize(24)
                                              .left(absolute(5))
                                              .top(absolute(5))
                                              .text("Connection status unknown")
                                              .child("connection_error",
                                                     UIElementType.LABEL,
                                                     errorBuilder -> errorBuilder.fontSize(12)
                                                                                 .left(absolute(0))
                                                                                 .top(percentOf(parentHeight(1.1)))
                                                                                 .color(0.545, 0.0, 0.0)
                                                                                 .text("")));
    }
}
