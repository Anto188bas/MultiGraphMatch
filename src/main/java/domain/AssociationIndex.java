package domain;

import tech.tablesaw.api.IntColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

public class AssociationIndex {
    private Table    target_candidate;
    private Table    target_candidate_rev;
    private IntIndex src_index;
    private IntIndex dst_index;
    private IntIndex src_index_rev;
    private IntIndex dst_index_rev;

    public AssociationIndex(Table table){
        this.target_candidate = table;
    }

    public void add_new_associations(Table table) {
        target_candidate = target_candidate.append(table).dropDuplicateRows();
    }

    // NOTE: IF THE AGGREGATION IS BETWEEN Q_SRC-Q_DST THEN:
    //    Q_SRC->T_SRC AND Q_DST->T_DST IN target_candidate TABLE
    //    Q_SRC->T_DST AND Q_DST->T_SRC IN target_candidate_rev TABLE
    public void add_reverse(Table table) {
        if(table != null) {
            this.target_candidate_rev = table;
        } else {
            this.target_candidate_rev = Table.create().addColumns(IntColumn.create("src")).addColumns(IntColumn.create("dst"));
        }
    }

    // TABLE SIZE
    public int get_table_size() {return target_candidate.rowCount();     }
    public int get_reverse_sz() {return  target_candidate_rev.rowCount();}

    // SETTER
    public void index_configuration(){
       src_index     = new IntIndex(target_candidate.intColumn("src"    ));
       dst_index     = new IntIndex(target_candidate.intColumn("dst"    ));
       src_index_rev = new IntIndex(target_candidate_rev.intColumn("src"));
       dst_index_rev = new IntIndex(target_candidate_rev.intColumn("dst"));
    }

    // GETTER
    public Table    get_by_src(int src)         {return target_candidate.where(src_index.get(src));}
    public Table    get_by_dst(int dst)         {return target_candidate.where(dst_index.get(dst));}
    public Table    get_rev_by_src(int src)     {return target_candidate_rev.where(src_index_rev.get(src));}
    public Table    get_reb_by_dst(int dst)     {return target_candidate_rev.where(dst_index_rev.get(dst));}
    public Table    get_complete_table()        {return target_candidate;}
    public Table    get_complete_tab_rev()      {return target_candidate_rev;}
}
