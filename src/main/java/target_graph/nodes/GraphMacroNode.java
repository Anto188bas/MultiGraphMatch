package target_graph.nodes;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class GraphMacroNode {
    /**
     *                   GraphMacroNode
     *     labels               : [1,3,...n]
     *     graphNodesProperties : [Tab1, Tab2, ..., Tab_m]
     *     children:
     *        2 -> GraphMacroNode_2
     *        3 -> GraphMacroNode_3
     *        ...
     *        n -> GraphMacroNode_n
     **/
    // ATTRIBUTES
    protected int[]                                 labels;
    protected NodesProperties[]                     graphNodesProperties;
    protected Int2ObjectOpenHashMap<GraphMacroNode> children;

    // CONSTRUCTORS
    public GraphMacroNode(int[] labels){
        this.labels   = labels;
        this.children = new Int2ObjectOpenHashMap<>();
    }

    public GraphMacroNode(int[] labels, int num_tables, int pos, NodesProperties nodes_props){
        this.labels               = labels;
        graphNodesProperties      = new NodesProperties[num_tables];
        graphNodesProperties[pos] = nodes_props;
        this.children             = new Int2ObjectOpenHashMap<>();
    }


    // GETTER
    public int[]             get_macroNode_labels()            {return this.labels;}
    public NodesProperties[] get_propertiesTables()            {return this.graphNodesProperties;}
    public NodesProperties   get_propertiesTable(int pos)      {return this.graphNodesProperties[pos];}
    public Int2ObjectOpenHashMap<GraphMacroNode> getChildren() {return this.children;}
    public GraphMacroNode    getChild(int label)               {return this.children.get(label);}
    // TODO add other methods

    // UTILITY FUNCTION
    public void   set_properties_table(int pos, NodesProperties table)   {this.graphNodesProperties[pos] = table;}
    public void   add_child_in_map(int label, GraphMacroNode node_child) {this.children.put(label, node_child);  }
}
