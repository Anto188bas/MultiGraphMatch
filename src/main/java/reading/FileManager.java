package reading;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import configuration.Configuration;
import matching.models.OutData;
import target_graph.TableTypes;
import tech.tablesaw.api.*;
import tech.tablesaw.columns.strings.StringColumnType;
import tech.tablesaw.io.csv.CsvReadOptions;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.lang.Math;


public class FileManager {
    private final static HashMap<Integer,String> associations = new HashMap<>() {{
         put(1, "Integer");
         put(2, "Float"  );
         put(3, "Double" );
         put(4, "String" );
    }};

    // GET THE ELEMENT's TYPE
    private static int element_type_extraction(String a) {
        try {
            Integer.parseInt(a);
            return 1;
        } catch (Exception ignored){}
        try {
            Float.parseFloat(a);
            return 2;
        } catch (Exception ignored){}
        try {
            Double.parseDouble(a);
            return 3;
        } catch (Exception ignored){}
        return 4;
    }

    // IT IS USED TO INFER THE COLUMN TYPE
    private static String type_extraction(String a, String b, String c) {
        int first  = element_type_extraction(a);
        int second = element_type_extraction(b);
        int therd  = element_type_extraction(c);
        int max_v  = Math.max(first, Math.max(second, therd));
        return associations.get(max_v);
    }

    // IT ADDS A NEW COLUMN WHERE THE ELEMENT HAVE BEEN PROPERLY PARSED
    private static void add_new_column_creation(Table table, StringColumn sel_col, String type, String column_name) {
        switch (type) {
            case "Integer":
                IntColumn intCol = IntColumn.create(column_name);
                sel_col.forEach(value -> intCol.append(Integer.parseInt(value)));
                table.addColumns(intCol);
                break;
            case "Float":
                FloatColumn floatColumn = FloatColumn.create(column_name);
                sel_col.forEach(value -> floatColumn.append(Float.parseFloat(value)));
                table.addColumns(floatColumn);
                break;
            case "Double":
                DoubleColumn doubleCol = DoubleColumn.create(column_name);
                sel_col.forEach(value -> doubleCol.append(Double.parseDouble(value)));
                table.addColumns(doubleCol);
                break;
            default:
                table.addColumns(sel_col);
        }
    }


    public static Table[] files_reading(String elementsFolder, Character sep){
        File[] listFiles = new File(elementsFolder).listFiles();
        if(listFiles == null) return null;
        Table[] tables   = new Table[listFiles.length];
        int count        = 0;
        for(File file_name: listFiles){
            try {
                CSVParser parser = new CSVParserBuilder().withSeparator(sep).build();
                CSVReader reader = new CSVReaderBuilder(new FileReader(file_name)).withCSVParser(parser).build();
                ColumnType[] table_columns = TableTypes.header_table_creation(reader.readNext());
                reader.close();
                System.out.println(file_name.getName());
                Table table = Table.read().usingOptions(CsvReadOptions
                   .builder(file_name)
                   .separator(sep)
                   .maxCharsPerColumn(100000)
                   .columnTypes(table_columns)
                );
                TableTypes.column_rename(table);
                // SET NEW COLUMNS TYPE

                table.columnNames().forEach(column_name -> {
                    if (column_name.equals("source") || column_name.equals("dest")) return;
                    if (table.column(column_name).type() instanceof StringColumnType){
                        StringColumn tmp = (StringColumn) table.column(column_name);
                        table.removeColumns(column_name);
                        // SELECTED RECORD
                        String last   = tmp.get(tmp.size() - 1);
                        String first  = tmp.get(0);
                        String med    = tmp.get(tmp.size() / 2);
                        // INFERED TYPE
                        String ntype  =  type_extraction(first, last, med);
                        add_new_column_creation(table, tmp, ntype, column_name);
                    }
                });

                tables[count++] = table;
            } catch (CsvValidationException | IOException e) {e.printStackTrace();}
        }
        return tables;
    }

    public static List<String> query_reading(Configuration configuration) throws IOException {
        String path = configuration.query_file + "/query.txt";
        List<String> lines = Files.readAllLines(Path.of(path));
        return lines;
    }

    public static void saveIntoCSV(String query, String path, OutData outData) throws IOException {
        final BufferedWriter writer = new BufferedWriter(new FileWriter(path, true));
        String result = query + "\t" + outData.num_occurrences+ "\t" + outData.domain_time + "\t" + outData.ordering_time + "\t" + outData.symmetry_time + "\t" + outData.matching_time;
        writer.write(result + "\n");
        writer.close();
    }
}
