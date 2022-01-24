package matching.models;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;

public class MatchingData {
    public int[]          solution_nodes;
    public int[]          solution_edges;
    public IntOpenHashSet matchedNodes;
    public IntOpenHashSet matchedEdges;

    public MatchingData(QueryStructure query_obj){
        // INITIALIZATION
        solution_edges = new int[query_obj.getQuery_edges().size()];
        solution_nodes = new int[query_obj.getQuery_nodes().size()];
        matchedEdges   = new IntOpenHashSet();
        matchedNodes   = new IntOpenHashSet();

        // SETTING
        Arrays.fill(solution_nodes, -1);
        Arrays.fill(solution_edges, -1);
    }
}
