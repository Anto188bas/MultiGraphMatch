package matching.models;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;

public class OutData {
    public double domain_time;
    public double ordering_time;
    public double symmetry_time;
    public double matching_time;
    public long num_occurrences;
    public ObjectArraySet<String> occurrences;

    public OutData() {
        this.domain_time = 0d;
        this.ordering_time = 0d;
        this.symmetry_time = 0d;
        this.matching_time = 0d;
        this.num_occurrences = 0l;
        this.occurrences = new ObjectArraySet<>();
    }
}
