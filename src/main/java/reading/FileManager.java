package reading;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import configuration.Configuration;
import matching.models.OutData;
import target_graph.TableTypes;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class FileManager {
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
                Table table = Table.read().usingOptions(CsvReadOptions
                   .builder(file_name)
                   .separator(sep)
                   .columnTypes(table_columns)
                );
                TableTypes.column_rename(table);
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
