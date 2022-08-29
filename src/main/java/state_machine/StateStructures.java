package state_machine;

import ordering.EdgeDirection;

public class StateStructures {
    public int[]            map_state_to_edge;
    public int[]            map_edge_to_state;
    public int[]            map_state_to_first_endpoint;
    public int[]            map_state_to_second_endpoint;
    public EdgeDirection[]  map_edge_to_direction;
    // state to node that have to be matched
    public int[]            map_state_to_unmatched_node;

    public StateStructures(){}
    public StateStructures(
        int[] map_state_to_edge,
        int[] map_edge_to_state,
        int[] map_state_to_src,
        int[] map_state_to_dst,
        int[] map_state_to_mnode,
        EdgeDirection[] map_edge_to_direction
    ){
        this.map_edge_to_state      = map_edge_to_state;
        this.map_state_to_edge      = map_state_to_edge;
        this.map_state_to_first_endpoint = map_state_to_src;
        this.map_state_to_second_endpoint = map_state_to_dst;
        this.map_state_to_unmatched_node = map_state_to_mnode;
        this.map_edge_to_direction  = map_edge_to_direction;
    }
}
