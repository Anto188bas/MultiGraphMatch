package cypher.models;


import cypher.controller.WhereConditionHandler;
import org.opencypher.v9_0.expressions.Expression;

public class WhereExpression {
    // left_expression and right one could be: QueryCondition or WhereExpression
    private final Object  left_expression;
    private final String  conj_operator;
    private final Object  right_expression;

    // CONSTRUCTOR
    public WhereExpression(Expression lft, String cj_op, Expression rft){
        left_expression  = WhereConditionHandler.where_condition_handler(lft);
        conj_operator    = cj_op;
        right_expression = WhereConditionHandler.where_condition_handler(rft);
    }

    // GETTER
    public Object getLeft_expression()  {return left_expression; }
    public String getConj_operator()    {return conj_operator;   }
    public Object getRight_expression() {return right_expression;}

    // TOSTRING

    @Override
    public String toString() {
        return "WhereExpression{"     +
                "left_expression="    + left_expression +
                ", conj_operator='"   + conj_operator + '\'' +
                ", right_expression=" + right_expression +
                '}';
    }
}
