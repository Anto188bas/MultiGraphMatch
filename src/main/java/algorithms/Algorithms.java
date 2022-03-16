package algorithms;

import algorithms.algorithmsUtility.RelationshipEdge;
import com.google.gson.Gson;
import org.jgrapht.Graph;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * This class is and interface which unify all the operation of the package
 *
 */

public class Algorithms {

    private FileWriter writer;
    final private Gson gson = new Gson();
    final private Graph<Integer, RelationshipEdge> graph;
    private ShortestPath shortestPath;
    private Centrality centrality;
    private Clustering clustering;
    private LinkPrediction linkPrediction;

    /**
     *
     * Class constructor, constuct a Algorithms object
     *
     * @param graph the input network
     *
     */
    public Algorithms(Graph<Integer, RelationshipEdge> graph) {
        //create output directory
        File ShortestPathDir = new File("./OutputTest/ShortestPath");
        if (!ShortestPathDir.exists()){ ShortestPathDir.mkdirs(); }
        File CentralityDir = new File("./OutputTest/Centrality");
        if (!CentralityDir.exists()){ CentralityDir.mkdirs(); }
        File ClusteringDir = new File("./OutputTest/Clustering");
        if (!ClusteringDir.exists()){ ClusteringDir.mkdirs(); }
        File LinkPredictionDir = new File("./OutputTest/LinkPrediction");
        if (!LinkPredictionDir.exists()){ LinkPredictionDir.mkdirs(); }

        this.graph = graph;
    }

