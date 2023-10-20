package matching.controllers;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import matching.models.MatchingData;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;

public abstract class FindCandidateParent {
    public abstract IntArrayList find_candidates(
        GraphPaths graphPaths,         QueryStructure query,   int sel_state, IntArrayList[] nodes_symmetry,
        IntArrayList[] edges_symmetry, StateStructures states, MatchingData matchingData
    );

    public abstract IntArrayList find_first_candidates(
         int q_src, int q_dst, int t_src, int t_dst, int edge_id,
         QueryStructure query, GraphPaths graphPaths, MatchingData matchingData, IntArrayList[] nodes_symmetry,
         StateStructures states
    );
}
