package io.github.anjoismysign.entity.mechanics;

import org.bukkit.scheduler.BukkitTask;

public abstract class TycoonMechanics implements Mechanics {
    private final MechanicsOperator operator;
    private BukkitTask task;
    private boolean stoppedEarnings;

    protected TycoonMechanics(MechanicsOperator operator) {
        this.operator = operator;
    }

    public MechanicsOperator getOperator() {
        return operator;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    public boolean isFallingShort() {
        return stoppedEarnings;
    }

    public void setFallingShort(boolean fallingShort) {
        this.stoppedEarnings = fallingShort;
    }
}
