package algorithms;
//Singleton
import cypher.models.QueryStructure;
import org.opencypher.v9_0.ast.Query;
import org.opencypher.v9_0.parser.CypherParser;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    private final String query;
    private final String[] args;

    private final Pattern patternShortestPath = Pattern.compile("shortestPath", Pattern.CASE_INSENSITIVE);
    private final Pattern patternColoredShortestPath = Pattern.compile("coloredshortestPath", Pattern.CASE_INSENSITIVE);
    private final Pattern patternBetweenness = Pattern.compile("betweenness", Pattern.CASE_INSENSITIVE);
    private final Pattern patternCloseness = Pattern.compile("closeness", Pattern.CASE_INSENSITIVE);
    private final Pattern patternEigenVector= Pattern.compile("eigenVector", Pattern.CASE_INSENSITIVE);
    private final Pattern patternKatz = Pattern.compile("katz", Pattern.CASE_INSENSITIVE);
    private final Pattern patternPageRank = Pattern.compile("pageRank", Pattern.CASE_INSENSITIVE);
    private final Pattern patternClusteringCoefficient = Pattern.compile("clusteringCoefficient", Pattern.CASE_INSENSITIVE);
    private final Pattern patternAverageClusteringCoefficient = Pattern.compile("averageClusteringCoefficient", Pattern.CASE_INSENSITIVE);
    private final Pattern patternKSpanningTree = Pattern.compile("kSpanningTree", Pattern.CASE_INSENSITIVE);
    private final Pattern patternLabelPropagation = Pattern.compile("labelPropagation", Pattern.CASE_INSENSITIVE);
    private final Pattern patternCommonNeighborsPrediction = Pattern.compile("commonNeighborsPrediction", Pattern.CASE_INSENSITIVE);
    private final Pattern patternJaccardCoefficientPrediction = Pattern.compile("jaccardCoefficientPrediction", Pattern.CASE_INSENSITIVE);
    private final Pattern patternPreferentialAttachmentPrediction = Pattern.compile("preferentialAttachmentPrediction", Pattern.CASE_INSENSITIVE);

    private static QueryParser single_instance = null;

    private QueryParser(String query, String[] args) {
        this.query = query;
        this.args = args;
    }

    public static QueryParser getInstance(String query, String[] args){
        if(single_instance == null)
            single_instance = new QueryParser(query, args);
        return single_instance;
    }

    private static void create_node_colors(NodesEdgesLabelsMaps labels) {
        labels.stringVectorToIntOne("YELLOW");
        labels.stringVectorToIntOne("GREEN" );
        labels.stringVectorToIntOne("PURPLE");
    }

    private static void create_edge_colors(NodesEdgesLabelsMaps types) {
        types.createEdgeLabelIdx("RED"   );
        types.createEdgeLabelIdx("BLUE"  );
        types.createEdgeLabelIdx("ORANGE");
    }

    public void parser() throws Exception{
        UtilityGraph utilityGraph = new UtilityGraph(args);
        Algorithms algorithms = new Algorithms(utilityGraph);

        NodesEdgesLabelsMaps nodes_edges_labels = new NodesEdgesLabelsMaps();
        create_node_colors(nodes_edges_labels);
        create_edge_colors(nodes_edges_labels);

        QueryStructure query_obj = new QueryStructure();
        query_obj.parser(query, nodes_edges_labels);

        System.out.println(query_obj);


        if(patternShortestPath.matcher(query).find()){
            //TODO implement single and All calls
            System.out.println("shortestPath");
        }else if(patternColoredShortestPath.matcher(query).find()){
            //TODO implement single and All calls
            System.out.println("ColoredShortestPath");
        }else if(patternBetweenness.matcher(query).find()){
            algorithms.BetweennessCentrality();
        }else if(patternCloseness.matcher(query).find()){
            algorithms.ClosenessCentrality();
        }else if(patternEigenVector.matcher(query).find()){
            algorithms.EigenVectorCentrality();
        }else if(patternKatz.matcher(query).find()){
            algorithms.KatzCentrality();
        }else if(patternPageRank.matcher(query).find()){
            algorithms.PageRankCentrality();
        }else if(patternClusteringCoefficient.matcher(query).find()){
            algorithms.ClusteringCoefficient();
        }else if(patternAverageClusteringCoefficient.matcher(query).find()){
            algorithms.AverageClusteringCoefficient();
        }else if(patternKSpanningTree.matcher(query).find()){
            CypherParser parser = new CypherParser();
            Query query_object = (Query) parser.parse(query, null);
            String clusterNumber= query_object.asCanonicalStringVal();
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(clusterNumber);
            if (matcher.find()) { algorithms.KSpanningTreeClustering(Integer.parseInt(matcher.group(0))); }
        }else if(patternLabelPropagation.matcher(query).find()){
            algorithms.LabelPropagationClustering();
        }else if(patternCommonNeighborsPrediction.matcher(query).find()){
            CypherParser parser = new CypherParser();
            Query query_object = (Query) parser.parse(query, null);
            String vertex= query_object.asCanonicalStringVal();
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            if (matcher.find()) { algorithms.CommonNeighborsPrediction(Integer.parseInt(matcher.group()), Integer.parseInt(matcher.group())); }
        }else if(patternJaccardCoefficientPrediction.matcher(query).find()){
            CypherParser parser = new CypherParser();
            Query query_object = (Query) parser.parse(query, null);
            String vertex= query_object.asCanonicalStringVal();
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            if (matcher.find()) { algorithms.JaccardCoefficientPrediction(Integer.parseInt(matcher.group()), Integer.parseInt(matcher.group())); }
        }else if(patternPreferentialAttachmentPrediction.matcher(query).find()){
            CypherParser parser = new CypherParser();
            Query query_object = (Query) parser.parse(query, null);
            String vertex= query_object.asCanonicalStringVal();
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            if (matcher.find()) { algorithms.PreferentialAttachmentPrediction(Integer.parseInt(matcher.group()), Integer.parseInt(matcher.group())); }
        }else{
            System.out.println("Invalid Syntax\n");
        }
    }


}
