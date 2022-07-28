package matching.models;

import cypher.models.QueryCondition;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class WhereConditionsData {
    public int conditionIndex = 0;
    public int numAnd = 0;
    public int numOr = 0;
    public ArrayList<QueryCondition> conditionList = new ArrayList<>();

    @Override
    public String toString() {
        return "WhereConditionsData{" +
                "conditionIndex=" + conditionIndex +
                ", numAnd=" + numAnd +
                ", numOr=" + numOr +
                ", conditionList=" + conditionList +
                '}';
    }
}
