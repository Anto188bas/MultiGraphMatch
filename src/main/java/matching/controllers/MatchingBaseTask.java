package matching.controllers;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import matching.models.OutData;

import java.io.FileNotFoundException;

public class MatchingBaseTask implements Runnable {
    private final int id;
    public ObjectArrayList<ObjectArraySet<String>> sharedMemory;
    MatchingBase matchingMachine;
    public OutData outData;

    public MatchingBaseTask(int id, ObjectArrayList<ObjectArraySet<String>> sharedMemory, MatchingBase matchingMachine) {
        this.id = id;
        this.sharedMemory = sharedMemory;
        this.matchingMachine = matchingMachine;
    }

    public void run() {
        outData = matchingMachine.matching();
        sharedMemory.add(outData.occurrences);
    }
}
