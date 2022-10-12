package matching.controllers;

import cypher.models.NameValue;
import cypher.models.QueryCondition;
import cypher.models.QueryStructure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class WhereUtils {
    public static void assignSimpleConditionsToNodesAndEdges(ObjectArrayList<QueryCondition> simpleConditions, QueryStructure queryStructure) {
        simpleConditions.forEach((condition -> {
            condition.assignSimpleCondition(queryStructure);
        }));
    }

    public static void assignComplexConditionsToNodesAndEdges(ObjectArrayList<QueryCondition> complexConditions, QueryStructure queryStructure, IntArrayList nodesOrdering, IntArrayList edgesOrdering) {
        complexConditions.forEach((condition -> {
            condition.assignComplexCondition(queryStructure, nodesOrdering, edgesOrdering);
        }));
    }
    public static boolean quickCheckNodeCondition(int targetID, int sourceID, QueryCondition condition, QueryStructure queryStructure) {
        String operator = condition.getOperation();
        String elementName = condition.getNode_param().getElementName();
        String propertyName = condition.getNode_param().getElementKey();
        Object expressionValue = condition.getExpr_value();
        Object candidateValue = condition.getConditionCheck().getProperty(targetID, propertyName);
        boolean res = condition.getConditionCheck().getComparison().comparison(candidateValue, expressionValue, operator);

        boolean negate = condition.isNegation();

        if (negate) {
            return !res;
        }

        return res;
    }

    public static boolean checkQueryCondition(int targetElementID, QueryCondition condition, QueryStructure queryStructure, int[] solution_nodes, int[] solution_edges) {
        //TODO: rewrite without if
        String operator = condition.getOperation();
        String elementName = condition.getNode_param().getElementName();
        String propertyName = condition.getNode_param().getElementKey();
        Object expressionValue = condition.getExpr_value();

        boolean res;

        if (expressionValue instanceof NameValue) { // COMPLEX CONDITION
            String secondElementName = ((NameValue) expressionValue).getElementName();
            String secondPropertyName = ((NameValue) expressionValue).getElementKey();

            Object2IntOpenHashMap<String> mapNodeNameToID = queryStructure.getMap_node_name_to_idx();
            Object2IntOpenHashMap<String> mapEdgeNameToID = queryStructure.getMap_edge_name_to_idx();

            int firstID, secondID, firstCandidateID, secondCandidateID;
            if (mapNodeNameToID.containsKey(elementName) && mapNodeNameToID.containsKey(secondElementName)) { // CONDITION ON NODES
                firstID = mapNodeNameToID.getInt(elementName);
                secondID = mapNodeNameToID.getInt(secondElementName);

                firstCandidateID = solution_nodes[firstID];
                secondCandidateID = solution_nodes[secondID];
            } else {  // CONDITION ON EDGES  -> mapEdgeNameToID.containsKey(elementName) && mapEdgeNameToID.containsKey(secondElementName)
                firstID = mapEdgeNameToID.getInt(elementName);
                secondID = mapEdgeNameToID.getInt(secondElementName);

                firstCandidateID = solution_edges[firstID];
                secondCandidateID = solution_edges[secondID];
            }

            Object firstCandidateValue = condition.getConditionCheck().getProperty(firstCandidateID, propertyName);
            Object secondCandidateValue = condition.getConditionCheck().getProperty(secondCandidateID, secondPropertyName);
            res = condition.getConditionCheck().getComparison().comparison(firstCandidateValue, secondCandidateValue, operator);
        } else { // SIMPLE CONDITION
            Object candidateValue = condition.getConditionCheck().getProperty(targetElementID, propertyName);
            res = condition.getConditionCheck().getComparison().comparison(candidateValue, expressionValue, operator);
        }

        boolean negate = condition.isNegation();

        if (negate) {
            return !res;
        }

        return res;
    }
}
