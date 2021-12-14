package target_graph.nodes;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import target_graph.nodes.GraphMacroNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class NodesLabelsGraph {
    protected Int2ObjectOpenHashMap<GraphMacroNode> nodes_label_graph;

    // CONSTRUCTORS
    public NodesLabelsGraph(
         HashMap<String, GraphMacroNode>          nodes        ,
         Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId ,
         int                                      max_label_lev
    ){   init(nodes, level_nodeId, max_label_lev);}

    // METHODS
    // 1. ADD NODE
    private void add_graph_node(
            Int2ObjectOpenHashMap<GraphMacroNode> actual_level   ,
            Int2ObjectOpenHashMap<GraphMacroNode> previous_level ,
            int                                   actual_label   ,
            int                                   next_label     ,
            GraphMacroNode                        selected_node  ,
            int[]                                 sub_label
    ){
        actual_level.put(next_label, selected_node);
        // CREATE THE "next_label" ELEMENT IN THE LEVEL i-1 IF NOT EXISTS
        if(!previous_level.containsKey(next_label)) {
            sub_label[sub_label.length - 1] = next_label;
            previous_level.put(next_label, new GraphMacroNode(sub_label));
        }
        // CREATE AN EDGE BETWEEN THE "next_label" IN THE LEVEL i AND THE SAME LABEL IN THE LEVEL i-1
        GraphMacroNode prev_node = previous_level.get(next_label);
        prev_node.getChildren().put(actual_label, actual_level.get(next_label));
    }

    // 2. RECURSIVE FUNCTION TO POPULATE THE TREE
    private void tree_population(
            Int2ObjectOpenHashMap<GraphMacroNode> actual_level   ,
            Int2ObjectOpenHashMap<GraphMacroNode> previous_level ,
            int[]                                 parents_labels ,
            int[]                                 children_labels,
            GraphMacroNode                        node_to_add
    ){
        int actual_label = parents_labels[parents_labels.length - 1];
        int[] parents_labels_copy = Arrays.copyOfRange(parents_labels, 0, parents_labels.length);
        if(children_labels.length == 1)
            add_graph_node(actual_level, previous_level, actual_label, children_labels[0], node_to_add, parents_labels_copy);
        else {
            for(int ci : children_labels){
                if(actual_level.containsKey(ci))
                    continue;
                int[] next_node_labels = Arrays.copyOfRange(parents_labels, 0, parents_labels.length + 1);
                next_node_labels[parents_labels.length] = ci;
                GraphMacroNode next_node = new GraphMacroNode(next_node_labels);
                add_graph_node(actual_level, previous_level, actual_label, ci, next_node, parents_labels_copy);
            }
            previous_level  = actual_level;
            actual_level    = actual_level.get(children_labels[0]).getChildren();
            parents_labels  = Arrays.copyOfRange(parents_labels, 0, parents_labels.length + 1);
            parents_labels[parents_labels.length -1] = children_labels[0];
            children_labels = Arrays.copyOfRange(children_labels, 1, children_labels.length);
            tree_population(actual_level, previous_level, parents_labels, children_labels, node_to_add);
        }
    }

    public void init(
            HashMap<String, GraphMacroNode>          nodes,
            Int2ObjectOpenHashMap<ArrayList<String>> level_nodeId,
            int                                      max_label_lev
    ){
        this.nodes_label_graph = new Int2ObjectOpenHashMap<>();
        for(int i = 1; i <= max_label_lev; i++){
            ArrayList<String> level_i_nodes_name = level_nodeId.get(i);
            // NODES UNDER THE ROOT
            if(i == 1)
                level_i_nodes_name.forEach(node_name -> {
                    GraphMacroNode sel_node = nodes.get(node_name);
                    this.nodes_label_graph.put(sel_node.get_macroNode_labels()[0], sel_node);
                });
            // MIDDLE AND LEAVES NODES
            else{
                level_i_nodes_name.forEach(node_name -> {
                    GraphMacroNode sel_node    = nodes.get(node_name);
                    int[]     node_labels      = sel_node.get_macroNode_labels();
                    int[]     parents_labels   = Arrays.copyOfRange(node_labels, 0, 1);
                    int[]     children_labels  = Arrays.copyOfRange(node_labels, 1, node_labels.length);
                    if(!this.nodes_label_graph.containsKey(parents_labels[0]))
                        this.nodes_label_graph.put(parents_labels[0], new GraphMacroNode(parents_labels));
                    Int2ObjectOpenHashMap<GraphMacroNode> actual_level = this.nodes_label_graph.get(parents_labels[0]).getChildren();
                    tree_population(actual_level, this.nodes_label_graph, parents_labels, children_labels, sel_node);
                });
            }
        }
    }

    public Int2ObjectOpenHashMap<GraphMacroNode> getTriangle_node_graph() {
        return this.nodes_label_graph;
    }

}
