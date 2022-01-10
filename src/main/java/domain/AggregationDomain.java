package domain;

import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;
import tech.tablesaw.selection.Selection;
import java.util.Optional;

public class AggregationDomain {
    // ATTRIBUTE
    private final Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Table>> query_target_assoc;
    private final Int2ObjectOpenHashMap<Int2IntOpenHashMap>           aggregate_domain;

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
        TargetBitmatrix target_matrix,
        QueryBitmatrix  query_matrix
    ) {
        Int2ObjectOpenHashMap<Table> q_idrow_t_src_dst = qrow_target_association(compatibility, target_matrix);
        query_matrix.getTable().forEach(row -> {
             Table corresponding_table = q_idrow_t_src_dst.get(row.getInt("btx_id"));
             if (!query_target_assoc.containsKey(row.getInt("src"))) {
                 query_target_assoc.put(row.getInt("src"), new Int2ObjectOpenHashMap<>());
                 aggregate_domain.put(row.getInt("src"),   new Int2IntOpenHashMap());
             }
             Int2ObjectOpenHashMap<Table> target_part = query_target_assoc.get(row.getInt("src"));
             Int2IntOpenHashMap           dst_cardin  = aggregate_domain.get(row.getInt("src"));
             if (target_part.containsKey(row.getInt("dst"))){
                 Table tmp = target_part.get(row.getInt("dst"));
                 corresponding_table = corresponding_table.append(tmp).dropDuplicateRows();
             }
             target_part.put(row.getInt("dst"), corresponding_table);
             dst_cardin.put( row.getInt("dst"), corresponding_table.rowCount());
        });
    }

    // GETTER
    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<Table>> getQuery_target_assoc() {return query_target_assoc;}
    public Int2ObjectOpenHashMap<Int2IntOpenHashMap>           getAggregate_domain()   {return aggregate_domain;  }
}
