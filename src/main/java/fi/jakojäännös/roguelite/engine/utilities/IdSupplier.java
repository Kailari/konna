package fi.jakojäännös.roguelite.engine.utilities;

import java.util.PriorityQueue;

public class IdSupplier {
    private final PriorityQueue<Integer> freeIDs = new PriorityQueue<>();
    private int count;

    public int get() {
        int newId;
        if (this.freeIDs.isEmpty()) {
            newId = this.count + 1;
        } else {
            newId = this.freeIDs.poll();
        }

        this.count = this.count + 1;
        return newId;
    }

    public void free(int id) {
        this.freeIDs.offer(id);
        this.count = this.count - 1;
    }
}
