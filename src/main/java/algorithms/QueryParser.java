package algorithms;

import org.opencypher.v9_0.ast.Query;
import org.opencypher.v9_0.parser.CypherParser;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryParser {
    private final Algorithms algorithms;

    private final Pattern patternShortestPath = Pattern.compile("algorithms.shortestPath", Pattern.CASE_INSENSITIVE);
    private final Pattern patternColoredShortestPath = Pattern.compile("algorithms.coloredShortestPath", Pattern.CASE_INSENSITIVE);
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
    private final Pattern patternErdosReniyNM = Pattern.compile("algorithms.erdosReniyNMGenerator", Pattern.CASE_INSENSITIVE);
    private final Pattern patternErdosReniyNP = Pattern.compile("algorithms.erdosReniyNPGenerator", Pattern.CASE_INSENSITIVE);
    private final Pattern patternWattStrogatz = Pattern.compile("algorithms.wattsStrogatzGenerator", Pattern.CASE_INSENSITIVE);
    private final Pattern patternBarabasiAlbert = Pattern.compile("algorithms.BarabasiAlbertGenerator", Pattern.CASE_INSENSITIVE);
    private final Pattern patternRewiring = Pattern.compile("algorithms.rewireGraph", Pattern.CASE_INSENSITIVE);
    private final Pattern patternEdgeSwapping = Pattern.compile("algorithms.edgeSwapping", Pattern.CASE_INSENSITIVE);

    int QueryParsed;

    public QueryParser(String[] args) {
        UtilityGraph utilityGraph = new UtilityGraph(args);
        algorithms = new Algorithms(utilityGraph);
        QueryParsed=0;
    }

    public void parser(String query) throws Exception {
        QueryParsed++;
        if(patternShortestPath.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String vertexA;
            if (matcher.find()) {
                 vertexA = matcher.group();
                if(matcher.find()) algorithms.DijsktraShortestPath(Integer.parseInt(vertexA), Integer.parseInt(matcher.group()));
                else algorithms.DijsktraAllShortestPath(Integer.parseInt(vertexA));
            }
        }else if(patternColoredShortestPath.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String vertexA, vertexOrColor = null;
            if(matcher.find()){
                vertexA = matcher.group();
                if(matcher.find()) vertexOrColor = matcher.group();
                if(matcher.find())
                    algorithms.ColoredShortestPath(Integer.parseInt(vertexA), Integer.parseInt(Objects.requireNonNull(vertexOrColor)), Integer.parseInt(matcher.group()));
                else
                    algorithms.AllColoredShortestPath(Integer.parseInt(vertexA), Integer.parseInt(Objects.requireNonNull(vertexOrColor)));
            }
        }else if(patternBetweenness.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.BetweennessCentrality();
        }else if(patternCloseness.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.ClosenessCentrality();
        }else if(patternEigenVector.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.EigenVectorCentrality();
        }else if(patternKatz.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.KatzCentrality();
        }else if(patternPageRank.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.PageRankCentrality();
        }else if(patternClusteringCoefficient.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.ClusteringCoefficient();
        }else if(patternAverageClusteringCoefficient.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.AverageClusteringCoefficient();
        }else if(patternKSpanningTree.matcher(query).find()) {
            String clusterNumber = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(clusterNumber);
            if (matcher.find()) { algorithms.KSpanningTreeClustering(Integer.parseInt(matcher.group(0))); }
        }else if(patternLabelPropagation.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.LabelPropagationClustering();
        }else if(patternCommonNeighborsPrediction.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String v0 = null;
            if (matcher.find() ) { v0 = matcher.group(); }
            if(matcher.find()) {
                assert v0 != null;
                algorithms.CommonNeighborsPrediction(Integer.parseInt(v0), Integer.parseInt(matcher.group()));
            }
        }else if(patternJaccardCoefficientPrediction.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String v0 = null;
            if(matcher.find()) { v0 = matcher.group(); }
            if (matcher.find()) {
                assert v0 != null;
                algorithms.JaccardCoefficientPrediction(Integer.parseInt(v0), Integer.parseInt(matcher.group()));
            }
        }else if(patternPreferentialAttachmentPrediction.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String v0 = null;
            if(matcher.find()) { v0 = matcher.group(); }
            if (matcher.find()) {
                assert v0 != null;
                algorithms.PreferentialAttachmentPrediction(Integer.parseInt(v0), Integer.parseInt(matcher.group()));
            }
        }else if(patternErdosReniyNM.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            if (matcher.find()) { algorithms.GeneratorErdosReniyNM(Integer.parseInt(matcher.group()), Integer.parseInt(matcher.group())); }
        }else if(patternErdosReniyNP.matcher(query).find()){
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            Pattern pattern2 = Pattern.compile("0.[0-9]");
            Matcher matcher2 = pattern2.matcher(vertex);
            if (matcher.find() && matcher2.find()) { algorithms.GeneratorErdosReniyNP(Integer.parseInt(matcher.group()), Double.parseDouble(matcher2.group())); }
        }else if(patternWattStrogatz.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            Pattern pattern2 = Pattern.compile("0.[0-9]");
            Matcher matcher2 = pattern2.matcher(vertex);
            String n, k =null;
            if(matcher.find()){
                n = matcher.group();
                if(matcher.find())  k = matcher.group();
                if (matcher2.find()) {
                    assert k != null;
                    algorithms.GeneratorWattsStrogatz(Integer.parseInt(n),Integer.parseInt(k), Double.parseDouble(matcher2.group()));
                }
            }
        }else if(patternBarabasiAlbert.matcher(query).find()) {
            String vertex = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(vertex);
            String m0 = null, m = null;
            if(matcher.find()) m0 = matcher.group();
            if(matcher.find()) m = matcher.group();
            if(matcher.find()){
                assert m0 != null && m != null;
                algorithms.GeneratorBarabasiAlbert(Integer.parseInt(m0), Integer.parseInt(m), Integer.parseInt(matcher.group()));
            }
        }else if(patternRewiring.matcher(query).find()) {
            ChyperSyntaxChecker(query);
            algorithms.GeneratorRewiring();
        }else if(patternEdgeSwapping.matcher(query).find()) {
            String nGraph = ChyperSyntaxChecker(query);
            Pattern pattern = Pattern.compile("[0-9]+");
            Matcher matcher = pattern.matcher(nGraph);
            if(matcher.find()) {
                for (int i = 0; i < Integer.parseInt(matcher.group()); i++)
                    algorithms.GeneratorEdgeSwapping(i);
            }else { algorithms.GeneratorEdgeSwapping(1); }
        }else{
            System.out.println("\u001B[31m"+"Error handling: "+'"'+query+'"'+" Invalid syntax!\n"+"\u001B[0m");
        }
    }

    /**
     *
     * Check if the input query respect the Chyper syntax
     *
     * @param query the input query
     * @return an openChyper query string format
     *
     */
    public String ChyperSyntaxChecker(String query){
        CypherParser parser = new CypherParser();
        Query query_object = (Query) parser.parse(query, null);
        return query_object.asCanonicalStringVal();
    }

    /**
     *
     * query parsed getter
     *
     * @return the number of queries parsed
     *
     */
    public int getQueryParsed(){ return QueryParsed; }
}
