package simmetry_condition;

import cypher.models.QueryEdgeAggregation;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.BitSet;


public class SymmetryCondition {
    // 1. SEQUENCE CREATION
    private static int[][] create_sequence_matrix(QueryStructure query, int number_of_nodes){
        // sequence[i] contains the neighbours number for each neighbour of ith node
        int[][] sequence = new int[number_of_nodes][number_of_nodes];
        for (int i = 0; i < number_of_nodes; i++) {
            for (int j = 0; j < number_of_nodes; j++) {
                sequence[i][j] = 0;
                if (!(query.isOut(i, j) || query.isIn(i, j) || query.isRev(i, j)))
                    continue;
                for (int k = 0; k < number_of_nodes; k++) {
                    if (!(query.isOut(j, k) || query.isIn(j, k) || query.isRev(j, k)))
                        continue;
                    sequence[i][j] += 1;
                }
            } Arrays.sort(sequence[i]);
        } return sequence;
    }

    // 2. SUPPORT CREATION
    private static BitSet support_creation(int[][] sequence, int nodes_number){
        BitSet support = new BitSet(2 * nodes_number);
        for (int i = 0; i < nodes_number; i++)
            for (int j = i; j < nodes_number; j++){
                if (Arrays.equals(sequence[i], sequence[j])){
                   support.set(i * nodes_number + j);
                   support.set(j * nodes_number + i);
                }
            } return support;
    }

    // 3. ISOMORPHIC EXTENSION
    private static void isomorphicExtensions(int[] fDir, int[] fRev, ObjectArrayList<int[]> vv, BitSet support, int pos, QueryStructure query){
        int node_number = fDir.length;
        int[] cand      = new int[node_number];
        Arrays.fill(cand, 0, cand.length -1, -1);
        int i, j;
        if (pos == node_number)
            vv.add(Arrays.copyOfRange(fDir, 0, node_number));
        else {
            int[] count = new int[node_number];
            Arrays.fill(count, 0, count.length -1, 0);
            int   ncand = 0;
            // WE ARE WORKING ON THE ITH NODE
            for (i = 0; i < node_number; i++) {
                if(fDir[i] == -1) continue;
                IntArrayList vNei = query.get_node_neighbours(i);
                // MAINTAIN NEIGHBOURS WITH fDir == -1
                for (int neigh : vNei){
                    if (fDir[neigh] != -1) continue;
                    if (count[neigh] == 0)
                        cand[ncand++] = neigh;
                    count[neigh]++;
                }
            }
            // GET THE INDEX VALUE WHERE COUNT IS HIGHER. THEN, SELECT THE CORRESPONDING NEIGHBOUR
            int neigh_max_count = 0;
            for (i = 1; i < ncand; i++)
                if (count[i] > count[neigh_max_count])
                    neigh_max_count = i;
            neigh_max_count = cand[neigh_max_count];
            ncand           = 0;
            BitSet already  = new BitSet(node_number);
            // WE ARE WORKING ON THE NODE fDir[i]
            for (i = 0; i < node_number; i++) {
                if (fDir[i] == -1) continue;
                IntArrayList vNei = query.get_node_neighbours(fDir[i]);
                for (int neigh: vNei) {
                    if (!already.get(neigh) && fRev[neigh] == -1 && support.get(neigh_max_count * node_number + neigh)) {
                        cand[ncand++] = neigh;
                        already.set(neigh);
                    }
                }
            }
            boolean flag; int fneigh;
            QueryEdgeAggregation edges_data = query.getQuery_pattern();
            for (i = 0; i < ncand; i++){
                fneigh = cand[i]; flag   = false;
                if (fneigh == neigh_max_count || query.nodes_equivalent_to(neigh_max_count, fneigh)) {
                    for (j = 0; j < node_number; j++) {
                        if (fDir[j] == -1) continue;
                        // OUT CHECKING
                        flag = query.nodes_pairs_compatibilities(neigh_max_count, j, fneigh, fDir[j], edges_data.getOut_edges());
                        if (flag) break;
                        // IN CHECKING
                        flag = query.nodes_pairs_compatibilities(neigh_max_count, j, fneigh, fDir[j], edges_data.getIn_edges());
                        if (flag) break;
                        // IN_OUT CHECKING
                        flag = query.nodes_pairs_compatibilities(neigh_max_count, j, fneigh, fDir[j], edges_data.getIn_out_edges());
                        if (flag) break;
                    }
                    if (flag) continue;
                    fDir[neigh_max_count] = fneigh;
                    fRev[fneigh]          = neigh_max_count;
                    pos++;
                    isomorphicExtensions(fDir, fRev, vv, support, pos, query);
                    pos--;
                    fRev[fDir[fneigh]]    = -1;
                    fDir[neigh_max_count] = -1;
                }
            }
        }
    }

    // 4. AUTOMORPHISM COMPUTING
    private static ObjectArrayList<int[]> findAutomorphisms(QueryStructure query){
        int node_number = query.getQuery_nodes().size();

        // fDir and fRev creation and initialization. all the elements are set to -1
        int[] fDir = new int[node_number];
        int[] fRev = new int[node_number];
        Arrays.fill(fDir, 0, fDir.length -1, -1);
        Arrays.fill(fRev, 0, fRev.length -1, -1);

        // Sequence and support configuration
        int[][] sequence = create_sequence_matrix(query, node_number);
        BitSet  support  = support_creation(sequence, node_number);

        // Final operation
        ObjectArrayList<int[]> vv = new ObjectArrayList<>();
        for (int g = 0; g < node_number; g++) {
            if (support.get(g * node_number)) {
                fDir[0] = g;
                fRev[g] = 0;
                int pos = 1;
                isomorphicExtensions(fDir, fRev, vv, support, pos, query);
                fRev[fDir[0]] = -1;
                fDir[0] = -1;
            }
        } return vv;
    }

    public IntArrayList[] getNodeSymmetryConditions(QueryStructure query) {
        int i, j;
        ObjectArrayList<int[]> vv = findAutomorphisms(query);
        int vv_size               = vv.size();
        int numNodes              = query.getQuery_nodes().size();
        IntArrayList[] listCond   = new IntArrayList[numNodes];
        Arrays.fill(listCond, 0, numNodes -1, new IntArrayList());
        BitSet broken             = new BitSet(vv_size);
        // TODO complete me
        return null;
    }
}
