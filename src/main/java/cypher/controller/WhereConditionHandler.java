package cypher.controller;

import cypher.models.QueryCondition;
import cypher.models.WhereExpression;
import org.opencypher.v9_0.expressions.And;
import org.opencypher.v9_0.expressions.Expression;
import org.opencypher.v9_0.expressions.Or;

public class WhereConditionHandler {
    public static Object where_condition_handler(Expression expression){
        // COMPLEX WHERE CONDITION
        if (expression instanceof And) {
            And and_cond = (And) expression;
            return new WhereExpression(and_cond.lhs(), and_cond.getClass().getSimpleName(), and_cond.rhs());
        }
        else if(expression instanceof Or){
            Or or_cond   = (Or) expression;
            return new WhereExpression(or_cond.lhs(), or_cond.getClass().getSimpleName(), or_cond.rhs());
        }
        // TODO other cases
        // SINGLE CONDITION
        else
            return new QueryCondition(expression);
    }
}
