import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryStructure;
import reading.FileManager;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;
import java.util.function.Function;

public class test_where {
    Function<Integer, Boolean> Equals = (x) -> x == 10;
    public boolean comparisonTest(int num, Function<Integer, Boolean> function) {
        return function.apply(num);
    }


    public static void main(String[] args){
        // PATH
        String root_dir  = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/Person";

        // NETWORK
        Table[] nodes_tables            = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edges_tables_properties = FileManager.files_reading(netw_path + "/edges", ',');

        String query_test           = "MATCH (n1:Persona)-[r1:friends]->(n2:Persona) WHERE (n1.name <> n2.name AND NOT n1.name IN [\"Antonio\", \"Paolo\"]) OR (n2.name <> \"Franco\" AND n1.age > 18) RETURN n1,n2";
        NodesEdgesLabelsMaps labels = new NodesEdgesLabelsMaps();
        labels.stringVectorToIntOne("Persona");
        labels.createEdgeLabelIdx("friends");

        WhereConditionExtraction where_managing = new WhereConditionExtraction();
        where_managing.where_condition_extraction(query_test);
        where_managing.normal_form_computing();
        QueryStructure query = new QueryStructure();
        query.parser(query_test, labels, nodes_tables, edges_tables_properties);
    }
}
