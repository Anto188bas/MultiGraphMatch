package algorithms;
//Singleton
import org.opencypher.v9_0.ast.Query;
import org.opencypher.v9_0.parser.CypherParser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    private final String[] args;

    private final Pattern patternShortestPath = Pattern.compile("algorithms.shortestPath", Pattern.CASE_INSENSITIVE);
    private final Pattern patternColoredShortestPath = Pattern.compile("algorithms.coloredshortestPath", Pattern.CASE_INSENSITIVE);
    private final Pattern patternBetweenness = Pattern.compile("algorithms.betweenness", Pattern.CASE_INSENSITIVE);
    private final Pattern patternCloseness = Pattern.compile("algorithms.closeness", Pattern.CASE_INSENSITIVE);
    private final Pattern patternEigenVector= Pattern.compile("algorithms.eigenVector", Pattern.CASE_INSENSITIVE);
    private final Pattern patternKatz = Pattern.compile("algorithms.katz", Pattern.CASE_INSENSITIVE);
    private final Pattern patternPageRank = Pattern.compile("algorithms.pageRank", Pattern.CASE_INSENSITIVE);
    private final Pattern patternClusteringCoefficient = Pattern.compile("algorithms.clusteringCoefficient", Pattern.CASE_INSENSITIVE);
    private final Pattern patternAverageClusteringCoefficient = Pattern.compile("algorithms.averageClusteringCoefficient", Pattern.CASE_INSENSITIVE);
    private final Pattern patternKSpanningTree = Pattern.compile("algorithms.kSpanningTree", Pattern.CASE_INSENSITIVE);
    private final Pattern patternLabelPropagation = Pattern.compile("algorithms.labelPropagation", Pattern.CASE_INSENSITIVE);
    private final Pattern patternCommonNeighborsPrediction = Pattern.compile("algorithms.commonNeighborsPrediction", Pattern.CASE_INSENSITIVE);
    private final Pattern patternJaccardCoefficientPrediction = Pattern.compile("algorithms.jaccardCoefficientPrediction", Pattern.CASE_INSENSITIVE);
    private final Pattern patternPreferentialAttachmentPrediction = Pattern.compile("algorithms.preferentialAttachmentPrediction", Pattern.CASE_INSENSITIVE);

    private static QueryParser single_instance = null;

    private QueryParser(String[] args) {
        this.args = args;
    }

    public static QueryParser getInstance(String[] args){
        if(single_instance == null)
            single_instance = new QueryParser(args);
        return single_instance;
    }

    public void parser(String query) throws Exception{
        UtilityGraph utilityGraph = new UtilityGraph(args);
        Algorithms algorithms = new Algorithms(utilityGraph);

        if(patternShortestPath.matcher(query).find()){
            CypherParser parser = new CypherParser();
            Query query_object = (Query) parser.parse(query, null);
            String vertex= query_object.asCanonicalStringVal();
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String vertexA;
            if (matcher.find()) {
                 vertexA = matcher.group();
                if(matcher.find()) algorithms.DijsktraShortestPath(Integer.parseInt(vertexA), Integer.parseInt(matcher.group()));
                else algorithms.DijsktraAllShortestPath(Integer.parseInt(vertexA));
            }
        }else if(patternColoredShortestPath.matcher(query).find()){
            //TODO
            System.out.println("coloredShortestPath");
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
