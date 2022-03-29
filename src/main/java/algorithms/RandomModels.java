package algorithms;

import com.google.common.graph.*;
import org.jgrapht.generate.BarabasiAlbertGraphGenerator;
import org.jgrapht.generate.GnmRandomGraphGenerator;
import org.jgrapht.generate.GnpRandomGraphGenerator;
import org.jgrapht.generate.WattsStrogatzGraphGenerator;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.util.SupplierUtil;
import java.util.*;
import java.util.function.Supplier;

/**
 *
 * This class contains all the methods for the random networks generation
 *
 */
@SuppressWarnings({"rawtypes", "UnstableApiUsage"})
public class RandomModels {
    private final  Supplier<Integer> vSupplier = new Supplier<>() {
        private int id = 0;
        @Override
        public Integer get() {
            return  id++;
        }
    };

    /**
     *
     * Generate a random network using Barabasi-Albert algorithm
     *
     * @param m0 number of initial nodes
     * @param m number of edges of each new node added during the network growth
     * @param n final number of nodes
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateBarabasiAlbert(int m0, int m, int n){
        BarabasiAlbertGraphGenerator<Integer, RelationshipEdge> barabasiGenerator = new BarabasiAlbertGraphGenerator<>(m0,m,n);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        barabasiGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     * Generate a random network using G(n,m) Erdos-Reniy algorithm
     *
     * @param n the number of nodes
     * @param m the number of edges
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateErdosReniyNM(int n, int m ){
        GnmRandomGraphGenerator<Integer, RelationshipEdge> ErdosReniyGenerator = new GnmRandomGraphGenerator<>(n,m, new Random(), false, true);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        ErdosReniyGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     *  Generate a random network using G(n,p) Erdos-Reniy algorithm
     *
     * @param n the number of nodes
     * @param p the edge probability
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateErdosReniyNP(int n, double p){
        GnpRandomGraphGenerator<Integer, RelationshipEdge> ErdosReniyGenerator = new GnpRandomGraphGenerator<>(n,p);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        ErdosReniyGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     * Generate a random network using Watt-Strogatz algorithm
     *
     * @param n the number of nodes
     * @param k connect each node to its k nearest neighbors in a ring
     * @param p the probability of re-wiring each edge
     * @return the generated random network
     *
     */
    public DirectedMultigraph generateWattStrogatz(int n, int k, double p) {
        WattsStrogatzGraphGenerator<Integer, RelationshipEdge> WattStrogatzGenerator = new WattsStrogatzGraphGenerator<>(n,k,p);
        DirectedMultigraph randomGraph = new DirectedMultigraph(vSupplier, SupplierUtil.createDefaultWeightedEdgeSupplier(), true);
        WattStrogatzGenerator.generateGraph(randomGraph);
        return  randomGraph;
    }

    /**
     *
     * Rewire the edges of the native network
     *
     * @param initialGraph the source network
     * @param ColorNumbers the number of colors used by the network edges
     * @return the rewired network
     *
     */
    public List<RewiringGraphEdges> generateRewiring(MutableValueGraph<Integer, Integer> initialGraph, int ColorNumbers) {
        MutableNetwork<Integer, Integer> newGraph = NetworkBuilder.directed().allowsParallelEdges(true).build();
        List<RewiringGraphEdges> newEdgeList = new ArrayList<>();
        Random random = new Random();

        int id=0;
        for (var edge : initialGraph.edges()) {
            int U = edge.nodeU();
            int R = random.nextInt(initialGraph.nodes().size());
            if (U == R || R % 2 == 0) R = edge.nodeV();
            newGraph.addEdge(U, R, ++id);
        }

        System.out.println(newGraph.edges().size());
        //O(n^2) (l'alternativa è ritornare direttamente il grafo
        for (int U : newGraph.nodes()) {
            for (int V : newGraph.nodes())
                for (int i = 0; i < newGraph.edgesConnecting(U, V).size(); i++)
                    newEdgeList.add(new RewiringGraphEdges(U, V, random.nextInt(ColorNumbers)));
        }

        //O(n) bug non tratta multigrafi
        //newGraph.asGraph().edges().forEach(i -> newEdgeList.add(new RewiringGraphEdges(i.nodeU(), i.nodeV(),random.nextInt(ColorNumbers))));
        return newEdgeList;
    }
}

