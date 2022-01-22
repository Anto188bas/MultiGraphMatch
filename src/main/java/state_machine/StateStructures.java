package state_machine;

public class StateStructures {
    public int[] map_state_to_edge;
    public int[] map_edge_to_state;
    public int[] map_state_to_src;
    public int[] map_state_to_dst;

    public StateStructures(){}
    public StateStructures(int[] map_state_to_edge, int[] map_edge_to_state, int[] map_state_to_src, int[] map_state_to_dst){
        this.map_edge_to_state = map_edge_to_state;
        this.map_state_to_edge = map_state_to_edge;
        this.map_state_to_src  = map_state_to_src;
        this.map_state_to_dst  = map_state_to_dst;
    }
}
