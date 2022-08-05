package cypher.controller;
import cypher.models.QueryCondition;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.sf.tweety.logics.pl.parser.PlParser;
import net.sf.tweety.logics.pl.syntax.PlFormula;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhereConditionExtraction {
    // ATTRIBUTES
    private String          where_string;
    private String          disj_where_cond;
    private HashSet<String> conditions;
    private final String[]  origin;
    private final String[]  replacement;
    private final String[]  new_origin;

    protected IntArrayList setWhereConditions;
    protected Object2IntOpenHashMap<String> map_condition_to_orPropositionPos;
    protected Int2IntOpenHashMap mapPropositionToNumConditions;
    protected Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<QueryCondition>> mapOrPropositionToConditionSet;
    protected Int2ObjectOpenHashMap<PropositionStatus> mapOrPropositionToStatus;
    protected Int2ObjectOpenHashMap<PropositionStatus> mapConditionToStatus;
    protected Object2IntOpenHashMap<String> mapConditionToAndChainPos;

    public int conditionIndex = 0;
    // WHERE CONDITION
    public WhereConditionExtraction(){
        this.origin      = new String[] {
           "\\s*\\|\\s*", "\\s*<>\\s*", "\\s*<=\\s*", "\\s*>=\\s*", "\\s*>\\s*", "\\s*<\\s*", "\\s*=~\\s*", "\\s*=\\s*",
           "\\s+IS NULL\\s+", "\\s+IS NOT NULL\\s+", "NOT\\s+", "\\s+STARTS WITH\\s+", "\\s+ENDS WITH\\s+", "\\s+CONTAINS\\s+",
           "\\s+IN\\s+"
        };
        this.replacement = new String[] {
          "::", "_NEQ_", "_LEQ_", "_GEQ_", "_G_", "_L_", "_MR_", "_OPEQ_", "_EQNULL_", "_NEQNULL_", "_N_", "_STARTS_WITH_",
          "_ENDS_WITH_", "_CONTAINS_", "_IN_"
        };
        this.new_origin  = new String[] {
          "|", "!=", "<=", ">=", ">", "<", "=~", "=", "=NULL", "!=NULL", "NOT ", " StartsWith ", " EndsWith ",
          " Contains ", " In "
        };
    }

    // METHODS
    public void where_condition_extraction(String query) {
        String regex = "WHERE (.*) RETURN";
        Pattern pat  = Pattern.compile(regex);
        Matcher mat  = pat.matcher(query);
        while (mat.find())
            where_string = mat.group(1);
    }

    private String origin2custom_characters(String[] orig, String[] rep, String new_where) {
        for(int i=0; i<orig.length; i++)
            new_where = new_where.replaceAll(orig[i], rep[i]);
        return new_where;
    }

    public void normal_form_computing() {
        PlParser normal_parser = new PlParser();
        String   new_where = origin2custom_characters(origin, replacement, this.where_string);
        new_where = new_where
                .replace("AND", "&&")
                .replace("OR", "||" )
                .replace("XOR", "^^");
        PlFormula formula   = normal_parser.parseFormula(new StringReader(new_where)).toDnf();
        String[] condvals   = new String[formula.getLiterals().size()];
        AtomicInteger count = new AtomicInteger();
        formula.getLiterals().forEach(literature ->
            condvals[count.getAndIncrement()] =
                 origin2custom_characters(replacement, new_origin, literature.toString())
        );
        this.conditions = new HashSet<>();
        this.conditions.addAll(List.of(condvals));
        System.out.println(conditions);
        new_where = origin2custom_characters(replacement, new_origin, formula.toString());
        this.disj_where_cond  = new_where;
    }


    // GETTER AND SETTER
    // # 1
    public String getWhere_string() {return where_string;}
    public void   setWhere_string(String where_string) {this.where_string = where_string;}

    // # 2
    public String getDisj_where_cond() {return disj_where_cond;}
    public void   setDisj_where_cond(String disj_where_cond) {this.disj_where_cond = disj_where_cond;}

    // # 3
    public HashSet<String> getConditions() {return conditions;}
    public void setConditions(HashSet<String> conditions) {this.conditions = conditions;}


    public void buildSetWhereConditions(){
        this.setWhereConditions = new IntArrayList();
        this.map_condition_to_orPropositionPos = new Object2IntOpenHashMap();
        this.mapPropositionToNumConditions = new Int2IntOpenHashMap();
        this.mapOrPropositionToConditionSet = new Int2ObjectOpenHashMap<>();
        this.mapOrPropositionToStatus = new Int2ObjectOpenHashMap<>();
        this.mapConditionToAndChainPos = new Object2IntOpenHashMap<>();
        this.mapConditionToStatus = new Int2ObjectOpenHashMap<>();

        System.out.println("********************************************************************************");
        System.out.println("ORIGINAL WHERE: " + this.where_string);
        System.out.println("DNF: " + this.disj_where_cond);
        System.out.println();

        String[] splitOR = this.disj_where_cond.split("\\|\\|");

        for(int i = 0; i < splitOR.length; i++) {
            this.mapOrPropositionToConditionSet.put(i, new Int2ObjectOpenHashMap<>());
            this.mapOrPropositionToStatus.put(i, PropositionStatus.NOT_EVALUATED);

            System.out.println("SPLIT OR " + i + ": " + splitOR[i]);

            String[] splitAND = splitOR[i].split("&&");
            for(int j = 0; j < splitAND.length; j++) {
                splitAND[j] = splitAND[j].replace("(", "").replace(")", "").replace("\"", "");
                System.out.println("\tSPLIT AND " + j + ": " + splitAND[j] + "\t\torPropositionPos: " + i);

                this.map_condition_to_orPropositionPos.put(splitAND[j], i);
                this.mapConditionToAndChainPos.put(splitAND[j], j);
            }
            this.mapPropositionToNumConditions.put(i, splitAND.length);

            setWhereConditions.add(splitAND.length); // numAndConditions
        }

        System.out.println("setWhereConditions: " + this.setWhereConditions);
        System.out.println("map_condition_to_orPropositionPos: " + this.map_condition_to_orPropositionPos);

        System.out.println("********************************************************************************");

    }

    // GET
    public IntArrayList getSetWhereConditions() {
        return setWhereConditions;
    }

    public Object2IntOpenHashMap<String> getMap_condition_to_orPropositionPos() {
        return map_condition_to_orPropositionPos;
    }

    public Int2IntOpenHashMap getMapPropositionToNumConditions() {
        return mapPropositionToNumConditions;
    }

    public Int2ObjectOpenHashMap<Int2ObjectOpenHashMap<QueryCondition>> getMapOrPropositionToConditionSet() {
        return mapOrPropositionToConditionSet;
    }

    public Int2ObjectOpenHashMap<PropositionStatus> getMapOrPropositionToStatus() {
        return mapOrPropositionToStatus;
    }

    public Object2IntOpenHashMap<String> getMapConditionToAndChainPos() {
        return mapConditionToAndChainPos;
    }

    public Int2ObjectOpenHashMap<PropositionStatus> getMapConditionToStatus() {
        return mapConditionToStatus;
    }
}
