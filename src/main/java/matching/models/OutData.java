package matching.models;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class OutData {
    public double domain_time;
    public double ordering_time;
    public double symmetry_time;
    public double matching_time;
    public long num_occurrences;
    public ObjectOpenHashSet<String> occurrences;

    public OutData() {
        this.domain_time = 0d;
        this.ordering_time = 0d;
        this.symmetry_time = 0d;
        this.matching_time = 0d;
        this.num_occurrences = 0l;
        this.occurrences = new ObjectOpenHashSet<>();
    }

    public double getTotalTime() {
        double time = this.domain_time;
        time += this.ordering_time;
        time += this.symmetry_time;
        time += this.matching_time;

        return time;
    }
}
