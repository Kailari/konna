package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.*;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

@Slf4j
public class SlimeAIControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                .requireResource(Players.class)
                .withComponent(SlimeAI.class)
                .withComponent(CharacterInput.class)
                .withComponent(CharacterStats.class)
                .withComponent(Transform.class);
    }


    private final Vector2d tempPos = new Vector2d(),
            tempPos2 = new Vector2d(),
            tempPlayerPos = new Vector2d(),
            tempDir = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();
        val entityManager = world.getEntityManager();
        val player = world.getOrCreateResource(Players.class).player;
        if (player == null) {
            return;
        }

        val opt = entityManager.getComponentOf(player, Transform.class);
        if (opt.isEmpty()) {
            LOG.debug("Error: cannot get player's Transform!");
            return;
        }

        tempPlayerPos.set(opt.get().position);

        entities.forEach(entity -> {
            val ai = entityManager.getComponentOf(entity, SlimeAI.class).orElseThrow();
            val input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
            tempPos.set(entityManager.getComponentOf(entity, Transform.class).orElseThrow().position);

            ai.airTime -= delta;
            ai.jumpCoolDown -= delta;

            if (ai.airTime > 0.0) {
                input.move.set(ai.jumpDir);
                return;
            }

            val optSharedAI = entityManager.getComponentOf(entity, SlimeSharedAI.class);
            if (optSharedAI.isPresent()) {

                boolean continueNormalBehaviour = sharedAIBehaviour(entityManager, entity);
                if (!continueNormalBehaviour) return;
            }

            val dist = tempPlayerPos.distanceSquared(tempPos);

            ai.regroupTimer -= delta;
            if (dist > ai.chaseRadiusSquared || dist < ai.targetRadiusSquared) {
                input.move.set(0);
                return;
            }

            if (ai.jumpCoolDown < 0.0) {
                tempDir.set(tempPlayerPos)
                        .sub(tempPos);

                ai.jumpDir.set(tempDir);
                ai.airTime = ai.setAirTimeCoolDown;
                ai.jumpCoolDown = ai.setJumpCoolDown;

                input.move.set(tempDir);
            } else {
                input.move.set(0);
            }
        });
    }


    /**
     * @return returns true if entity should continue "regular" slime behaviour
     * (in other words: slime is not moving towards regroup position)
     */
    private boolean sharedAIBehaviour(EntityManager entityManager, Entity entity) {
        val ai = entityManager.getComponentOf(entity, SlimeAI.class).orElseThrow();
        val sharedAI = entityManager.getComponentOf(entity, SlimeSharedAI.class).orElseThrow();

        val input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
        val stats = entityManager.getComponentOf(entity, CharacterStats.class).orElseThrow();


        // check if everyone else died
        if (sharedAI.slimes.size() <= 1) {
            sharedAI.slimes.clear();
            entityManager.removeComponentFrom(entity, SlimeSharedAI.class);
            // FIXME: slime might have been crawling towards other slimes, and if all but one of them are killed
            //  he will continue with crawling speed. Set regular speed here

            return true;
        }

        if (sharedAI.regrouping) {
            // move to regroup area, if already there then check if others are present as well
            if (moveToRegroupArea(sharedAI, ai, input, tempPos, stats)) {

                if (doRegroupCheck(sharedAI, entityManager)) {
                    regroupSlimes(sharedAI, entityManager);
                    return false;
                }
            }


            return false;
        } else {
            // check if everyone is ready to regroup
            if (ai.regroupTimer <= 0.0) {
                slimeRegroupCheck(sharedAI, entityManager);
            }
            return true;
        }
    }


    /**
     * Check if slimes are ready to regroup. If everyone is ready, set regroup position.
     */
    private void slimeRegroupCheck(SlimeSharedAI sharedAI, EntityManager entityManager) {
        boolean everyoneReady = true;

        for (int i = sharedAI.slimes.size() - 1; i >= 0; i--) {
            val slime = sharedAI.slimes.get(i);
            val optAi = entityManager.getComponentOf(slime, SlimeAI.class);


            if (optAi.isEmpty()) {
                LOG.error("Error: entity without SlimeAI is in SlimeSharedAI's list!");
                sharedAI.slimes.remove(i);
                continue;
            }

            val optPos = entityManager.getComponentOf(slime, Transform.class);
            if (optPos.isEmpty()) {
                LOG.error("Error: entity without Transform is in SlimeSharedAI's list!");
                sharedAI.slimes.remove(i);
                continue;
            }

            val ai = optAi.get();
            if (ai.regroupTimer > 0.0) {
                everyoneReady = false;
            }
        }


        if (everyoneReady && sharedAI.slimes.size() != 0) {
            sharedAI.regrouping = true;
            tempPos2.set(0.0, 0.0);

            for (val slime : sharedAI.slimes) {
                tempPos2.add(entityManager.getComponentOf(slime, Transform.class).orElseThrow().position);
            }

            tempPos2.mul(1.0 / sharedAI.slimes.size());
            sharedAI.regroupPos.set(tempPos2);
        }
    }


    /**
     * @return true if already at position (meaning: inside SlimeAI.regroupRadius), false otherwise
     */
    private boolean moveToRegroupArea(
            SlimeSharedAI sharedAI,
            SlimeAI ai,
            CharacterInput input,
            Vector2d myPos,
            CharacterStats stats
    ) {
        if (sharedAI.regroupPos.distanceSquared(myPos) <= sharedAI.regroupRadiusSquared) {
            input.move.set(0);
            return true;
        }


        stats.speed = ai.crawlSpeed;
        tempDir.set(sharedAI.regroupPos)
                .sub(myPos);

        input.move.set(tempDir);
        return false;
    }

    /**
     * check if all slimes are near each other at regroup position
     *
     * @return true if everyone is at the position
     */
    private boolean doRegroupCheck(SlimeSharedAI sharedAI, EntityManager entityManager) {
        for (int i = sharedAI.slimes.size() - 1; i >= 0; i--) {
            val slime = sharedAI.slimes.get(i);
            val optPos = entityManager.getComponentOf(slime, Transform.class);

            if (optPos.isEmpty()) {
                LOG.error("Error: entity without Transform is in SlimeSharedAI's list!");
                sharedAI.slimes.remove(i);
                continue;
            }

            if (sharedAI.regroupPos.distanceSquared(optPos.get().position) > sharedAI.regroupRadiusSquared)
                return false;

        }

        return true;
    }

    /**
     * Spawn a bigger slime at the regroup position, remove smaller slimes
     */
    private void regroupSlimes(SlimeSharedAI sharedAI, EntityManager entityManager) {
        int totalSize = 0;
        for (val slime : sharedAI.slimes) {
            val optAi = entityManager.getComponentOf(slime, SlimeAI.class);
            if (optAi.isEmpty()) continue;

            val ai = optAi.get();
            totalSize += ai.slimeSize;

            entityManager.destroyEntity(slime);
        }

        if (sharedAI.slimes.size() == 0) totalSize = 1;
        else totalSize = (int) Math.ceil(1.0 * totalSize / sharedAI.slimes.size());

        sharedAI.slimes.clear();

        final double x = sharedAI.regroupPos.x,
                y = sharedAI.regroupPos.y;

        if (totalSize == 1) SlimeArchetype.createMediumSlimeWithInitialVelocity(entityManager, x, y, new Vector2d(0,0), 0.0);
        if (totalSize >= 2) SlimeArchetype.createLargeSlime(entityManager, x, y);
    }


}
