import algorithms.Algorithms;
import algorithms.UtilityGraph;
import java.io.IOException;

public class algorithms_test {
    public static void main(String[] args){
        /*
        //NEW STRUCTURE
        int nPairs = graphPaths.getNum_pairs();
        int nEdgeColors = graphPaths.getNum_edge_colors();
        System.out.println(nPairs);
        System.out.println(nEdgeColors);

        var mat = graphPaths.getMap_key_to_edge_list();

        for(int i=0; i<nPairs; i++){
            System.out.println();
            for(int j=0; j<nEdgeColors; j++){
                System.out.println("Pair_id: "+i+", color_id: "+j+": "+mat[i][j]);
            }
        }

        System.out.println("Pair_id: "+994949+", color_id: "+0+", edge label: "+mat[994949][0]);
        System.out.println("Pair_id: "+994949+", color_id: "+1+", edge label: "+mat[994949][1]);
        System.out.println("Pair_id: "+994949+", color_id: "+2+", edge label: "+mat[994949][2]);
        System.out.println("Pair_id: "+994949+", color_id: "+3+", edge label: "+mat[994949][3]);
        System.out.println("Pair_id: "+994949+", color_id: "+4+", edge label: "+mat[994949][4]);
        System.out.println("Pair_id: "+994949+", color_id: "+5+", edge label: "+mat[994949][5]);
        System.out.println("Pair_id: "+994949+", color_id: "+6+", edge label: "+mat[994949][6]);
        System.out.println("Pair_id: "+994949+", color_id: "+7+", edge label: "+mat[994949][7]);
        System.out.println("Pair_id: "+994949+", color_id: "+8+", edge label: "+mat[994949][8]);
        System.out.println("Pair_id: "+994949+", color_id: "+9+", edge label: "+mat[994949][9]);

        int pair_id = graphPaths.getMap_pair_to_key().get(9984).get(9999);
        System.out.println("src: 9984, dst: 9999: pair_id: "+pair_id);

        */ //NEW STRUCTURE

        //Multithreading algorithm testing

        long startTime = System.nanoTime();

        UtilityGraph utilityGraph = new UtilityGraph(args);

        long endTime = System.nanoTime();
        System.out.println(endTime - startTime);

        //System.out.println(utilityGraph.getVGraph().edges());
        //System.out.println(utilityGraph.getJGraph().edgeSet());

        System.out.println("...Testing begin...");
        Runnable runnableColoredShortestPath =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.ColoredShortestPath(254, 431, 2);
                        a.AllColoredShortestPath(254, 431);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableShortestPath =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.DijsktraShortestPath(254,431);
                        a.BellmanFordShortestPath(0,3);
                        a.DijsktraAllShortestPath(0);
                        a.BellmanFordAllShortestPath(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableCentrality =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.EigenVectorCentrality();
                        a.BetweennessCentrality();
                        a.ClosenessCentrality();
                        a.PageRankCentrality();
                        a.KatzCentrality();
                        a.ClusteringCoefficient();
                        a.AverageClusteringCoefficient();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableClustering =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.LabelPropagationClustering();
                        a.KSpanningTreeClustering(3);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };

        Runnable runnableLinkPrediction =
                () -> {
                    Algorithms a = new Algorithms(utilityGraph);
                    try {
                        a.PreferentialAttachmentPrediction(0,1);
                        a.CommonNeighborsPrediction(0,1);
                        a.JaccardCoefficientPrediction(0,1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                };


        Thread threadColoredShortestPath = new Thread(runnableColoredShortestPath);
        Thread threadShortestPath = new Thread(runnableShortestPath);
        Thread threadCentrality = new Thread(runnableCentrality);
        Thread threadClustering = new Thread(runnableClustering);
        Thread threadLinkPrediction = new Thread(runnableLinkPrediction);

        threadColoredShortestPath.start();
        threadShortestPath.start();
        threadCentrality.start();
        threadClustering.start();
        threadLinkPrediction.start();
    }

}
