package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;

public class JumpingMovementAbility implements Component {
    public long jumpCoolDownInTicks;
    public long jumpDurationInTicks;
    public double jumpForce;

    public long lastJumpTimeStamp;

    public static Builder builder() {
        return new Builder();
    }

    private JumpingMovementAbility(
            final long jumpCoolDownInTicks,
            final long jumpDurationInTicks,
            final double jumpForce
    ) {
        this.jumpCoolDownInTicks = jumpCoolDownInTicks;
        this.jumpDurationInTicks = jumpDurationInTicks;
        this.jumpForce = jumpForce;

        this.lastJumpTimeStamp = -100000000;
    }

    public static class Builder {
        public long jumpCoolDownInTicks = 50;
        public long jumpDurationInTicks = 30;
        public double jumpForce = 5.0;

        public Builder jumpCoolDownInTicks(final long jumpCoolDownInTicks) {
            this.jumpCoolDownInTicks = jumpCoolDownInTicks;
            return this;
        }

        public Builder jumpDurationInTicks(final long jumpDurationInTicks) {
            this.jumpDurationInTicks = jumpDurationInTicks;
            return this;
        }

        public Builder jumpForce(final double jumpForce) {
            this.jumpForce = jumpForce;
            return this;
        }

        public JumpingMovementAbility build() {
            return new JumpingMovementAbility(this.jumpCoolDownInTicks,
                                              this.jumpDurationInTicks,
                                              this.jumpForce);
        }
    }
}
