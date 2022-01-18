package domain;

import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

public class AssociationIndex {
    private Table    target_candidate;
    private IntIndex src_index;
    private IntIndex dst_index;

    public AssociationIndex(Table table){
        this.target_candidate = table;
    }

    public void add_new_associations(Table table) {
        target_candidate = target_candidate.append(table).dropDuplicateRows();
    }

    public int get_table_size() {
        return target_candidate.rowCount();
    }

    // SETTER
    public void index_configuration(){
       src_index = new IntIndex(target_candidate.intColumn("src"));
       dst_index = new IntIndex(target_candidate.intColumn("dst"));
    }

    // GETTER
    public Table get_by_src(int src) {return target_candidate.where(src_index.get(src));}
    public Table get_by_dst(int dst) {return target_candidate.where(dst_index.get(dst));}
    public Table get_complete_table(){return target_candidate;}
}
