package target_graph;

import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.Table;

import static tech.tablesaw.api.ColumnType.*;


public class TableTypes {
    private static void column_type_selector(ColumnType[] type, String element, int pos) {
        switch (element) {
            case "short" -> type[pos] = SHORT;
            case "int", "integer" -> type[pos] = INTEGER;
            case "float" -> type[pos] = FLOAT;
            case "double" -> type[pos] = DOUBLE;
            case "boolean" -> type[pos] = BOOLEAN;
            case "long" -> type[pos] = LONG;
            case "text" -> type[pos] = TEXT;
            case "date" -> type[pos] = LOCAL_DATE;
            case "time" -> type[pos] = LOCAL_TIME;
            case "date_time" -> type[pos] = LOCAL_DATE_TIME;
            default -> type[pos] = STRING;
        }
    }

    public static ColumnType[] header_table_creation(String[] header) {
        ColumnType[] types = new ColumnType[header.length];
        for (int i = 0; i < header.length; i++) {
            String[] name_type = header[i].split(":");
            if (name_type[0].equalsIgnoreCase("id")) {
                types[i] = INTEGER;
                continue;
            }
            column_type_selector(types, name_type.length > 1 ? name_type[1] : "string", i);
        }
        return types;
    }

    public static void column_rename(Table table) {
        table.columns().forEach(column -> {
            String name = column.name();
            column.setName(name.split(":")[0]);
        });
    }
}
