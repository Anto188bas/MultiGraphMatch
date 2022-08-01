package condition;
import scala.Function2;
import java.util.HashMap;


public class StringComparison extends Comparison{
    // ATTRIBUTES
    private HashMap<String, Function2<String, String, Boolean>> functions;

    // CONSTRUCTOR
    public StringComparison() {init_object();}

    // METHODS
    private void init_object() {
        this.functions = new HashMap<>();
        this.functions.put("Equals"    , String::equals    );
        this.functions.put("StartsWith", String::startsWith);
        this.functions.put("EndsWith"  , String::endsWith  );
        this.functions.put("Contains"  , String::contains  );
    }

    public boolean comparison(Object x, Object y, String operator){
        return functions.get(operator).apply((String) x, (String) y);
    }
}