    /**
     *
     * Invoke the DijsktraSP method from the class ShortestPath, convert to json format the output and save it on the DijkstraSP.json file
     *
     * @param source the source vertex id
     * @param destination the destination vertex id
     * @throws IOException if the directory "ShortestPath" doesn't exist
     *
     */
    public void DijsktraShortestPath(int source, int destination) throws IOException {
        shortestPath = new ShortestPath(graph);
        File DijkstraSP = new File("./OutputTest/ShortestPath/DijkstraSP.json");
        writer = new FileWriter(DijkstraSP);
        writer.write(gson.toJson(shortestPath.DijkstraSP(source, destination)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the DijsktraAllSP method from the class ShortestPath, convert to json format the output and save it on the DijkstraAllSP.json file
     *
     * @param source the source vertex id
     * @throws IOException if the directory "ShortestPath" doesn't exist
     *
     */
    public void DijsktraAllShortestPath(int source) throws IOException {
        shortestPath = new ShortestPath(graph);
        File DijkstraAllSP = new File("./OutputTest/ShortestPath/DijkstraAllSP.json");
        writer = new FileWriter(DijkstraAllSP);
        writer.write(gson.toJson(shortestPath.DijkstraAllSP(source)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the BellmanFordSP method from the class ShortestPath, convert to json format the output and save it on the BellmanFordSP.json file
     *
     * @param source the source vertex id
     * @param destination the destination vertex id
     * @throws IOException if the directory "ShortestPath" doesn't exist
     *
     */
    public void BellmanFordShortestPath(int source, int destination) throws IOException {
        shortestPath = new ShortestPath(graph);
        File BellmanFordSP = new File("./OutputTest/ShortestPath/BellmanFordSP.json");
        writer = new FileWriter(BellmanFordSP);
        writer.write(gson.toJson(shortestPath.BellmanFordSP(source, destination)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the BellmanFordAllSP method from the class ShortestPath, convert to json format the output and save it on the BellmanFordAllSP.json file
     *
     * @param source the source vertex id
     * @throws IOException if the directory "ShortestPath" doesn't exist
     *
     */
    public void BellmanFordAllShortestPath(int source) throws  IOException {
        shortestPath = new ShortestPath(graph);
        File BellmanFordAllSP = new File("./OutputTest/ShortestPath/BellmanFordAllSP.json");
        writer = new FileWriter(BellmanFordAllSP);
        writer.write(gson.toJson(shortestPath.BellmanFordAllSP(source)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the Betweenness method from the class Centrality, convert to json format the output and save it on the Betweenness.json file
     *
     * @throws IOException if the directory "Centrality" doesn't exist
     *
     */
    public void BetweennessCentrality() throws IOException {
        centrality = new Centrality(graph);
        File Betweenness = new File("./OutputTest/Centrality/Betweenness.json");
        writer = new FileWriter(Betweenness);
        writer.write(gson.toJson(centrality.Betweenness()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the Closeness method from the class Centrality, convert to json format the output and save it on the Closeness.json file
     *
     * @throws IOException if the directory "Centrality" doesn't exist
     *
     */
    public void ClosenessCentrality() throws IOException {
        centrality = new Centrality(graph);
        File Closeness = new File("./OutputTest/Centrality/Closeness.json");
        writer = new FileWriter(Closeness);
        writer.write(gson.toJson(centrality.Closeness()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the EigenVector method from the class Centrality, convert to json format the output and save it on the EigenVector.json file
     *
     * @throws IOException if the directory "Centrality" doesn't exist
     *
     */
    public void EigenVectorCentrality() throws  IOException {
        centrality = new Centrality(graph);
        File EigenVector = new File("./OutputTest/Centrality/EigenVector.json");
        writer = new FileWriter(EigenVector);
        writer.write(gson.toJson(centrality.EigenVector()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the PageRank method from the class Centrality, convert to json format the output and save it on the PageRank.json file
     *
     * @throws IOException if the directory "Centrality" doesn't exist
     *
     */
    public void PageRankCentrality() throws IOException {
        centrality = new Centrality(graph);
        File PageRank = new File("./OutputTest/Centrality/PageRank.json");
        writer = new FileWriter(PageRank);
        writer.write(gson.toJson(centrality.PageRank()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the Katz method from the class Centrality, convert to json format the output and save it on the Katz.json file
     *
     * @throws IOException if the directory "Centrality" doesn't exist
     *
     */
    public void KatzCentrality() throws  IOException {
        centrality = new Centrality(graph);
        File Katz = new File("./OutputTest/Centrality/Katz.json");
        writer = new FileWriter(Katz);
        writer.write(gson.toJson(centrality.Katz()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the ClusteringCoefficient method from the class Centrality, convert to json format the output and save it on the ClusteringCoefficient.json file
     *
     * @throws IOException if the directory"Centrality"  doesn't exist
     *
     */
    public void ClusteringCoefficient() throws IOException {
        centrality = new Centrality(graph);
        File ClusteringCoefficient = new File("./OutputTest/Centrality/ClusteringCoefficient.json");
        writer = new FileWriter(ClusteringCoefficient);
        writer.write(gson.toJson(centrality.ClusteringCoefficient()));
        writer.flush();
        writer.close();
    }

    /**
     *
     *  Invoke the AverageClusteringCoefficient method from the class Centrality, convert to json format the output and save it on the AverageClusteringCoefficient.json file
     *
     * @throws IOException if the directory "Centrality" doesn't exist
     *
     */
    public void AverageClusteringCoefficient() throws IOException {
        centrality = new Centrality(graph);
        File AverageClusteringCoefficient = new File("./OutputTest/Centrality/AverageClusteringCoefficient.json");
        writer = new FileWriter(AverageClusteringCoefficient);
        writer.write(gson.toJson(centrality.AverageClusteringCoefficient()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the KSpanningTree method from the class Clustering, convert to json format the output and save it on the KSpanningTreeClustering.json file
     *
     * @param clusterNumber the desired number of cluster in output
     * @throws IOException if the directory "Clustering" doesn't exist
     *
     */
    public void KSpanningTreeClustering(int clusterNumber) throws IOException {
        clustering = new Clustering(graph, clusterNumber);
        File Clustering = new File("./OutputTest/Clustering/KSpanningTreeClustering.json");
        writer = new FileWriter(Clustering);
        writer.write(gson.toJson(clustering.KSpanningTree()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the LabelPropagation method from the class Clustering, convert to json format the output and save it on the LabelPropagation.json file
     *
     * @throws IOException if the directory "Clustering" doesn't exist
     *
     */
    public void LabelPropagationClustering() throws IOException {
        clustering = new Clustering(graph);
        File Clustering = new File("./OutputTest/Clustering/LabelPropagationClustering.json");
        writer = new FileWriter(Clustering);
        writer.write(gson.toJson(clustering.LabelPropagation()));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the PreferentialAttachmentPrediction method from the class LinkPrediction, convert to json format the output and save it on the PreferentialAttachmentPrediction.json file
     *
     * @param u the source vertex id
     * @param v the destination vertex id
     * @throws IOException if the directory "LinkPrediction" doesn't exist
     *
     */
    public void PreferentialAttachmentPrediction(int u, int v) throws IOException {
        linkPrediction = new LinkPrediction(graph);
        File LinkPrediction = new File("./OutputTest/LinkPrediction/PreferentialAttachmentPrediction.json");
        writer = new FileWriter(LinkPrediction);
        writer.write(gson.toJson(linkPrediction.PreferentialAttachmentPrediction(u,v)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the CommonNeighborsPrediction method from the class LinkPrediction, convert to json format the output and save it on the CommonNeighborsPrediction.json file
     *
     * @param u the source vertex id
     * @param v the destination vertex id
     * @throws IOException if the directory "LinkPrediction" doesn't exist
     *
     */
    public void CommonNeighborsPrediction(int u, int v) throws IOException {
        linkPrediction = new LinkPrediction(graph);
        File LinkPrediction = new File("./OutputTest/LinkPrediction/CommonNeighborsPrediction.json");
        writer = new FileWriter(LinkPrediction);
        writer.write(gson.toJson(linkPrediction.CommonNeighborsPrediction(u,v)));
        writer.flush();
        writer.close();
    }

    /**
     *
     * Invoke the JaccardCoefficientPrediction method from the class LinkPrediction, convert to json format the output and save it on the JaccardCoefficientPrediction.json file
     *
     * @param u the source vertex id
     * @param v the destination vertex id
     * @throws IOException if the directory "LinkPrediction" doesn't exist
     *
     */
    public void JaccardCoefficientPrediction(int u, int v) throws IOException {
        linkPrediction = new LinkPrediction(graph);
        File LinkPrediction = new File("./OutputTest/LinkPrediction/JaccardCoefficientPrediction.json");
        writer = new FileWriter(LinkPrediction);
        writer.write(gson.toJson(linkPrediction.JaccardCoefficientPrediction(u,v)));
        writer.flush();
        writer.close();
    }
}
