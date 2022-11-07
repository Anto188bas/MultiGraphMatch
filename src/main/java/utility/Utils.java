package utility;

import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import ordering.EdgeDirection;
import ordering.EdgeOrdering;
import state_machine.StateStructures;
import target_graph.graph.GraphPaths;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.function.IntConsumer;

public class Utils {
    public static IntArraySet intArraySetUnion(IntArraySet a, IntArraySet b) {
        IntArraySet result = a.clone();
        result.addAll(b);

        return result;
    }

    public static IntArraySet intArraySetDifference(IntArraySet a, IntArraySet b) {
        IntArraySet result = a.clone();
        result.removeAll(b);

        return result;
    }

    public static IntArraySet intArraySetIntersection(IntArraySet a, IntArraySet b) {
        IntArraySet result = a.clone();
        result.retainAll(b);

        return result;
    }
    public static void printDebugInfo(GraphPaths graphPaths, QueryStructure query_obj, StateStructures states, EdgeOrdering edgeOrdering)  {
        /**
         * LOG
         */
//        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream("output.txt")), true));
        System.out.println("TARGET GRAPH");
        graphPaths.getMap_pair_to_key().forEach((src, map) -> {
            map.forEach((dst, key) -> {
                System.out.print("(SRC: " + src +", DST: " + dst + ") -> {");
                IntArrayList[] edgeList = graphPaths.getMap_key_to_edge_list()[key];
                for(int color = 0; color < edgeList.length; color++) {
                    int finalColor = color;
                    edgeList[color].forEach((IntConsumer) (edge) -> {
                        System.out.print("( " + edge + ":C" + finalColor + "), " );
                    });
                }
                System.out.print("}\n");
            });
        });

        System.out.println("QUERY NODES");
        query_obj.getQuery_nodes().forEach((id, node) -> {
            System.out.println("ID: " + id + "-> " + node);
        });

        System.out.println("QUERY EDGES");
        query_obj.getQuery_pattern().getOut_edges().forEach((key, list) -> {
            System.out.println(key + "->" + list);
        });

        System.out.println("PAIRS DOMAINS");
        query_obj.getPairs().forEach((pair) -> {
            System.out.print("P: " + pair + "\tDOMAIN (FS): ");
            pair.getFirst_second().forEach((key, list) -> {
                for (int dst : list) {
                    System.out.print("[" + key + ", " + dst + "], ");
                }

            });

            System.out.print("\tDOMAIN (SF): ");

            pair.getSecond_first().forEach((key, list) -> {
                for (int dst : list) {
                    System.out.print("[" + key + ", " + dst + "], ");
                }
            });
            System.out.print("\n");
        });
//
//        System.out.println("NODES DOMAINS");
//        query_obj.getMap_node_to_domain().forEach((node, domain) -> {
//            System.out.println("NODE: " + node + " -> " + domain.size());
//
//            System.out.println("NODE: " + node + " -> " + domain);
//        });
//
        System.out.println("PARIS ORDERING");
        System.out.println(edgeOrdering.getPairs_ordering());

//        System.out.println("ORDERING DETAILS");
//        for (int i = 0; i < states.map_state_to_first_endpoint.length; i++) {
//            int edge = states.map_state_to_edge[i];
//            int src = states.map_state_to_first_endpoint[i];
//            int dst = states.map_state_to_second_endpoint[i];
//            int matchedNode = states.map_state_to_unmatched_node[i];
//            EdgeDirection direction = states.map_edge_to_direction[i];
//            System.out.println("STATE: " + i + "\tSRC: " + src + "\tDST: " + dst + "\tEDGE: " + edge + "\tDIRECTION: " + direction + "\tUN-MATCHED_NODE: " + matchedNode);
//        }
    }

    public static void printSymmetryConditions(IntArrayList[] nodes_symmetry, IntArrayList[] edges_symmetry) {
        System.out.println("NODES SYMMETRY: " + Arrays.toString(nodes_symmetry)) ;
        System.out.println("EDGES SYMMETRY: " + Arrays.toString(edges_symmetry)) ;
    }

    public static IntArrayList intersection(IntCollection a, IntCollection b) {
        IntArrayList result = new IntArrayList();

        IntIterator iterator = b.iterator();
        while (iterator.hasNext()) {
            int next = iterator.nextInt();
            if (a.contains(next)) {
                result.add(next);
            }
        }

        return result;
    }
}
