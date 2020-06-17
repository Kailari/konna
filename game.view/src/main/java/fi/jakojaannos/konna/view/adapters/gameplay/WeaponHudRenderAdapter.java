package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.assets.AssetManager;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.data.resources.Events;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponInventory;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.weapons.ActionInfo;

public class WeaponHudRenderAdapter implements EcsSystem<WeaponHudRenderAdapter.Resources, EcsSystem.NoEntities, EcsSystem.NoEvents> {
    //private static final Color COLOR_NEGATIVE = new Color(0.75f, 0.15f, 0.15f);
    //private static final Color COLOR_POSITIVE = new Color(0.95f, 0.95f, 0.95f);

    private final UiElement hud;

    public WeaponHudRenderAdapter(final AssetManager assetManager) {
        this.hud = assetManager.getStorage(UiElement.class)
                               .getOrDefault("ui/weapon-status.json");
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<NoEntities>> entities,
            final NoEvents noEvents
    ) {
        final var renderer = resources.renderer;
        resources.players.getLocalPlayer().ifPresent(localPlayer -> {
            final var localPlayerAbilities = localPlayer.getComponent(AttackAbility.class)
                                                        .orElseThrow();
            final var inventory = localPlayer.getComponent(WeaponInventory.class)
                                             .orElseThrow();
            final var playerPos = localPlayer.getComponent(Transform.class).orElseThrow();

            final var info = new ActionInfo(resources.timeManager,
                                            resources.entities,
                                            playerPos,
                                            localPlayerAbilities,
                                            resources.events);
            final var query = inventory.slots[localPlayerAbilities.equippedSlot]
                    .doStateQuery(info);
            final int ammo = query.currentAmmo;
            final int maxAmmo = query.maxAmmo;
            final var maxAmmoString = maxAmmo == 666 ? "REL" : String.format("%03d", maxAmmo);
            renderer.ui().setValue("AMMO[LOCAL_PLAYER]", String.format("%03d/%s", ammo, maxAmmoString));
            // FIXME: allow further programmatic manipulation of the UI
            //  - Requires some definition of "linked fields" for the UI
            //  - Idea is to allow setting ANY field value using any "compatible" UI variable
            //this.ammoCounter.setProperty(UIProperty.COLOR, ammo == 0 ? COLOR_NEGATIVE : COLOR_POSITIVE);

            final double heat = query.heat;
            if (heat == -1) {
                renderer.ui().setValue("HEAT[LOCAL_PLAYER]", "No heat");
            } else {
                final var heatString = query.jammed ? "JAMMED!" : String.format("%.2f", heat);
                renderer.ui().setValue("HEAT[LOCAL_PLAYER]", heatString);
            }

            renderer.ui().draw(this.hud);
        });
    }

    public static record Resources(
            Renderer renderer,
            TimeManager timeManager,
            Players players,
            Entities entities,
            Events events
    ) {}
}
