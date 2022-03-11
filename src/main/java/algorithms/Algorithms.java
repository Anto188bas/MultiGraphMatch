package algorithms;

import com.google.gson.Gson;
import org.jgrapht.Graph;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Algorithms {
    private FileWriter writer;
    final private Gson gson = new Gson();
    final private Graph<Integer, RelationshipEdge> graph;
    private ShortestPath s;
    private Centrality c;
    private Clustering C;
    private LinkPrediction Lp;

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

    public void DijsktraShortestPath(int source, int destination) throws IOException {
        s = new ShortestPath(graph);
        File DijkstraSP = new File("./OutputTest/ShortestPath/DijkstraSP.json");
        writer = new FileWriter(DijkstraSP);
        writer.write(gson.toJson(s.DijkstraSP(source, destination)));
        writer.flush();
        writer.close();
    }

    public void DijsktraAllShortestPath(int source) throws IOException {
        s = new ShortestPath(graph);
        File DijkstraAllSP = new File("./OutputTest/ShortestPath/DijkstraAllSP.json");
        writer = new FileWriter(DijkstraAllSP);
        writer.write(gson.toJson(s.DijkstraAllSP(source)));
        writer.flush();
        writer.close();
    }

    public void BellmanFordShortestPath(int source, int destination) throws IOException {
        s = new ShortestPath(graph);
        File BellmanFordSP = new File("./OutputTest/ShortestPath/BellmanFordSP.json");
        writer = new FileWriter(BellmanFordSP);
        writer.write(gson.toJson(s.BellmanFordSP(source, destination)));
        writer.flush();
        writer.close();
    }

    public void BellmanFordAllShortestPath(int source) throws  IOException {
        s = new ShortestPath(graph);
        File BellmanFordAllSP = new File("./OutputTest/ShortestPath/BellmanFordAllSP.json");
        writer = new FileWriter(BellmanFordAllSP);
        writer.write(gson.toJson(s.BellmanFordAllSP(source)));
        writer.flush();
        writer.close();
    }

    public void FloydWarshallShortestPath() throws IOException {
        s = new ShortestPath(graph);
        File FloydWarshallSP = new File("./OutputTest/ShortestPath/FloydWarshallSP.json");
        writer = new FileWriter(FloydWarshallSP);
        writer.write(gson.toJson(s.FloydWarshallSP()));
        writer.flush();
        writer.close();
    }

    public void JohnsonShortestPath(int source) throws IOException {
        s = new ShortestPath(graph);
        File JohnsonSP = new File("./OutputTest/ShortestPath/JohnsonSP.json");
        writer = new FileWriter(JohnsonSP);
        writer.write(gson.toJson(s.JohnsonSP(source)));
        writer.flush();
        writer.close();
    }



    public void BetweennessCentrality() throws IOException {
        c = new Centrality(graph);
        File Betweenness = new File("./OutputTest/Centrality/Betweenness.json");
        writer = new FileWriter(Betweenness);
        writer.write(gson.toJson(c.Betweenness()));
        writer.flush();
        writer.close();
    }

    public void ClosenessCentrality() throws IOException {
        c = new Centrality(graph);
        File Closeness = new File("./OutputTest/Centrality/Closeness.json");
        writer = new FileWriter(Closeness);
        writer.write(gson.toJson(c.Closeness()));
        writer.flush();
        writer.close();
    }

    public void EigenVectorCentrality() throws  IOException {
        c = new Centrality(graph);
        File EigenVector = new File("./OutputTest/Centrality/EigenVector.json");
        writer = new FileWriter(EigenVector);
        writer.write(gson.toJson(c.EigenVector()));
        writer.flush();
        writer.close();
    }

    public void PageRankCentrality() throws IOException {
        c = new Centrality(graph);
        File PageRank = new File("./OutputTest/Centrality/PageRank.json");
        writer = new FileWriter(PageRank);
        writer.write(gson.toJson(c.PageRank()));
        writer.flush();
        writer.close();
    }

    public void KatzCentrality() throws  IOException {
        c = new Centrality(graph);
        File Katz = new File("./OutputTest/Centrality/Katz.json");
        writer = new FileWriter(Katz);
        writer.write(gson.toJson(c.Katz()));
        writer.flush();
        writer.close();
    }

    public void ClusteringCoefficient() throws IOException {
        c = new Centrality(graph);
        File ClusteringCoefficient = new File("./OutputTest/Centrality/ClusteringCoefficient.json");
        writer = new FileWriter(ClusteringCoefficient);
        writer.write(gson.toJson(c.ClusteringCoefficient()));
        writer.flush();
        writer.close();
    }

    public void AverageClusteringCoefficient() throws IOException {
        c = new Centrality(graph);
        File AverageClusteringCoefficient = new File("./OutputTest/Centrality/AverageClusteringCoefficient.json");
        writer = new FileWriter(AverageClusteringCoefficient);
        writer.write(gson.toJson(c.AverageClusteringCoefficient()));
        writer.flush();
        writer.close();
    }

    public void KSpanningTreeClustering(int clusterNumber) throws IOException {
        C = new Clustering(graph, clusterNumber);
        File Clustering = new File("./OutputTest/Clustering/KSpanningTreeClustering.json");
        writer = new FileWriter(Clustering);
        writer.write(gson.toJson(C.KSpanningTree()));
        writer.flush();
        writer.close();
    }

    public void LabelPropagationClustering() throws IOException {
        C = new Clustering(graph);
        File Clustering = new File("./OutputTest/Clustering/LabelPropagationClustering.json");
        writer = new FileWriter(Clustering);
        writer.write(gson.toJson(C.LabelPropagation()));
        writer.flush();
        writer.close();
    }

    public void PreferentialAttachmentPrediction(int u, int v) throws IOException {
        Lp = new LinkPrediction(graph);
        File LinkPrediction = new File("./OutputTest/LinkPrediction/PreferentialAttachmentPrediction.json");
        writer = new FileWriter(LinkPrediction);
        writer.write(gson.toJson(Lp.PreferentialAttachmentPrediction(u,v)));
        writer.flush();
        writer.close();
    }

    public void CommonNeighborsPrediction(int u, int v) throws IOException {
        Lp = new LinkPrediction(graph);
        File LinkPrediction = new File("./OutputTest/LinkPrediction/CommonNeighborsPrediction.json");
        writer = new FileWriter(LinkPrediction);
        writer.write(gson.toJson(Lp.CommonNeighborsPrediction(u,v)));
        writer.flush();
        writer.close();
    }

    public void JaccardCoefficientPrediction(int u, int v) throws IOException {
        Lp = new LinkPrediction(graph);
        File LinkPrediction = new File("./OutputTest/LinkPrediction/JaccardCoefficientPrediction.json");
        writer = new FileWriter(LinkPrediction);
        writer.write(gson.toJson(Lp.JaccardCoefficientPrediction(u,v)));
        writer.flush();
        writer.close();
    }
}
