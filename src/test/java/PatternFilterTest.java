import cypher.controller.WhereConditionExtraction;
import cypher.models.QueryStructure;
import reading.FileManager;
import target_graph.propeties_idx.NodesEdgesLabelsMaps;
import tech.tablesaw.api.Table;

import java.util.Optional;


public class PatternFilterTest {
    public static void main(String [] args){
        // PATH
        String root_dir  = System.getProperty("user.dir");
        String netw_path = root_dir + "/Networks/Person";

        // TARGET READING
        Table[] nodes_tables            = FileManager.files_reading(netw_path + "/nodes", ',');
        Table[] edges_tables_properties = FileManager.files_reading(netw_path + "/edges", ',');

        // QUERY
        NodesEdgesLabelsMaps idx_label  = new NodesEdgesLabelsMaps();
        // String query = "MATCH (n1:Person)-[*2..3]->(n2:Person), (n2)<-[r1:college]-(n3:Person) WHERE NOT (n1)-[r2:college]->(n3) AND NOT (n1)-[r3:college]->(n2) RETURN COUNT(n1)";
        String query = "MATCH (n1:Person)-[*2..3]->(n2:Person), (n2)<-[r1:college]-(n3:Person) WHERE NOT (n2:Person {name:'Mario'})<-[r3:college]-(n1)-[r2:college]->(n3) RETURN COUNT(n1)";
        // String query = "MATCH (n1:Person)-[*2..3]->(n2:Person), (n2)<-[r1:college]-(n3:Person) WHERE NOT n1.name CONTAINS 'PIPPO' RETURN COUNT(n1)";

        //
        QueryStructure query_obj = new QueryStructure();
        WhereConditionExtraction where_managing = new WhereConditionExtraction();
        //where_managing.where_condition_extraction(query);
        //where_managing.normal_form_computing();
        //where_managing.buildSetWhereConditions();
        query_obj.parser(query, idx_label, nodes_tables, edges_tables_properties, Optional.of(where_managing));

    }
}
