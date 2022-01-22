package bitmatrix.controller;

import bitmatrix.models.QueryBitmatrix;
import bitmatrix.models.TargetBitmatrix;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import tech.tablesaw.api.Table;
import tech.tablesaw.index.IntIndex;

import java.util.ArrayList;
import java.util.BitSet;

public class BitmatrixManager {
    /*
    *  -- CASE 1:
    *    If the query is a directed one we have to do a normal check between target bitset and query one.
    *  -- CASE 2:
    *    Otherwise, we need to consider the third block into the query bitset (IN/OUT).
    *    Therefore, in that case we have to verify if an in or out bit can also be into IN/OUT
    */
    public static Int2ObjectOpenHashMap<IntArrayList> bitmatrix_manager(
        QueryBitmatrix  query_bitmatrix,
        TargetBitmatrix target_bitmatrix
    ) {
        boolean is_directed = query_bitmatrix.isIs_directed();
        Int2ObjectOpenHashMap<IntArrayList> qaggr_taggr_assoc = new Int2ObjectOpenHashMap<>();
        ArrayList<BitSet> qmatrix = query_bitmatrix.getBitmatrix();
        ArrayList<BitSet> tmatrix = target_bitmatrix.getBitmatrix();
        boolean is_compatible;

        // CASE 1
        // TODO: WE FIXED THE REVERSE CASE, SO WE HAVE TO DO A COMBINED CHECK SRC-DST AND DST-SRC TO REDUCE THE CONTROL NUMBER
        if (is_directed) {
           for (int i = 0; i < qmatrix.size(); i++){
               IntArrayList compatible_record = new IntArrayList();
               BitSet row_i = qmatrix.get(i);
               int k;
               for (int j = 0; j < tmatrix.size(); j++){
                   BitSet row_j  = tmatrix.get(j);
                   is_compatible = true;
                   for (k = row_i.nextSetBit(0); k != -1; k = row_i.nextSetBit(k + 1)) {
                        is_compatible = row_j.get(k);
                        if(!is_compatible) break;
                   }
                   if(is_compatible) compatible_record.add(j);
               }
               qaggr_taggr_assoc.put(i, compatible_record);
           }
        }

        // CASE 2
        // TODO implement me

        return qaggr_taggr_assoc;
    }
}
