package matching.models;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.Arrays;

public class MatchingData {
    public int[]          solution_nodes;
    public int[]          solution_edges;
    public int[]          candidatesIT;
    public IntOpenHashSet matchedNodes;
    public IntOpenHashSet matchedEdges;
    public IntArrayList[] setCandidates;


    public MatchingData(QueryStructure query_obj){
        // INITIALIZATION
        int edges_num  = query_obj.getQuery_edges().size();
        int nodes_num  = query_obj.getQuery_nodes().size();
        solution_edges = new int[edges_num];
        solution_nodes = new int[nodes_num];
        candidatesIT   = new int[edges_num];
        matchedEdges = new IntOpenHashSet();
        matchedNodes = new IntOpenHashSet();
        setCandidates  = new IntArrayList[edges_num];

        // SETTING
        Arrays.fill(solution_nodes, -1);
        Arrays.fill(solution_edges, -1);
        Arrays.fill(candidatesIT,   -1);
        for(int i=0; i<edges_num; i++)
            setCandidates[i] = new IntArrayList();
    }
}
