package configuration;

import java.io.PrintStream;

public class Configuration {
    public String nodes_main_directory;
    public String edges_main_directory;
    public String query_file;
    public String result_file;
    public String out_file;
    public int timeout;

    public Configuration(String[] args) {
        set_error();
        timeout = 1800;
        out_file = null;
        for (int i = 0; i < args.length; i++)
            option_setting(args[i], args[++i]);
        validate_input_parameter();
    }

    public void option_setting(String key, String value) {
        switch (key) {
            case "-n" -> nodes_main_directory = value;
            case "-e" -> edges_main_directory = value;
            case "-q" -> query_file = value;
            case "-r" -> result_file = value;
            case "-t" -> timeout = Integer.parseInt(value);
            case "-o" -> out_file = value;
            default -> printHelp(key);
        }
    }

    private void printHelp(String parameter) {
        String help = "Usage: java -jar MultiRI.jar -n <nodeFolder> -e <edgeFolder> -q <queryFile> -r <resultsFile>\n\n";
        help += "REQUIRED PARAMETERS:\n";
        help += "-n\tTarget nodes folder\n";
        help += "-e\tTarget edges folder\n";
        help += "-q\tQueries file\n";
        help += "-r\tResults file\n\n";
        help += "-u\tUse uncompressed data structure to store whole target\n\n";
        if (parameter != null) System.out.println("Error! Unrecognizable command '" + parameter + "'");
        System.out.println(help);
    }

    private void printError(String parameter) {
        if (parameter != null) return;
        System.out.println("Error! No path for target nodes has been specified!\n");
        printHelp(null);
        System.exit(1);
    }

    private void validate_input_parameter() {
        printError(nodes_main_directory);
        printError(edges_main_directory);
        // TODO add all checks
    }

    private void set_error() {
        System.setErr(new PrintStream(System.err) {
            public void println(String l) {
                if (!l.startsWith("SLF4J")) {
                    super.println(l);
                }
            }
        });
    }

    // TOSTRING
    @Override
    public String toString() {
        return "Configuration{" + "nodes_main_directory='" + nodes_main_directory + '\'' + ", edges_main_directory='" + edges_main_directory + '\'' + ", query_file='" + query_file + '\'' + ", result_file='" + result_file + '\'' + ", timeout=" + timeout + '}';
    }
}

//MATCH (n2:I)-[:N]->(n4:H), (n3:E)-[:M]->(n1:A), (n3)-[:N]->(n2), (n4)-[:N]->(n1), (n5:D)-[:M]->(n1), (n6:L)-[:M]->(n1), (n6)-[:M]->(n3), (n6)-[:M]->(n7:H), (n8:E)-[:N]->(n4) RETURN count(n1)