package domain;

import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;
import tech.tablesaw.selection.Selection;
import java.util.Optional;


public class AggregationDomain {
    // ATTRIBUTE
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<AssociationIndex>> query_target_assoc;
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap>                      aggregate_domain;

    // CONSTRUCTOR
    public AggregationDomain(){
        query_target_assoc = new Int2ObjectOpenHashMap<>();
        aggregate_domain   = new Int2ObjectOpenHashMap<>();
    }

    // QUERY BITMATRIX ROWS - TARGET SRC-DST ASSOCIATION
    private Int2ObjectOpenHashMap<Table> qrow_target_association (
        Int2ObjectOpenHashMap<IntArrayList> compatibility,
        TargetBitmatrix target_matrix
    ){
        // DATA
        Int2ObjectOpenHashMap<Table> q_idrow_t_src_dst  = new Int2ObjectOpenHashMap<>(compatibility.size());
        IntIndex  bitmatrix_id_indexing                 = target_matrix.getBitmatrix_id_indexing();
        Table     bitmatrix_id_table                    = target_matrix.getTable();
        // PROCEDURE
        compatibility.int2ObjectEntrySet().fastForEach(record -> {
            int key = record.getIntKey();
            Optional<Selection> selections = record.getValue().stream()
               .map(bitmatrix_id_indexing::get)
               .reduce(Selection::or);
            if (selections.isPresent()){
                Table sub_table = bitmatrix_id_table
                   .where(selections.get())
                   .removeColumns("btx_id")
                   .dropDuplicateRows();
                q_idrow_t_src_dst.put(key, sub_table);
            }
        });
        return q_idrow_t_src_dst;
    }

    public void query_target_association(
        Int2ObjectOpenHashMap<IntArrayList> compatibility,
        TargetBitmatrix  target_matrix,
        QueryBitmatrix   query_matrix,
        QueryStructure   query_obj
    ) {
        Int2ObjectOpenHashMap<Table> q_idrow_t_src_dst = qrow_target_association(compatibility, target_matrix);
        // 1. AGGREGATE TARGET TABLEs BASED ON TARGET AND QUERY BIT MATRIX COMPATIBILITY
        query_matrix.getTable().forEach(row -> {
             Table corresponding_table = q_idrow_t_src_dst.get(row.getInt("btx_id"));
             if (!query_target_assoc.containsKey(row.getInt("src")))
                 query_target_assoc.put(row.getInt("src"), new Int2ObjectOpenHashMap<>());
             // SRC PART
             Int2ObjectOpenHashMap<AssociationIndex> target_part = query_target_assoc.get(row.getInt("src"));
             // COMPATIBILITY CONFIGURATION (DST PART CONFIGURATION)
             if (target_part.containsKey(row.getInt("dst")))
                 target_part.get(row.getInt("dst")).add_new_associations(corresponding_table);
             else
                 target_part.put(row.getInt("dst"), new AssociationIndex(corresponding_table));
        });
        // 2. REVERSE MERGING (WE PORT EVERYTHING IN SRC-DST)
        query_obj.getQuery_pattern().getQ_aggregation().forEach(record -> {
            AssociationIndex association = query_target_assoc.get(record[0]).get(record[1]);
            association.add_reverse(query_target_assoc.get(record[1]).remove(record[0]).get_complete_table());
        });
        // 3. DOMAIN CONFIGURATION
        query_target_assoc.int2ObjectEntrySet().fastForEach(record -> {
             if (record.getValue().size() != 0) {
                 aggregate_domain.put(record.getIntKey(), new Int2IntOpenHashMap());
                 record.getValue().int2ObjectEntrySet().fastForEach(dst_table -> {
                     AssociationIndex associations = dst_table.getValue();
                     aggregate_domain.get(
                         record.getIntKey()).put(dst_table.getIntKey(),
                        associations.get_table_size() + associations.get_reverse_sz()
                     );
                     dst_table.getValue().index_configuration();
                 });
             } else query_target_assoc.remove(record.getIntKey());
        });
    }

    // NEW (TEST)
    public void query_target_association_new(
            Int2ObjectOpenHashMap<IntArrayList> compatibility,
            TargetBitmatrix  target_matrix,
            QueryBitmatrix   query_matrix,
            QueryStructure   query_obj
    ) {


        query_matrix.getTable().forEach(row -> {
            query_obj.getQuery_pattern().getQ_aggregation();
        });
    }


    // GETTER
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<AssociationIndex>> getQuery_target_assoc() {return query_target_assoc;}
    public Int2ObjectOpenHashMap<Int2IntOpenHashMap> getAggregate_domain()   {return aggregate_domain;  }
}
